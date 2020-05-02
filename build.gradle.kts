import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.jaanerikpihel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

buildscript {
	repositories {
		maven {
			url = uri("https://plugins.gradle.org/m2/")
		}
	}
	dependencies {
		classpath("com.diffplug.spotless:spotless-plugin-gradle:3.28.1")
	}
}

apply(plugin = "com.diffplug.gradle.spotless")

spotless {
	format("misc") {
		target("**/*.gradle", "**/*.md", "**/.gitignore")

		trimTrailingWhitespace()
		indentWithTabs() // or spaces. Takes an integer argument if you don't like 4
		endWithNewline()
	}
	format("cpp") {
		target("**/*.hpp", "**/*.cpp")

		replace("Not enough space after if", "if(", "if (")
		replaceRegex("Too much space after if", "if +\\(", "if (")

	}
}

plugins {
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
	id("com.diffplug.gradle.spotless") version "3.28.1"
	kotlin("jvm") version "1.3.71"
	kotlin("plugin.spring") version "1.3.71"
}

repositories {
	mavenCentral()
}

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework:spring-messaging")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.webjars:webjars-locator-core")
	implementation("org.webjars:sockjs-client:1.0.2")
	implementation("org.webjars:stomp-websocket:2.3.3")
	implementation("org.webjars:bootstrap:3.3.7")
	implementation("org.webjars:jquery:3.1.1-1")
	implementation("com.google.code.gson:gson:2.8.5")
	implementation("org.slf4j:slf4j-api:1.7.5")
	implementation("io.github.microutils:kotlin-logging:1.7.9")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
