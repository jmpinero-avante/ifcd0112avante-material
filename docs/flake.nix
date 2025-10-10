{
  description = "Entorno de documentación IFCD0112 Avante con MkDocs Material y PDF export (portable)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # 🔹 Plugin mkdocs-pdf-export-plugin (no está en nixpkgs)
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
            mkdocs
            beautifulsoup4
            lxml
            weasyprint
          ];

          doCheck = false;
        };
      in {
        devShells.default = pkgs.mkShell {
          name = "ifcd0112avante-docs";

          packages = with pkgs.python3Packages; [
            mkdocs
            mkdocs-material
            pymdown-extensions
            weasyprint
            beautifulsoup4
            lxml
            mkdocsPdfExportPlugin
          ];

          shellHook = ''
            echo ""
            echo "🧠 Entorno de documentación IFCD0112 Avante activado para ${system}"
            echo "----------------------------------------------------"
            echo "  Python : $(python3 --version)"
            echo "  MkDocs : $(mkdocs --version)"
            echo ""
            echo "📘 Usa 'cd pages && mkdocs serve' para previsualizar"
            echo "📗 Usa 'mkdocs build' para generar PDFs y sitio estático"
            echo ""
          '';
        };
      });
}

