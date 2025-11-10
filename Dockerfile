# Java 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 빌드된 JAR 파일을 컨테이너 내부로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/

# 컨테이너가 실행될 때 JAR 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]