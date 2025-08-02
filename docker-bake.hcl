target "docker-metadata-action" {
  tags = []
}

variable "TAG_BASE" {}

target "image" {
  inherits = ["docker-metadata-action"]
}

target "image-local" {
  inherits = ["image"]
  output = ["type=docker"]
}

target "image-all" {
  name = "${tgt}"
  matrix = {
    tgt = ["kobot"]
  }
  target = tgt
  platforms = [
    "linux/amd64",
    "linux/arm64"
  ]
}

target "kobot" {
  dockerfile = "./Dockerfile"
  tags = [for tag in target.docker-metadata-action.tags : "${TAG_BASE}/kobot:${tag}"]
}
