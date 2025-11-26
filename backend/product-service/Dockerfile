FROM eclipse-temurin:24-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8443

CMD ["java", "-jar", "target/productservice-0.0.1-SNAPSHOT.jar"]
