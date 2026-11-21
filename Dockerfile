# renovate: datasource=docker depName=denoland/deno
ARG DENO_VERSION=2.9.8

FROM gradle:jdk25 AS build
ARG VERSION
COPY . /app/
RUN cd /app && gradle --build-cache assemble

FROM denoland/deno:bin-${DENO_VERSION} AS deno

FROM eclipse-temurin:25 AS yt-dlp
ARG TARGETARCH
# renovate: datasource=github-releases depName=yt-dlp/yt-dlp
ARG YT_DLP_VERSION=2026.10.26
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && case "${TARGETARCH}" in \
      arm64) YT_DLP_ASSET=yt-dlp_linux_aarch64 ;; \
      *) YT_DLP_ASSET=yt-dlp_linux ;; \
    esac \
    && curl -fsSL -o /yt-dlp \
      "https://github.com/yt-dlp/yt-dlp/releases/download/${YT_DLP_VERSION}/${YT_DLP_ASSET}" \
    && chmod +x /yt-dlp

FROM eclipse-temurin:25
ARG VERSION
ENV KOBOT_VERSION=${VERSION}
# yt-dlp needs a JavaScript runtime to download from YouTube
COPY --from=deno /deno /usr/local/bin/deno
COPY --from=yt-dlp /yt-dlp /usr/local/bin/yt-dlp
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/KotlinBot.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/KotlinBot.jar"]
