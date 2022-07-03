
# Actuator endpoints for Kafka

[![](https://jitpack.io/v/LeoFuso/actuator-kafka.svg)](https://jitpack.io/#LeoFuso/actuator-kafka)

This is a simple project built on top of Spring Boot's Actuator and [Spring Boot for Apache Kafka project](https://spring.io/projects/spring-kafka/)
that aims to provide a simple way of getting health-check functionality for your Kafka Spring Boot application.

It was inspired by existent functionalities present in the [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) project.

## Dependency
It is available both on JitPack's and on Maven Central.
```maven
<dependency>
  <groupId>io.github.leofuso</groupId>
  <artifactId>autoconfigure-actuator-kafka</artifactId>
  <version>v2.7.0.1-SNAPSHOT</version>
</dependency>
``` 

The version indicates the compatibility with the Spring Boot. In other worlds, I'll try to keep it up to date with other
Spring Boot versions, e.g, the `v2.7.x.x.RELEASE` should be compatible with the Spring Boot `2.7.x` version and so on.

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

