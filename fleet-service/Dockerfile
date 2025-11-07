# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY fleet-service/pom.xml ./fleet-service/
COPY fleet-service/src ./fleet-service/src
WORKDIR /app/fleet-service
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/fleet-service/target/*-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev-docker

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
