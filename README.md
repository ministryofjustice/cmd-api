# Check my diary
[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fcmd-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/cmd-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/cmd-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/cmd-api)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/cmd-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://cmd-api-preprod.prison.service.justice.gov.uk/swagger-ui/index.html)

## Check my diary backend api service

CMD API queries CSR API and turns the raw shift data into calendar days.

It applies business logic such as identifying whether shifts are day or night shifts, which activities are overtime and calculating the shift start, finish and duration. It then groups the shift data into ‘days’ (so one night shift spans 2 days)
This codebase also includes Spring scheduling with Shedlock which is used as a cluster aware cron to schedule checking for modified shifts via CSR-API. This data is then sent to users via Gov Notify and stored in a table and made available over rest.

Continuous Integration:
https://app.circleci.com/pipelines/github/ministryofjustice/cmd-api

### Prerequisites
- Java JDK 17+
- An editor/IDE
- Gradle
- Docker
- OAuth (running in a container)
- OAuth security

In order to run the service locally, Nomis OAuth Service is required. This can be run locally using the docker-compose.yml file which will pull down the latest version. From the command line run:
```bash
 docker-compose up
``` 
### Build service and run tests
This service is built using Gradle. In order to build the project from the command line and run the tests, use:
```bash
./gradlew clean build
```  
The created JAR file will be named "csr-api-<yyyy-mm-dd>.jar", using the date that the build takes place in the format yyyy-mm-dd.

### Start the application with H2 database
The configuration can be changed for the api to use an in-memory H2 database by using the spring boot profile dev. On the command line run:
```bash
SPRING_PROFILES_ACTIVE=dev 
java -jar build/libs/cmd-api-<yyyy-mm-dd>.jar  
```
### Additional configuration
The application is configurable with conventional Spring parameters.

The Spring documentation can be found here: https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

#### Default port
By default the application starts on port '8080'. To override, set server.port, e.g. 
```bash
SERVER_PORT=8099 java -jar build/libs/cmd-api-<yyyy-mm-dd>.jar
```

### Documentation
The generated documentation for the api can be viewed at http://localhost:8081/swagger-ui.html

### Health
- /ping: will respond pong to all requests. This should be used by dependent systems to check connectivity to
cmd-api, rather than calling the /health endpoint.
- /health: provides information about the application health and its dependencies. This should only be used
by cmd-api health monitoring (e.g. pager duty) and not other systems who wish to find out the
state of cmd-api.
- /info: provides information about the version of deployed application.
#### Health and info Endpoints (curl)
Application info
curl -X GET http://localhost:8081/info  
Application health
curl -X GET http://localhost:8081/health  
Application ping
curl -X GET http://localhost:8081/ping  
### Using the api
#### Authentication using OAuth
In order to make queries to the api, first a client credential access token must be generated from OAuth. Send a POST request to the OAuth container with the client name:secret, encoded in base64 (check-my-diary:clientsecret)

Firstly, to encode in base64: 
```bash
echo -n 'check-my-diary:clientsecret' | openssl base64
```
to output Y2hlY2stbXktZGlhcnk6Y2xpZW50c2VjcmV0.

Then, POST the request for the access token from OAuth, using the encoded secret as authorisation:
```bash
curl --location --request POST 'http://localhost:9090/auth/oauth/token?grant_type=client_credentials' \
--header 'Authorization: Basic Y2hlY2stbXktZGlhcnk6Y2xpZW50c2VjcmV0'
```
