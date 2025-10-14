{
  description = "Documentación modular MkDocs con integración de subproyectos, assets comunes y múltiples outputs";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";

    # Plugin local
    mermaid-xform-plugin.url = "path:./plugins/mermaid-xform-plugin";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
    mermaid-xform-plugin,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      pkgs = import nixpkgs {inherit system;};
      python = pkgs.python312;

      # ✅ mkdocs-with-pdf desde PyPI (no está en nixpkgs)
      mkdocsWithPdf = pkgs.python3Packages.buildPythonPackage rec {
        pname = "mkdocs-with-pdf";
        version = "0.9.3";
        src = pkgs.fetchPypi {
          inherit pname version;
          sha256 = "sha256-vaM3XXBA0biHHaF8bXHqc2vcpsZpYI8o7WJ3EDHS4MY=";
        };
        pyproject = true;
        build-system = [
          pkgs.python3Packages.setuptools
          pkgs.python3Packages.wheel
        ];
        propagatedBuildInputs = with pkgs.python3Packages; [
          mkdocs
          mkdocs-material
          weasyprint
          lxml
          pyyaml
          markdown
          beautifulsoup4
          libsass
        ];
        doCheck = false;
      };

      # --- 1️⃣ COMMON PACKAGE ---
      commonPkg = pkgs.stdenv.mkDerivation {
        pname = "mkdocs-common";
        version = "1.0";
        src = ./common;
        installPhase = ''
          mkdir -p $out/root
          mkdir -p $out/assets/common
          cp -r root/* $out/root/ 2>/dev/null || true
          cp -r assets/* $out/assets/common/ 2>/dev/null || true
        '';
      };

      # --- 2️⃣ PLUGIN PACKAGE ---
      pluginPkg = pkgs.python3Packages.buildPythonPackage {
        pname = "mkdocs-mermaid-xform-plugin";
        version = "1.0.0";
        src = ./plugins/mermaid-xform-plugin;

        format = "setuptools";
        propagatedBuildInputs = with pkgs.python3Packages; [mkdocs mkdocs-material];

        # Durante el desarrollo no necesitamos instalarlo en el store, sino compilarlo localmente
        doCheck = false;
      };

      # --- 3️⃣ FUNCIÓN PARA PROYECTOS MKDOCS ---
      mkdocsProject = name: path:
        pkgs.stdenv.mkDerivation rec {
          pname = "mkdocs-site-${name}";
          version = "1.0";
          src = path;
          outputs = ["out" "pdf" "webpdf"];

          buildInputs = [
            python
            pluginPkg
            python.pkgs.mkdocs
            python.pkgs.mkdocs-material
            pkgs.nodePackages.mermaid-cli
            pkgs.google-fonts
            pkgs.dejavu_fonts
            pkgs.inkscape
          ]
          ## Dependencia con chromium en linux
          ++ pkgs.lib.optionals pkgs.stdenv.isLinux [ pkgs.chromium ];

          propagatedBuildInputs = [commonPkg];

          buildPhase = ''
            echo "🔗 Preparando enlaces comunes para ${name}..."
            ln -sf ${commonPkg}/root common
            mkdir -p docs/assets
            ln -sf ${commonPkg}/assets/common docs/assets/common

            echo "🚧 Construyendo sitio..."
            mkdocs build --clean

            echo "📄 Preparando PDFs..."
            mkdir -p pdf
            cp -r ${commonPkg}/assets/common/pdf/* pdf/ 2>/dev/null || true

            echo "📦 Combinando sitio y PDFs..."
            mkdir -p webpdf/site/assets
            cp -r site/* webpdf/site/
            mkdir -p webpdf/site/assets/pdf
            ln -sf ../../../pdf/* webpdf/site/assets/pdf/ 2>/dev/null || true
          '';

          installPhase = ''
            mkdir -p $out $pdf $webpdf
            cp -r site/* $out/
            cp -r pdf/* $pdf/ 2>/dev/null || true
            cp -r webpdf/* $webpdf/
          '';
        };

      # --- 4️⃣ PROYECTOS INDIVIDUALES ---
      modulo2 =
        mkdocsProject "modulo2-bases-de-datos"
        ./projects/modulo2-bases-de-datos;
      modulo3_1 = mkdocsProject "modulo3_1-java" ./projects/modulo3.1-java;

      siteRoot = pkgs.stdenv.mkDerivation {
        pname = "mkdocs-site-root";
        version = "1.0";
        src = ./projects/site-root;
        outputs = ["out"];

        buildInputs = [python python.pkgs.mkdocs python.pkgs.mkdocs-material pluginPkg];

        propagatedBuildInputs = [commonPkg];

        buildPhase = ''
          echo "🔗 Enlazando recursos comunes (site-root)..."
          ln -sf ${commonPkg}/root common
          mkdir -p docs/assets
          ln -sf ${commonPkg}/assets/common docs/assets/common

          echo "🚧 Construyendo sitio raíz..."
          mkdocs build --clean
        '';

        installPhase = ''
          mkdir -p $out
          cp -r site/* $out/
        '';
      };

      # --- 5️⃣ SITIO GLOBAL ---
      mkdocsAll = pkgs.stdenv.mkDerivation {
        pname = "mkdocs-site-all";
        version = "1.0";
        outputs = ["out" "pdf" "webpdf"];
        buildInputs = [pkgs.coreutils pkgs.findutils];
        propagatedBuildInputs = [commonPkg modulo2 modulo3_1 siteRoot];

        buildPhase = ''
          echo "🚀 Construyendo sitio raíz (site-root)..."
          cp -r ${siteRoot.out} ./site-root
          cd site-root

          echo "📂 Integrando subproyectos dentro del site raíz..."
          mkdir -p site/modulo2-bases-de-datos
          mkdir -p site/modulo3.1-java

          cp -r ${modulo2.out}/* site/modulo2-bases-de-datos/
          cp -r ${modulo3_1.out}/* site/modulo3.1-java/

          echo "🎨 Fusionando assets de subproyectos..."
          for assetsDir in ${modulo2.out}/assets ${modulo3_1.out}/assets; do
            if [ -d "$assetsDir" ]; then
              echo "→ copiando assets de $assetsDir"
              cp -rn $assetsDir/* site/assets/ 2>/dev/null || true
            fi
          done

          echo "📄 Copiando PDFs globales..."
          mkdir -p pdf
          cp -r ${commonPkg}/assets/common/pdf/* pdf/ 2>/dev/null || true
          cp -r ${modulo2.pdf}/* pdf/ 2>/dev/null || true
          cp -r ${modulo3_1.pdf}/* pdf/ 2>/dev/null || true

          echo "🔗 Creando enlaces simbólicos a los PDFs..."
          mkdir -p site/assets/pdf
          ln -sf ../../../pdf/* site/assets/pdf/ 2>/dev/null || true

          echo "📦 Preparando outputs finales..."
          mkdir -p $out $pdf $webpdf
          cp -r site/* $out/
          cp -r pdf/* $pdf/ 2>/dev/null || true

          echo "🌐 Creando webpdf (web + pdfs simbólicos)..."
          cp -r site/* $webpdf/
          mkdir -p $webpdf/assets/pdf
          ln -sf ../../pdf/* $webpdf/assets/pdf/ 2>/dev/null || true
        '';
      };
    in {
      # --- 6️⃣ EXPORTS ---
      packages = {
        mkdocs-common = commonPkg;
        mkdocs-plugin-mermaid-xform = pluginPkg;
        mkdocs-site-modulo2-bases-de-datos = modulo2;
        mkdocs-site-modulo3_1-java = modulo3_1;
        mkdocs-site-root = siteRoot;
        default = mkdocsAll;
      };

      # --- 7️⃣ DEV SHELL ---
      devShells.default = pkgs.mkShell {
        name = "mkdocs-dev-shell";

        # 📦 Herramientas incluidas en el entorno
        packages = with pkgs; [
          zsh
          neovim
          python3
          fontconfig.bin
          fontconfig.out
          pkgs.nodePackages.mermaid-cli
          pkgs.google-fonts
          pkgs.dejavu_fonts
          pkgs.inkscape
          (python3.withPackages (ps:
            with ps; [
              pip
              setuptools
              wheel
              mkdocs
              mkdocs-material
              pymdown-extensions
              weasyprint
              beautifulsoup4
              lxml
              mkdocsWithPdf
              pluginPkg
            ]))
          ]

          ## Chromium solo en linux
          ++ pkgs.lib.optionals pkgs.stdenv.isLinux [ pkgs.chromium ];

        # 🎯 Configuración al entrar en el entorno

        shellHook = let
          chromePath =
            if pkgs.stdenv.isDarwin then
              "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
            else
              "${pkgs.chromium}/bin/chromium";
        in ''
          echo ""
          echo "🧠 Entorno IFCD0112 Avante activado para ${system}"
          echo "----------------------------------------------------"
          echo "  Shell   : Zsh disponible"
          echo "  Editor  : Neovim instalado"
          echo "  Python  : $(python3 --version)"
          echo "  MkDocs  : $(mkdocs --version)"
          echo ""
          echo "🔗 Creando enlaces simbólicos a recursos comunes..."

          export FONTCONFIG_FILE=${pkgs.fontconfig.out}/etc/fonts/fonts.conf
          export EDITOR=nvim

          export PUPPETEER_EXECUTABLE_PATH="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
          echo "🔗 Puppeteer usará el Chrome del sistema en: $PUPPETEER_EXECUTABLE_PATH"

          alias ls='command ls --color=auto -Ah'
          alias l='command ls --color=auto -Ah'
          alias ll='l -l'
          alias vi=vim
          alias vim='command nvim'

          echo "🔧 Aliases disponibles: l, ll, vi, mdserve, mdbuild"

          for proj in projects/*; do
            if [ -d "$proj" ]; then
              echo "→ Configurando $proj"
              mkdir -p "$proj/docs/assets"

              # Limpiar y enlazar root común
              rm "$proj/common" 2> /dev/null
              ln -sf ../../common/root "$proj/common"

              # Limpiar y enlazar assets comunes (4 niveles arriba)
              rm "$proj/docs/assets/common" 2> /dev/null
              ln -sf ../../../../common/assets "$proj/docs/assets/common"
            fi
          done

          ## ACTUALIZA EL PYTHONPATH
          export PYTHONPATH="$PWD/plugins/mermaid-xform-plugin:$PYTHONPATH"

          # FIN
          echo "✅ Listo. Entra en un subproyecto y ejecuta: mkdocs serve"
        '';
      };
    });
}
