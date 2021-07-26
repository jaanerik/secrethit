java.sourceCompatibility = JavaVersion.VERSION_1_8

plugins {
	kotlin("jvm") version "1.3.70"
	id("application")
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	//id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
	//id("com.diffplug.gradle.spotless") version "3.28.1"
	//id("com.moowork.node") version "1.3.1"
	id("com.github.node-gradle.node") version "3.1.0"
}

application {
	mainClassName = "com.jaanerikpihel.secrethit.SecrethitApplicationKt"
}

repositories {
	mavenCentral()
	jcenter()
}

val jar: Jar by tasks
val bootJar : org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
configurations {
	listOf(apiElements, runtimeElements).forEach { ndop ->
		ndop.get().outgoing.artifacts.removeIf { it.buildDependencies.getDependencies(null).contains(jar) }
		ndop.get().outgoing.artifact(bootJar)
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework:spring-messaging")
	implementation("org.springframework.security:spring-security-config:5.3.2.RELEASE")
	implementation("org.springframework.security:spring-security-web:5.3.2.RELEASE")

	implementation("com.google.code.gson:gson:2.8.5")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
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

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
	sourceCompatibility = JavaVersion.VERSION_1_8.toString()
	targetCompatibility = JavaVersion.VERSION_1_8.toString()

	kotlinOptions {
		jvmTarget = "1.8"
		apiVersion = "1.4"
		languageVersion = "1.4"
	}
}

//task installExpress(type: NpmTask) {
//	// install the express package only
//	args = ['install', 'express', '--save-dev']
//}
//

node {
	version.set("14.17.3")
	npmVersion.set("")
	yarnVersion.set("")
	npmInstallCommand.set("install")
	distBaseUrl.set("https://nodejs.org/dist")
	download.set(true)
	workDir.set(file("${project.projectDir}/src/main/webapp/.cache/nodejs"))
	npmWorkDir.set(file("${project.projectDir}/src/main/webapp/.cache/npm"))
	yarnWorkDir.set(file("${project.projectDir}/src/main/webapp/.cache/yarn"))
	nodeProjectDir.set(file("${project.projectDir}/src/main/webapp"))
	//nodeModulesDir.set(file("${project.projectDir}/src/main/webapp"))
}

//tasks.npmInstall {
//	nodeModulesOutputFilter {
//		exclude("notExistingFile")
//	}
//}
//
//tasks.yarn {
//	nodeModulesOutputFilter {
//		exclude("notExistingFile")
//	}
//}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("appNpmInstall") {
	description = "Installs all dependencies from package.json"
    workingDir.set(file("${project.projectDir}/src/main/webapp"))
    args.set(listOf("install"))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("appNpmBuild") {
	dependsOn("appNpmInstall")
	description = "Builds project"
	workingDir.set(file("${project.projectDir}/src/main/webapp"))
	args.set(listOf("run", "build"))
}

tasks.register<Copy>("copyWebApp") {
	dependsOn("appNpmBuild")
	description = "Copies built project to where it will be served"
	from("src/main/webapp/build")
	into("build/resources/main/static/.")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}
