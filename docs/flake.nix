{
  description = "Entorno de documentación IFCD0112 Avante con MkDocs, Zsh y Neovim";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        mkdocsPdfExportPlugin = pkgs.python3Packages.buildPythonPackage rec {
          pname = "mkdocs-pdf-export-plugin";
          version = "0.5.10";
          src = pkgs.fetchPypi {
            inherit pname version;
            sha256 = "sha256-d7qJ6+pvVlKG+wQ2Dj2YEFZ+JzAjg8nitvo4b47Mp9k=";
          };
          pyproject = true;
          build-system = [ pkgs.python3Packages.setuptools pkgs.python3Packages.wheel ];
          propagatedBuildInputs = with pkgs.python3Packages; [
            mkdocs beautifulsoup4 lxml weasyprint
          ];
          doCheck = false;
        };
      in {
        devShells.default = pkgs.mkShell {
          name = "ifcd0112avante-docs";

          # 📦 Herramientas incluidas
          packages = with pkgs; [
            zsh
            neovim
            python3
            python3Packages.mkdocs
            python3Packages.mkdocs-material
            python3Packages.pymdown-extensions
            python3Packages.weasyprint
            python3Packages.beautifulsoup4
            python3Packages.lxml
            mkdocsPdfExportPlugin
          ];

          # Shell Hook: comandos al entrar al entorno
          shellHook = ''
            echo ""
            echo "Entorno de documentación IFCD0112 Avante activado para ${system}"
            echo "----------------------------------------------------"
            echo "  Shell   : Zsh disponible"
            echo "  Editor  : Neovim instalado"
            echo "  Python  : $(python3 --version)"
            echo "  MkDocs  : $(mkdocs --version)"
            echo ""
            echo "📘 Usa 'cd pages && mkdocs serve' para previsualizar"
            echo "📗 Usa 'mkdocs build' para generar PDFs y sitio estático"
            echo ""

            # Activar alias personalizados
            export EDITOR=nvim

            alias ls='command ls --color=auto -Ah'
            alias  l='command ls --color=auto -Ah'
            alias ll='l -l'

            alias  vi=vim
            alias vim='command nvim'

            alias mdserve='cd pages && mkdocs serve'
            alias mdbuild='cd pages && mkdocs build --clean'

            echo "🔧 Aliases disponibles: l, ll, vi, mdserve, mdbuild"
            echo "🔧 ZSH disponible"
            echo ""
          '';
        };
      });
}

