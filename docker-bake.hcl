target "docker-metadata-action" {}

target "image" {
  inherits = ["docker-metadata-action"]
  tags = [
    "kobot:latest"
  ]
}

target "image-local" {
  inherits = ["image"]
  output = ["type=docker"]
}

target "release" {
  inherits = ["docker-metadata-action"]
  platforms = [
    "linux/amd64",
    "linux/arm64",
  ]
}
