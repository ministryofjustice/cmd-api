plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.0.3"
  kotlin("plugin.spring") version "1.6.10"
  kotlin("plugin.jpa") version "1.6.10"
  idea
}

repositories {
  maven("https://dl.bintray.com/gov-uk-notify/maven")
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable"
  )
}

dependencies {

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2:2.1.210")
  runtimeOnly("org.postgresql:postgresql:42.3.2")
  runtimeOnly("com.zaxxer:HikariCP")
  runtimeOnly("org.flywaydb:flyway-core:8.5.0")

  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-quartz")
  implementation("com.github.ben-manes.caffeine:caffeine")

  implementation("org.springframework:spring-webflux")
  implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.6")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.6")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("uk.gov.service.notify:notifications-java-client:3.17.2-RELEASE")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.28.0")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("com.ninja-squad:springmockk:3.1.0")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "16"
  }
}
