# oopt-murmansk

### Env

```shell
➜  oopt-murmansk git:(master) ✗ java -version
openjdk version "17.0.6" 2023-01-17
OpenJDK Runtime Environment Temurin-17.0.6+10 (build 17.0.6+10)
OpenJDK 64-Bit Server VM Temurin-17.0.6+10 (build 17.0.6+10, mixed mode)

➜  oopt-murmansk git:(master) ✗ mvn -v
Apache Maven 3.8.3 (ff8e977a158738155dc465c6a97ffaf31982d739)
Maven home: /Users/solairerove/.sdkman/candidates/maven/current
Java version: 17.0.6, vendor: Eclipse Adoptium, runtime: /Users/solairerove/.sdkman/candidates/java/17.0.6-tem
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "15.2", arch: "aarch64", family: "mac"
```

### How to run

```shell
mvn clean install -DskipTests=true && java -jar target/oopt-murmansk-0.0.1-SNAPSHOT.jar
```

### How to create .exe
```shell
mvn clean package
  
jpackage \
  --input target \
  --name oopt \
  --main-jar oopt-murmansk-0.0.1-SNAPSHOT.jar \
  --type app-image \
  --dest dist \
  --java-options "-Dspring.output.ansi.enabled=ALWAYS"
  
./dist/oopt.app/Contents/MacOS/oopt
```