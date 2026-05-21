FROM eclipse-temurin:21-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

ENV GRADLE_USER_HOME=/gradle-cache \
    NODE_EXECUTABLE=node \
    VIEWER_PORT=8087 \
    VIEWER_BIND_ADDRESS=0.0.0.0 \
    VIEWER_REPLAY_DIR=/data/replays

COPY . .

RUN chmod +x ./gradlew ./scripts/sim-viewer.sh

EXPOSE 8087

CMD ["sh", "./scripts/sim-viewer.sh", "--no-open", "--world-dir", "/workspace/run/saves", "--replay-dir", "/data/replays"]
