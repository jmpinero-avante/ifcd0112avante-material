let
  inherit
    (builtins)
    attrNames
    concatLists
    concatStringsSep
    getAttr
    hasAttr
    listToAttrs
    map
    trace
    ;
in let
  all = (import ./generateAllDerivations.nix) {};

  projects_ =
    attrNames all.projects;
  project_str_ =
    concatStringsSep
    "\n    "
    (map (v: "* \"${v}\"") projects_);

  fx_msg_ = ''
    buildProject

    Call this function with the following set:
    {
      target =
        * "all" -> all projects (default option)

        ${project_str_}
      ;

      mode =
        * "site"    -> build the site (default option)
        * "pdf"     -> build the pdf
        * "sitepdf" -> build site + pdf
      ;
    }
  '';

  get_target_ = tgt:
    if tgt == "all"
    then all.all
    else
      (
        if hasAttr tgt all.projects
        then getAttr tgt all.projects
        else null
      );

  get_target_der_ = aset: mode_: let
    mode = "der-${mode_}";
  in
    if isNull aset
    then null
    else
      (
        if hasAttr mode aset
        then getAttr mode aset
        else null
      );

  get_der_base_ = tgt: mode: get_target_der_ (get_target_ tgt) mode;

  get_der_ = tgt: mode: let
    trace_args = "INFO {target=\"${tgt}\"; mode=\"${mode}\";}";
    der = trace trace_args (get_der_base_ tgt mode);
  in
    if isNull der
    then trace "${trace_args} NOT VALID" null
    else der;

  ders_ = ["site" "sitepdf" "pdf"];

  targets_ = ["all"] ++ projects_;

  objects_der_ = d:
    map (tgt: {
      name = "${tgt}-${d}";
      value = get_der_ tgt d;
    })
    targets_;

  objects_ = listToAttrs (concatLists (map (d: (objects_der_ d)) ders_));

  der_names_ = attrNames objects_;

  der_names_str_ = concatStringsSep "\n  " (map (v: "* ${v}") der_names_);

  der_names_msg_ = ''
    Derivations:
      ${der_names_str_}
  '';

  help_msg = ''
    ${der_names_msg_}

    ${fx_msg_}
  '';

  derivations = trace help_msg objects_;

  buildProject = {
    target ? "all",
    mode ? "site",
  }:
    get_der_ target mode;
in {
  inherit buildProject help_msg derivations;
}
