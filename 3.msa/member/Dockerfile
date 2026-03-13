FROM eclipse-temurin:17-jdk-alpine as stage1
WORKDIR /app
COPY gradle gradle
# 폴더명 폴더명
COPY src src
# 파일copy
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째스테이지(컨테이너) : 이미지 경량화를 위해 스테이지 분리작업진행
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=stage1 /app/build/libs/*.jar ordersystem.jar
ENTRYPOINT [ "java", "-jar", "ordersystem.jar" ]

# ordersystem안에 명령문 실행. Dockerfile이 있고 이경로가 buildContext이기 때문
# 도커이미지 빌드
# 장식
EXPOSE 8080
# docker build -t realview45/myordersystem:latest .
# 도커컨테이너 실행
# docker run --name myspring -d -p 8081:8080 myordersystem:1.0.0
# 도커컨테이너 실행시점에 컨테이너 밖 localhost로 환경변수 수정하여 주입                                            내진짜 로컬을 지칭
# docker run --name myspring -d -p 8081:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/realview45/ordersystem?useSSL=true -e SPRING_REDIS_HOST=host.docker.internal realview45/myordersystem:latest
# docker run --name myspring -d -p 8081:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://rds주소:3306/realview45/ordersystem?useSSL=true -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=test1234 -e SPRING_REDIS_HOST=host.docker.internal realview45/myordersystem:latest
