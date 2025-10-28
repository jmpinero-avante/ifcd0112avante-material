let
  fx = import ./generateAllDerivations.nix;
  all = fx {};
in
  all.all.der-site
