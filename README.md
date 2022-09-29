
# ulozto-remote-downloader

Simple tool allowing user to download files from uluzto.net service to remote machine.


## Installation

Install project with maven

```bash
    mvn clean install
```
This will produce jar file within docker folder.


## Run standalone jar with


```bash
  java -jar ulozto-remote-downloader-1.0-SNAPSHOT.jar --dpath=<download_path>
```

Where <download_path> is target folder for the downloads.

Launch user interface at:
```bash
  http://localhost:8081/ulozto/
```

## Run as docker container

Create Dockerfile with following content:
```bash
FROM adoptopenjdk/openjdk11:jre-11.0.9.1_1-alpine@sha256:b6ab039066382d39cfc843914ef1fc624aa60e2a16ede433509ccadd6d995b1f

COPY ulozto-remote-downloader-1.0-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build container with following
```bash
docker build -t spring-boot-app:latest .
```

Run container with following
```bash
docker run --rm -d -p 8081:8081 -v=<system_share>:/download --name ulozto-remote-downloader ulozto-remote-downloader --dpath=/download
```
Where <system_share> is local download path