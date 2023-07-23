FROM gradle:jdk17-alpine

WORKDIR /app/backend/builder

COPY . /app/backend/builder/

ENTRYPOINT gradle bootRun