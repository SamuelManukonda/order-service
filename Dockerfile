FROM  amazoncorretto:21
WORKDIR /app
COPY target/order-service-0.0.1-SNAPSHOT.jar /app/order-service.jar
EXPOSE 8080
CMD ["java", "-jar", "order-service.jar"]