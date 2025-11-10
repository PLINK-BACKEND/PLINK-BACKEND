# Java 17 기반 이미지 사용
FROM eclipse-temurin:17-jdk-jammy

# 빌드된 JAR 파일을 컨테이너 내부로 복사
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
