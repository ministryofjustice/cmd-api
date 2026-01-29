plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.3"
  kotlin("plugin.spring") version "2.3.0"
  kotlin("plugin.jpa") version "2.3.0"
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

  runtimeOnly("com.h2database:h2:2.4.240")
  runtimeOnly("org.postgresql:postgresql:42.7.9")
  runtimeOnly("com.zaxxer:HikariCP")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.0.0")
  implementation("org.springframework.boot:spring-boot-starter-restclient")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine")

  implementation("net.javacrumbs.shedlock:shedlock-spring:7.6.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:7.6.0")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("uk.gov.service.notify:notifications-java-client:6.0.0-RELEASE")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.0.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.boot:spring-boot-resttestclient")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xwhen-guards", "-Xannotation-default-target=param-property")
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
}
