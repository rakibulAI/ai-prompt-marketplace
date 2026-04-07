

# Build Stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Maven wrapper + pom copy করে dependencies cache করি (build faster)
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Source code copy করে build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime Stage (ছোট ও ফাস্ট)
FROM eclipse-temurin:21-jre
WORKDIR /app

# সঠিক JAR কপি করছি
COPY --from=build /app/target/ai-prompt-marketplace-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Render.com এর জন্য PORT variable ব্যবহার
CMD ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
