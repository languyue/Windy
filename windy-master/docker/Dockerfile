FROM openjdk:11

RUN mkdir -p /opt/windy-master

COPY ./*.jar /opt/windy-master/

COPY start.sh /opt/windy-master/

WORKDIR /opt/windy-master

CMD ["sh","start.sh"]

EXPOSE 9888