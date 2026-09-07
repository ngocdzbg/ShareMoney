FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/Share-Money-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 6969

ENTRYPOINT ["java", "-jar", "app.jar"]