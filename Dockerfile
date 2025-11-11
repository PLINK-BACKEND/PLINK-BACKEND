FROM eclipse-temurin:17-jdk-jammy

# JAR 파일 경로 지정
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
