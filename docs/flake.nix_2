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
      ## PACKAGES
      pkgs = import nixpkgs {inherit system;};
      python = pkgs.python312;

      ## VERSION
      defversion = "2.0";

      ## DEPENDENCIAS
      # mkdocs-with-pdf desde PyPI (no está en nixpkgs)
      mkdocsWithPdf = pkgs.python3Packages.buildPythonPackage rec {
        pname = "mkdocs-with-pdf";
        version = "0.9.3";
        src = pkgs.fetchPypi {
          inherit pname version;
          sha256 = "sha256-vaM3XXBA0biHHaF8bXHqc2vcpsZpYI8o7WJ3EDHS4MY=";
        };
        pyproject = true;
        build-system = [pkgs.python3Packages.setuptools pkgs.python3Packages.wheel];
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

      # pluginPkg
      pluginPkg = pkgs.python3Packages.buildPythonPackage {
        pname = "mkdocs-mermaid-xform-plugin";
        version = "1.0.0";
        src = ./plugins/mermaid-xform-plugin;

        format = "setuptools";
        propagatedBuildInputs = with pkgs.python3Packages; [
          mkdocs
          mkdocs-material
        ];

        # Durante el desarrollo no necesitamos instalarlo en el store, sino compilarlo localmente
        doCheck = false;
      };

      ## Common PKG
      commonPkg = pkgs.stdenv.mkDerivation {
        pname = "mkdocs-common";
        version = "1.0";
        src = ./common;
        installPhase = ''
          mkdir -p $out/root
          mkdir -p $out/assets
          cp -r root/* $out/root/ 2>/dev/null || true
          cp -r assets/* $out/assets/ 2>/dev/null || true
        '';
      };

      ##  =====> FUNCION MKDOCS
      buildMkDocs = {
        basename,
        version ? defversion,
        src,
        withPdf ? false,
      }: let
        pdf_env = withPdf:
          if withPdf
          then "ENABLE_PDF_EXPORT=1"
          else "";

        install_site = ''
          ## Instalando el website
          echo "\nInstalando website"
          mkdir -p $out
          cp -r site $out/ 2>/dev/null || true
        '';

        install_pdf =
          if withPdf
          then ''
            ## Instalando pdfs
            echo "\nInstalando pdfs"
            mkdir -p $out/pdf
            cp -r $out/site/assets/pdf/*.pdf $out/pdf/ 2>/dev/null || true
          ''
          else "";
      in
        pkgs.stdenv.mkDerivation rec {
          inherit version src;

          # package name
          name =
            if withPdf
            then "${basename}-pdfsite"
            else "${basename}-site";

          ## build inputs
          buildInputs =
            [
              pkgs.nodePackages.mermaid-cli
              commonPkg
              (python.withPackages (ps:
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
                  pluginPkg
                  mkdocsWithPdf
                ]))
            ]
            ++ (pkgs.lib.optionals pkgs.stdenv.isLinux [pkgs.chromium])
            ++ (pkgs.lib.optionals withPdf [
              pkgs.fontconfig.bin
              pkgs.fontconfig.out
              pkgs.google-fonts
              pkgs.dejavu_fonts
              pkgs.inkscape
            ]);

          ## Build Phase
          buildPhase = ''
            ## Link common stuff
            echo "\nLink common stuff"
            ln -sf ${commonPkg.out}/root common 2>/dev/null || true;
            mkdir -p docs/assets
            ln -sf ${commonPkg.out}/assets docs/assets/common 2>/dev/null || true;

            ## Build site
            echo "\nBuild Site"
            ${pdf_env withPdf} mkdocs build --clean
          '';

          # Install phase
          installPhase = ''
            ${install_site}
            ${install_pdf}
          '';
        };

      buildMkDocsSitePdf = {
        basename,
        version ? defversion,
        src,
      }: let
      in {
        site = buildMkDocs {
          inherit version src basename;
          withPdf = false;
        };
        pdfsite = buildMkDocs {
          inherit version src basename;
          withPdf = true;
        };
      };

      buildMkdocsAll = {
        version ? defversion,
        withPdf ? false,
      }: let
        copy_project_pdf = prj:
          if withPdf
          then ''
            cp -rn ${prj.out}/pdf/*.pdf $out/pdf 2>/dev/null || true
          ''
          else "";

        copy_project = prj: ''
          cp -rn ${prj.out}/site/* $out/site/ 2>/dev/null || true
          ${copy_project_pdf prj}
        '';

        copy_all_projects =
          builtins.concatStringsSep ""
          (builtins.map (v: copy_project v) inputs);

        create_output_pdf =
          if withPdf
          then ''
            # create pdf output folder
            echo "\n Create Pdf Output"
            mkdir -p $out/pdf
          ''
          else "";

        pkgKey =
          if withPdf
          then "pdfsite"
          else "site";

        project_inputs =
          builtins.map (v: v."${pkgKey}") (builtins.attrValues projects);

        inputs = [root-site] ++ project_inputs;
      in
        pkgs.stdenv.mkDerivation rec {
          inherit version;
          name =
            if withPdf
            then "allsite-pdfsite"
            else "allsite-site";

          buildInputs = inputs;

          buildPhase = ''
            # create site output folder
            echo "\n Create Site Output"
            mkdir -p $out/site

            ${create_output_pdf}

            # Copy details from the internal projects
            echo "\nCopy Details from the internal projects"
            ${copy_all_projects}
          '';
        };

      ####### SITE ROOT
      root-site = buildMkDocs {
        basename = "root";
        src = ./common;
        withPdf = false;
      };

      ###### PROJECTS
      projects = {
        module2-db = buildMkDocsSitePdf {
          basename = "module2-db";
          src = ./projects/module2-db;
        };

        module3-java = buildMkDocsSitePdf {
          basename = "module3-java";
          src = ./projects/module3-java;
        };

        module3-app-springboot = buildMkDocsSitePdf {
          basename = "module3-app-springboot";
          src = ./projects/module3-app-springboot;
        };

        module3-hibernate = buildMkDocsSitePdf {
          basename = "module3-hibernate";
          src = ./projects/module3-hibernate;
        };
      };

      ### SITIO COMPLETO JUNTO
      allsite = {
        site = buildMkdocsAll {withPdf = false;};
        pdfsite = buildMkdocsAll {withPdf = true;};
      };

      ### AGRUPANDO PAQUETES
      exportpkgs_nested = {inherit allsite;} // projects;

      exportpkgs = builtins.listToAttrs (builtins.concatLists
        (builtins.attrValues (builtins.mapAttrs
          (k: v: [
            {
              name = "${k}-site";
              value = v.site;
            }
            {
              name = "${k}-pdfsite";
              value = v.pdfsite;
            }
          ])
          exportpkgs_nested)));
    in {
      # --- EXPORTS ---
      packages = exportpkgs // {default = allsite.site;};

      # --- DEV SHELL ---
      devShells.default = pkgs.mkShell {
        name = "mkdocs-dev-shell";

        # Herramientas incluidas en el entorno
        packages = with pkgs;
          [
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
          ++ pkgs.lib.optionals pkgs.stdenv.isLinux [pkgs.chromium];

        # Configuración al entrar en el entorno

        shellHook = let
          chromePath =
            if pkgs.stdenv.isDarwin
            then "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
            else "${pkgs.chromium}/bin/chromium";
        in ''
          echo ""
          echo "Entorno IFCD0112 Avante activado para ${system}"
          echo "----------------------------------------------------"
          echo "  Shell   : Zsh disponible"
          echo "  Editor  : Neovim instalado"
          echo "  Python  : $(python3 --version)"
          echo "  MkDocs  : $(mkdocs --version)"
          echo ""
          echo "Creando enlaces simbólicos a recursos comunes..."

          export FONTCONFIG_FILE=${pkgs.fontconfig.out}/etc/fonts/fonts.conf
          export EDITOR=nvim

          export PUPPETEER_EXECUTABLE_PATH="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
          echo "Puppeteer usará el Chrome del sistema en: $PUPPETEER_EXECUTABLE_PATH"

          alias ls='command ls --color=auto -Ah'
          alias l='command ls --color=auto -Ah'
          alias ll='l -l'
          alias vi=vim
          alias vim='command nvim'

          echo "Aliases disponibles: l, ll, vi, mdserve, mdbuild"

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
          echo "Listo. Entra en un subproyecto y ejecuta: mkdocs serve"
        '';
      };
    });
}
