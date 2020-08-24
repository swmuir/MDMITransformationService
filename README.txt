# MDMI Transformation Service

## Running A Release build

`docker run -it --rm -p 3000:8080 -v maps:/maps logicahealth/mdmi-transformation-service:latest`

## Create a New Docker Image

`docker build logicahealth/mdmi-transformation-service:latest`

docker build -t mdmi-transformation-service .

docker run -d -p 5000:8080 -v <<Maps Folder Volume>>:/maps mdmi-transformation-service

for instance

docker run -d -p 5000:8080 -v `pwd`/src/test/resources/testmaps:/maps mdmi-transformation-service

http://localhost:5000/v3/api-docs

http://localhost:5000/swagger-ui.html

 