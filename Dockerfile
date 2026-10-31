# El archivo Dockerfile define el proceso de construcción y despliegue de la aplicación.
# Utiliza una estrategia de dos etapas: En la primera compila el proyecto Spring Boot con Maven y genera
# el archivo JAR; en la segunda crea una imagen ligera basada en Java Runtime Environment, copia el JAR generado
# y ejecuta la aplicación en el puerto 8080. Esto optimiza el tamaño de la imagen y facilita el despliegue en
# plataformas como Render.

# Etapa 1: compilar el proyecto con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: ejecutar solo el .jar generado
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]