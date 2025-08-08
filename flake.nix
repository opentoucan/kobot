{
  description = "Kobot flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs = { nixpkgs, ... } @ inputs:
    let
      pkgs = nixpkgs.legacyPackages."x86_64-linux";
    in
    {
      devShells."x86_64-linux".default = with pkgs; mkShell {
        packages = [
          jdk
          gradle
          ktlint
          detekt
          go-task
          pre-commit
          act
        ];

      };
  };
}
