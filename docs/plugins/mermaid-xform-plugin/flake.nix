{
  description = "MkDocs plugin local: mermaid-xform (inyecta estilos y envoltorios automáticos a bloques Mermaid)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        python = pkgs.python312;

        # Directorio fuente del plugin
        src = ./.;

        # Crear paquete Python editable
        mermaid-xform-plugin = python.pkgs.buildPythonPackage {
          pname = "mkdocs-mermaid-xform-plugin";
          version = "1.0.0";
          format = "setuptools";
          src = ./.;

          propagatedBuildInputs = with python.pkgs; [
            mkdocs
            mkdocs-material
          ];

          # Incluye assets del plugin (CSS)
          installPhase = ''
            mkdir -p $out/lib/python3.12/site-packages/mermaid_xform_plugin/assets
            cp -r mermaid_xform_plugin $out/lib/python3.12/site-packages/
            if [ -d assets ]; then
              cp -r assets/* $out/lib/python3.12/site-packages/mermaid_xform_plugin/assets/
            fi
          '';

          meta = with pkgs.lib; {
            description = "MkDocs plugin que transforma bloques personalizados en bloques Mermaid, con estilos y centrado automático";
            license = licenses.mit;
            maintainers = [ "Juan Manuel Piñero Sánchez" ];
          };
        };

      in {
        # Paquete exportado para que el flake principal lo pueda importar
        packages = {
          ${system}.mermaid-xform-plugin = mermaid-xform-plugin;
          default = mermaid-xform-plugin;
        };

        # Entorno de desarrollo local del plugin
        devShells.default = pkgs.mkShell {
          name = "mermaid-xform-plugin-dev";
          packages = with pkgs; [
            python
            python.pkgs.setuptools
            python.pkgs.wheel
            python.pkgs.mkdocs
            python.pkgs.mkdocs-material
          ];

          shellHook = ''
            echo "🐍 Entorno de desarrollo del plugin Mermaid-XForm"
            echo "Puedes probarlo con:"
            echo "  python -m mkdocs serve"
          '';
        };
      }
    );
}
