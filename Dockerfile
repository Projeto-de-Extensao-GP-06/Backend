# Estagio 1: Build com Maven e JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Instala Maven
RUN apk add --no-cache maven

# Copia o pom.xml primeiro para aproveitar cache do Docker
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Baixa as dependencias (camada de cache separada)
RUN mvn dependency:go-offline -q

# Copia o codigo-fonte e compila gerando o JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Estagio 2: Runtime JRE 21 ultraleve
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
