from openjdk:17.0.2-jdk

RUN mkdir /app
COPY . /app
WORKDIR /app
CMD ["./script.sh"]