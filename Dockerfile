FROM maven:3.9-eclipse-temurin-17 AS b
WORKDIR /app
COPY . .
RUN mvn -q package -DskipTests
FROM eclipse-temurin:17-jre
COPY --from=b /app/target/hospital.jar /hospital.jar
CMD ["java","-jar","/hospital.jar"]
