target "docker-metadata-action" {}

group "images" {
  targets = ["kobot"]
}

target "kobot" {
  dockerfile = "./Dockerfile"
  labels = {
    "org.opencontainers.image.source" = "https://github.com/opentoucan/kobot"
  }
}

target "release" {
  inherits = ["docker-metadata-action"]
  platforms = [
    "linux/amd64",
    "linux/arm64"
  ]
}

target "kobot-release" {
  inherits = ["kobot", "release"]
}

target "kobot-local" {
  inherits = ["kobot"]
  tags = ["localhost/kobot:latest"]
  args = {
    VERSION = "local"
  }
}
