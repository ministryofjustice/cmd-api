plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.6"
    kotlin("plugin.spring") version "1.4.10"
    kotlin("plugin.jpa") version "1.4.10"
}

repositories {
    maven("https://dl.bintray.com/gov-uk-notify/maven")
}

configurations {
    implementation { exclude(mapOf("module" to "tomcat-jdbc")) }
}

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.zaxxer:HikariCP:3.4.5")
    runtimeOnly("org.flywaydb:flyway-core:6.5.6")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("org.springframework:spring-webflux")
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.nimbusds:nimbus-jose-jwt:8.19")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("uk.gov.service.notify:notifications-java-client:3.17.0-RELEASE")


    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.19.0")
    testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
    testImplementation("com.ninja-squad:springmockk:2.0.3")
    testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
