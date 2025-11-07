# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar y compilar distance-client primero
COPY distance-client/pom.xml ./distance-client/
COPY distance-client/src ./distance-client/src
RUN cd distance-client && mvn -q install -DskipTests

# Copiar y compilar logistics-service
COPY logistics-service/pom.xml ./logistics-service/
COPY logistics-service/src ./logistics-service/src
WORKDIR /app/logistics-service
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/logistics-service/target/*-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev-docker

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
