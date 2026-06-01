plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example.dynamodb.theory"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("reflect"))
    implementation(platform("software.amazon.awssdk:bom:2.31.78"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("software.amazon.awssdk:dynamodb")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
