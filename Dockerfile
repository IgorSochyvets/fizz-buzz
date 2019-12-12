FROM openjdk:8
ADD java-web-app/target/java-web-app.jar java-web-app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "java-web-app.jar" ]
