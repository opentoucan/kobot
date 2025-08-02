target "docker-metadata-action" {
  tags = []
}

variable "TAG_BASE" {}

target "image-local" {
  output = ["type=docker"]
}

target "image-all" {
  name = "${tgt}"
  matrix = {
    tgt = ["kobot"]
  }
  target = tgt
}

target "kobot" {
  inherits = ["docker-metadata-action"]
  dockerfile = "./Dockerfile"
  tags = [for tag in target.docker-metadata-action.tags : "${TAG_BASE}/kobot:${tag}"]
  platforms = [
    "linux/amd64",
    "linux/arm64"
  ]
}
