FROM maven:3.9.9-eclipse-temurin-21-alpine
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
CMD ["mvn", "test"]