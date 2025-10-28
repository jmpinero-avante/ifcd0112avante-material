{
  pkgs ? (import <nixpkgs>) {},
  dir ? ../projects,
  root ? ../site-root,
  version ? "1.0",
}: let
  extra_pkgs = (import ./buildExtraPackages.nix) { inherit pkgs; };

  fx = import ./buildMkdocsPackage.nix;

  all_entries = builtins.readDir dir;

  filterAttrs = pred: attrs:
    builtins.listToAttrs (builtins.filter (x: pred x.name x.value)
      (builtins.attrValues (builtins.mapAttrs (name: value: {
          name = name;
          value = value;
        })
        attrs)));

  dirnames =
    builtins.attrNames
    (filterAttrs (k: v: (v == "directory" && (builtins.substring 0 1 k) != "."))
      all_entries);

  build_entry = name: let
    src = "${dir}/${name}";
  in
    {
      inherit name src;
    }
    // fx {
      inherit name pkgs version;
      withPdf = true;
      src=builtins.toPath src;
    };

  projects = builtins.listToAttrs (builtins.map (v: {
      name = v;
      value = build_entry v;
    })
    dirnames);

  site-root = let
    name = "site-root";
    src = root;
  in
    {
      inherit name src version;
    }
    // fx {
      inherit name src pkgs;
      withPdf = false;
    };

  gen-all-derivation = name: isSite: let
    copy_project_site = prj: ''
      cp -rn ${prj.out}/site/* $out/site/ 2>/dev/null || true
    '';
    copy_project_pdf = prj: ''
      cp -rn ${prj.out}/pdf/*.pdf $out/pdf 2>/dev/null || true
    '';
    copy_project =
      if isSite
      then copy_project_site
      else copy_project_pdf;

    project_data = builtins.attrValues (builtins.mapAttrs (_: v: rec {
        der = v."${name}";
        copy_site = copy_project der;
      })
      projects);

    root_der = site-root."${name}";

    root_copy_site = copy_project_site root_der;
    root_copy =
      if isSite
      then root_copy_site
      else "";

    all_copy =
      builtins.concatStringsSep ""
      ([root_copy] ++ (builtins.map (v: v.copy_site) project_data));

    buildInputs = (builtins.map (v: v.der) project_data ) ++ [root_der];

    make_site = "mkdir -p $out/site";
    make_pdf = "mkdir -p $out/pdf";

    make_out =
      if isSite
      then make_site
      else make_pdf;
  in
    pkgs.stdenv.mkDerivation rec {
      inherit version buildInputs;
      pname = "mkdocs-all-${name}";
      src = dir;
      buildPhase = ''
        ${make_out}

        ${all_copy}
      '';
    };

  all = {
    name = "all";
    src = dir;
    der-site = gen-all-derivation "der-site" true;
    der-sitepdf = gen-all-derivation "der-sitepdf" true;
    der-pdf = gen-all-derivation "der-pdf" false;
  };
in {
  inherit site-root projects all;
  extra=extra_pkgs;
}
