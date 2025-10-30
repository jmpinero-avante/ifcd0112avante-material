{
  pkgs ? (import <nixpkgs>) {},
  pythonPkg ? null,
  version ? "1.0",
  ...
}: let
  python_ =
    if builtins.isNull pythonPkg
    then pkgs.python3
    else pythonPkg;
  pythonPackages = python_.pkgs;

  # mkdocs-with-pdf desde PyPI (no está en nixpkgs)
  mkdocsWithPdf = pythonPackages.buildPythonPackage rec {
    pname = "mkdocs-with-pdf";
    version = "0.9.3";
    src = pkgs.fetchPypi {
      inherit pname version;
      sha256 = "sha256-vaM3XXBA0biHHaF8bXHqc2vcpsZpYI8o7WJ3EDHS4MY=";
    };
    pyproject = true;
    build-system = [pythonPackages.setuptools pythonPackages.wheel];
    propagatedBuildInputs = with pythonPackages; [
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

  pluginPkg = pythonPackages.buildPythonPackage {
    inherit version;
    pname = "mkdocs-mermaid-xform-plugin";
    src = ../plugins/mermaid-xform-plugin;

    format = "setuptools";
    propagatedBuildInputs = with pythonPackages; [
      mkdocs
      mkdocs-material
    ];

    # Durante el desarrollo no necesitamos instalarlo en el store, sino compilarlo localmente
    doCheck = false;
  };

  commonPkg = pkgs.stdenv.mkDerivation {
    inherit version;
    pname = "mkdocs-common";
    src = ../common;
    installPhase = ''
      mkdir -p $out/root
      mkdir -p $out/assets
      cp -r root/* $out/root/ 2>/dev/null || true
      cp -r assets/* $out/assets/ 2>/dev/null || true
    '';
  };
in {inherit mkdocsWithPdf pluginPkg commonPkg;}
