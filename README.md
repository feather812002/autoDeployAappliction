# autoDeployAappliction

## 1. Build the project
 - Install Gradle 7.5 or higher
 - Install Java 1.8


```
gradle jar
```
The jar file will be created in the build/libs directory.
## 2. Run the project

```
java -jar build/libs/autoDeployAappliction.jar  <adminPassword> <sourceNtfFileName> <targetUpdateNsfFilePath> <targetUpdateServerName>
```
adminPassword: the password of the admin user
sourceNtfFileName: the path to the source nsf/ntf file
targetUpdateNsfFilePath: the path to the target update nsf file
targetUpdateServerName: the name of the target update server

sourceNtfFileName can't same with targetUpdateNsfFilePath.
