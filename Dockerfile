FROM java:8 

# Create directory
RUN mkdir -p /usr/src/autocourt
WORKDIR /usr/src/autocourt

# Install maven
RUN apt-get update
RUN apt-get install -y maven
RUN apt-get install -y -f

# # Install ChromeDriver
# ARG driver_url
# RUN wget ${driver_url} -O chromedriver.deb
# RUN dpkg -i chromedriver.deb

# Prepare by downloading dependencies
ADD pom.xml pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src src
ADD data data

RUN ["mvn", "package"]

EXPOSE 4567 9222
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/AutoCourt-1.0.jar"]
