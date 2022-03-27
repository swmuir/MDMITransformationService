# MDMI Transformation Service 
The MDMI Transformation Service is implemented in Java and designed as a service in enterprise-scale environments.  For installing and executing the MDMI Transformation Service, please see the Installation Instructions in the documentation folder in this repository. Instructions for executing transformations using the open source software provided on this site are provided in file MTSDemontration.doc

If in using the transformation service and you have an issue or enhancement, please use [ISSUES](https://github.com/MDMI/MDMITransformationService/issues) tab of this Repository.


docker build -t  mdmiservices/njtransform:0.8.0   .

docker run -d -p 5000:8080 mdmiservices/njtransform

docker pull mdmiservices/njtransform:0.8.0