# TakeHome

This is a Java 17 console application, not using Spring framework to keep it simple.

It uses [Gradle](https://gradle.org/) as dependency manager as well as a build and run tool.

If you don't have Java 17 installed, you can install the package `openjdk-17-jdk` on Linux for example.
That should be enough to run the application. Once installed, you can check it by running:
```
$ java --version
openjdk 17.0.7 2023-04-18
OpenJDK Runtime Environment (build 17.0.7+7-Ubuntu-0ubuntu122.04.2)
OpenJDK 64-Bit Server VM (build 17.0.7+7-Ubuntu-0ubuntu122.04.2, mixed mode, sharing)
```
That's how the output should look like.


To build and run the application, in the project root directory, execute:
```
$ sh gradlew build
$ sh gradlew run
```

### Code details

Where to start looking? `src/main/java/com/homevision/Application.java`

Where to find tests? `src/test/java/com/homevision`
