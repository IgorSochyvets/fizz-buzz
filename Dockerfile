FROM adoptopenjdk/openjdk13
ADD target/fizz-buzz.jar fizz-buzz.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "fizz-buzz.jar" ]
