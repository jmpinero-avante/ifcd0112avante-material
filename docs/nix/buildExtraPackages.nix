{
  pkgs ? (import <nixpkgs>) {}
}: let
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

  pluginPkg = pkgs.python3Packages.buildPythonPackage {
    pname = "mkdocs-mermaid-xform-plugin";
    version = "1.0.0";
    src = ../plugins/mermaid-xform-plugin;

    format = "setuptools";
    propagatedBuildInputs = with pkgs.python3Packages; [
      mkdocs
      mkdocs-material
    ];

    # Durante el desarrollo no necesitamos instalarlo en el store, sino compilarlo localmente
    doCheck = false;
  };

  commonPkg = pkgs.stdenv.mkDerivation {
    pname = "mkdocs-common";
    version = "1.0";
    src = ../common;
    installPhase = ''
      mkdir -p $out/root
      mkdir -p $out/assets
      cp -r root/* $out/root/ 2>/dev/null || true
      cp -r assets/* $out/assets/ 2>/dev/null || true
    '';
  };

in
  { inherit mkdocsWithPdf pluginPkg commonPkg; }
