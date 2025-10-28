let
  fx = import ./generateAllDerivations.nix;
  all = fx {};
in
  all.projects.module2-db.der-sitepdf
