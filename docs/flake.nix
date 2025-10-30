{
  description = "Documentación modular MkDocs con integración de subproyectos, assets comunes y múltiples outputs";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (
      system: let
        ## PACKAGES
        pkgs = import nixpkgs {inherit system;};
        pythonPkg = pkgs.python3;

        ## VERSION
        version = "2.0";

        ## OBJECTS
        objects_ = (import ./nix) {
          inherit pkgs pythonPkg version;
          showHelp = false;
        };

        packages_ = objects_.derivations;

        default = packages_.all-site;

        packages = packages_ // {inherit default;};
      in {
        inherit packages;
      }
    );
}
