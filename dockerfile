# Usamos imagen oficial de Java 17
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el JAR construido por Maven
COPY target/final-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto de la app
EXPOSE 8080

# Arranque del JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
