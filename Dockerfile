FROM tomcat:9-jdk11-openjdk

MAINTAINER mail@ingvord.ru

ARG REST_SERVER_VERSION

ENV REST_SERVER_VERSION=$REST_SERVER_VERSION

RUN apt-get update && apt-get install -y ssl-cert libtcnative-1

COPY target/${REST_SERVER_VERSION}.war /usr/local/tomcat/webapps/tango.war

COPY docker/tomcat-users.xml \
     docker/server.xml \
     /usr/local/tomcat/conf/