FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY app ./app
COPY common ./common
COPY auth ./auth
COPY order ./order
COPY payment ./payment
COPY inventory ./inventory
RUN gradle :app:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-Xmx320m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
