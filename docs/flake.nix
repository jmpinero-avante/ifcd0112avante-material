{
  description = "Entorno de documentación IFCD0112 Avante con MkDocs, Zsh y Neovim";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      pkgs = import nixpkgs {inherit system;};

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
    in {
      devShells.default = pkgs.mkShell {
        name = "ifcd0112avante-docs";

        # 📦 Herramientas incluidas en el entorno
        packages = with pkgs; [
          zsh
          neovim
          python3
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
            ]))
        ];

        # 🎯 Configuración al entrar en el entorno
        shellHook = ''
          echo ""
          echo "🧠 Entorno IFCD0112 Avante activado para ${system}"
          echo "----------------------------------------------------"
          echo "  Shell   : Zsh disponible"
          echo "  Editor  : Neovim instalado"
          echo "  Python  : $(python3 --version)"
          echo "  MkDocs  : $(mkdocs --version)"
          echo ""
          echo "📘 Usa 'cd pages && mkdocs serve' para previsualizar"
          echo "📗 Usa 'mkdocs build' para generar PDFs y sitio estático"
          echo ""
          export EDITOR=nvim
          alias ls='command ls --color=auto -Ah'
          alias l='command ls --color=auto -Ah'
          alias ll='l -l'
          alias vi=vim
          alias vim='command nvim'
          alias mdserve='cd pages && mkdocs serve'
          alias mdbuild='cd pages && mkdocs build --clean'
          echo "🔧 Aliases disponibles: l, ll, vi, mdserve, mdbuild"
          echo ""
        '';
      };
    });
}
