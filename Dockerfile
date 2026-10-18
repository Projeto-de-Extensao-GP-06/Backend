# Estágio único: apenas executar o JAR
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR já compilado
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
