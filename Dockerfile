FROM armv7/armhf-java8

# Install maven
RUN apt-get update
RUN apt-get install -y maven
RUN apt-get install -y -f

# Create directory
RUN mkdir -p /usr/src/autocourt
WORKDIR /usr/src/autocourt

# # Install ChromeDriver
# ARG driver_url
# RUN wget ${driver_url} -O chromedriver.deb
# RUN dpkg -i chromedriver.deb

# Adding source, compile and package into a fat jar
ADD src src
ADD data data

# Prepare by downloading dependencies
ADD pom.xml pom.xml

RUN mvn package -DskipTests

EXPOSE 4567 9222 4444

RUN ls target/
CMD java -jar target/AutoCourt-1.0.jar
