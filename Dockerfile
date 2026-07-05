#stage 1
FROM maven:3.9-eclipse-temurin-21 AS Build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#stage 2
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY --from=Build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]