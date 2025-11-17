from gradle:8.14.0-jdk21 as builder

workdir /app

copy . .

run ./gradlew build -x test

from alpine:3.22.2

run apk add --no-cache openjdk21-jre=21.0.9_p10-r0

run addgroup -S appgroup && adduser -S appuser -G appgroup
user appuser

workdir /app

copy --from=builder /app/build/libs/*.jar app.jar

expose 8083

entrypoint ["java", "-jar", "app.jar"]