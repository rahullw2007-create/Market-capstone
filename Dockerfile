FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM tomcat:9.0-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/target/zenith-bazaar.war /usr/local/tomcat/webapps/ROOT.war

COPY docker/start-rahulmart.sh /usr/local/bin/start-rahulmart.sh

RUN chmod +x /usr/local/bin/start-rahulmart.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/start-rahulmart.sh"]
