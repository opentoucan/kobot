{
  description = "Kobot flake";

  inputs = {
    nixpkgs-unstable.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs = { nixpkgs-unstable, ... } @ inputs:
    let
      pkgs = nixpkgs-unstable.legacyPackages."x86_64-linux";
    in
    {
      devShells."x86_64-linux".default = with pkgs; mkShell {
        packages = [
          zulu25
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
