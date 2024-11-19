plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.9"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"
  idea
}

repositories {
  maven("https://dl.bintray.com/gov-uk-notify/maven")
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable",
  )
}

dependencies {

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2:2.3.232")
  runtimeOnly("org.postgresql:postgresql:42.7.4")
  runtimeOnly("com.zaxxer:HikariCP")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.8")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.16.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.16.0")

  implementation("org.springframework:spring-webflux")
  implementation("org.springframework.boot:spring-boot-starter-reactor-netty")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("uk.gov.service.notify:notifications-java-client:5.2.1-RELEASE")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.8")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.0.0")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("org.wiremock:wiremock-standalone:3.9.2")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
