# word_light_document_builder
REST service handling the document building logic for the Word light API. Uses Java 17 with Spring Boot.

# Run 
### Locally
```docker-compose -f docker-compose.local.yml up``` <br>
Call this inside project root folder of repository inside dev or stage branch. <br>

### Dockerhub
```docker-compose up``` <br>
Call this with .env file in same folder as docker-compose.yml. <br>

### The whole thing
```docker-compose -f docker-compose.all.yml up``` <br>
Call this with .env file in same folder as docker-compose.yml. <br>
Will start the whole microservice including frontend etc. using images from Dockerhub. No further configuration needed. Access api at https://localhost

# More documentation
Run api, then visit http://localhost:4001 or https://localhost:4001 (if run on stage branch)