let
  fx = import ./generateAllDerivations.nix;
  all = fx {};
in
  all.projects.module3-app-springboot.der-sitepdf
