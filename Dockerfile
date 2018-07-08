FROM anapsix/alpine-java:8_server-jre

ADD build/libs/Hilo*.jar /app/Hilo.jar

CMD ["/bin/sh", "-c", "java -jar /app/Hilo.jar"]