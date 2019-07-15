import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.2.41"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion

	id("org.springframework.boot") version "2.1.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.7.RELEASE"
	id("com.google.cloud.tools.jib") version "1.3.0"
	id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
	jacoco
}

group = "com.ampnet"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude("junit")
	}
	testImplementation("org.junit.jupiter:junit-jupiter:5.5.0")
	testImplementation("org.subethamail:subethasmtp:3.1.7")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.test {
	useJUnitPlatform()
}

jib {
	val dockerUsername: String = System.getenv("DOCKER_USERNAME") ?: "DOCKER_USERNAME"
	val dockerPassword: String = System.getenv("DOCKER_PASSWORD") ?: "DOCKER_PASSWORD"

	to {
		image = "ampnet/mail-service:$version"
		auth {
			username = dockerUsername
			password = dockerPassword
		}
		tags = setOf("latest")
	}
	container {
		useCurrentTimestamp = true
	}
}

jacoco.toolVersion = "0.8.4"
tasks.jacocoTestReport {
	reports {
		xml.isEnabled = false
		csv.isEnabled = false
		html.destination = file("$buildDir/reports/jacoco/html")
	}
	sourceDirectories.setFrom(listOf(file("${project.projectDir}/src/main/kotlin")))
	classDirectories.setFrom(fileTree("$buildDir/classes/kotlin/main").apply {
		exclude("**/model/**", "**/pojo/**")
	})
	dependsOn(tasks.test)
}
tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.7".toBigDecimal()
			}
		}
	}
	mustRunAfter(tasks.jacocoTestReport)
}
