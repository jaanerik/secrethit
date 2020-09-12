import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

group = "com.jaanerikpihel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
	kotlin("jvm") version "1.3.70"
	id("application")
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
	id("com.diffplug.gradle.spotless") version "3.28.1"
	id("com.moowork.node") version "1.3.1"
	id("com.github.node-gradle.node") version "2.2.3"
}

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework:spring-messaging")
	implementation("org.springframework.security:spring-security-config:5.3.2.RELEASE")
	implementation("org.springframework.security:spring-security-web:5.3.2.RELEASE")

	implementation("com.google.code.gson:gson:2.8.5")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.webjars:webjars-locator-core")
	implementation("org.webjars:stomp-websocket:2.3.3")
	implementation("org.webjars:bootstrap:3.3.7")
	implementation("org.slf4j:slf4j-api:1.7.5")
	implementation("io.github.microutils:kotlin-logging:1.7.9")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}