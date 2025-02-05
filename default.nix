# Gradle's toolchain support does not work with IntelliJ, thus we have to use buildFHSUserEnv

# https://discourse.nixos.org/t/how-to-create-a-development-environment-with-intellij-idea-and-openjdk/10153
{
  pkgs ? import <nixpkgs> {
     # https://github.com/NixOS/nixpkgs/issues/166220#issuecomment-1745803058
     config.allowUnfree = true;
  }
}:

let
  unstable = import (fetchTarball https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz) { };
  jdk = unstable.jdk23;
  gradle = unstable.gradle;
  ktfmt = unstable.ktfmt;
  go-task = unstable.go-task;
  pre-commit = unstable.pre-commit;
in

(
  pkgs.buildFHSUserEnv
  {
    name = "intellij-gradle-jdk23";
    targetPkgs = pkgs_: [
      pkgs_.jetbrains.idea-ultimate
      jdk
      gradle
      ktfmt
      go-task
      pre-commit
    ];
  }
).env
