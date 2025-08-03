target "docker-metadata-action" {}

target "kobot" {
  dockerfile = "./Dockerfile"
}

target "release" {
  inherits = ["docker-metadata-action"]
  platforms = [
    "linux/amd64",
    "linux/arm64"
  ]
}

target "release-all" {
  inherits = ["release"]
  name = "${tgt}"
  matrix = {
    tgt = ["kobot"]
  }
  target = tgt
}
