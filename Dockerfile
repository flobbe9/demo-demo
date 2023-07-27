FROM gradle:jdk17-alpine

WORKDIR /app

COPY . /app/

ENTRYPOINT gradle bootRun