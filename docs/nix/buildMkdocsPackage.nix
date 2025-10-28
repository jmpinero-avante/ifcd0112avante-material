{
  pkgs ? (import <nixpkgs>) {},
  name,
  version ? "2.0",
  src,
  withPdf ? true,
}: let
  extra_pkgs = (import ./buildExtraPackages.nix) { inherit pkgs; };

  inherit (extra_pkgs) mkdocsWithPdf pluginPkg commonPkg;


  cmd_linkCommon = ''
    ln -sf ${commonPkg.out}/root common 2>/dev/null || true;
    mkdir -p docs/assets
    ln -sf ${commonPkg.out}/assets docs/assets/common 2>/dev/null || true;
  '';

  pdf_env = withPdf:
    if withPdf
    then "ENABLE_PDF_EXPORT=1"
    else "";

  cmd_build_site = withPdf: ''
    echo "Preparando enlaces comunes"
    ${cmd_linkCommon}

    echo "Construyendo sitio..."
    ${pdf_env withPdf} mkdocs build --clean
  '';

  cmd_install_site = ''
    mkdir -p $out

    echo "Instalando website"
    cp -r site $out/ 2>/dev/null || true
  '';

  cmd_install_pdf = sitepdf: ''
    echo "Linking PDFS"
    mkdir -p $out/pdf
    cp -r ${sitepdf.out}/site/assets/pdf/*.pdf $out/pdf/ 2>/dev/null || true
  '';

  deps_base_nopdf = with pkgs; [python3 nodePackages.mermaid-cli commonPkg];

  deps_base_pdf = with pkgs;
    [
      fontconfig.bin
      fontconfig.out
      google-fonts
      dejavu_fonts
      inkscape
    ]
    ++ (pkgs.lib.optionals pkgs.stdenv.isLinux [pkgs.chromium]);

  deps_python_nopdf = ps:
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
    ];

  build_deps_ = base: base ++ [(pkgs.python3.withPackages deps_python_nopdf)];

  deps_nopdf = build_deps_ deps_base_nopdf;

  deps_pdf = build_deps_ (deps_base_nopdf ++ deps_base_pdf);

  outputs = ["out"];

  basename = "mkdocs-${name}";

  der-site = pkgs.stdenv.mkDerivation rec {
    inherit version outputs src;

    pname = "${basename}-site";
    buildInputs = deps_nopdf;
    buildPhase = cmd_build_site false;
    installPhase = cmd_install_site;
  };

  der-sitepdf = pkgs.stdenv.mkDerivation rec {
    inherit version outputs src;

    pname = "${basename}-sitepdf";
    buildInputs = deps_pdf;
    buildPhase = cmd_build_site true;
    installPhase = cmd_install_site;
  };

  der-pdf = pkgs.stdenv.mkDerivation rec {
    inherit version outputs src;

    pname = "${basename}-pdf";
    buildInputs = [der-sitepdf];
    dontBuild = true;
    installPhase = cmd_install_pdf der-sitepdf;
  };

  export_pdf = {inherit der-site der-sitepdf der-pdf;};

  export_nopdf = {inherit der-site;};

  exports =
    if withPdf
    then export_pdf
    else export_nopdf;
in
  exports
