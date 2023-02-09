
# Actuator endpoints for Kafka

[![](https://maven-badges.herokuapp.com/maven-central/io.github.leofuso/actuator-kafka/badge.svg?style=flat)](https://mvnrepository.com/artifact/io.github.leofuso/actuator-kafka)
[![](https://jitpack.io/v/LeoFuso/actuator-kafka.svg)](https://jitpack.io/#LeoFuso/actuator-kafka)

This is a simple project built on top of Spring Boot's Actuator and [Spring Boot for Apache Kafka project](https://spring.io/projects/spring-kafka/)
that aims to provide a simple way of getting health-check functionality for your Kafka Spring Boot application.

It was inspired by existent functionalities present in the [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) project.

## Dependency

Maven Central
```maven
<dependency>
  <groupId>io.github.leofuso</groupId>
  <artifactId>actuator-kafka</artifactId>
  <version>v3.0.2.0.RELEASE</version>
</dependency>
``` 

JitPack
```maven
<dependency>
  <groupId>com.github.leofuso</groupId>
  <artifactId>actuator-kafka</artifactId>
  <version>v3.0.2.0.RELEASE</version>
</dependency>
``` 

The version indicates the compatibility with the Spring Boot. In other worlds, I'll try to keep it up to date with other
Spring Boot versions, e.g, the `v3.0.x.y.RELEASE` should be compatible with the Spring Boot `3.0.x` version and so on.

## Usage

This package assumes a `KafkaAdmin` bean available on the classpath. Simply import it, and you can assign it to a health 
group as `kafka`, just like any other health-check dependency. 

```txt
management.endpoint.health.group.readiness.include=ping, kafka
```

### Available properties

| name                        | type     | description                                                                  |
|-----------------------------|----------|------------------------------------------------------------------------------|
| response-timeout            | Duration | Time to wait for a response from the cluster description operation.          |
| consider-replication-factor | boolean  | Use the replication factor of the broker to validate if it has enough nodes. |

