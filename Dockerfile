# Java 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

FROM openjdk:17-jdk-slim
COPY *.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]