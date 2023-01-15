import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.0"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.openapi.generator") version "6.2.1"
	kotlin("jvm") version "1.7.21"
	kotlin("plugin.spring") version "1.7.21"
}

group = "com.nikitaevg"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter:3.0.0")
	implementation("org.springframework.boot:spring-boot-starter-web:3.0.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator:3.0.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.swagger.core.v3:swagger-models:2.2.7")
	implementation("org.springframework:spring-web:6.0.2")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
	implementation("org.openapitools:jackson-databind-nullable:0.2.4")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("io.swagger.core.v3:swagger-annotations:2.2.6")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	implementation("javax.servlet:javax.servlet-api:4.0.1")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test:3.0.0")
	testImplementation("org.mockito:mockito-inline")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.0.0")
}

val spec = "$rootDir/schema/api.yaml"
val generatedSourcesDir = "$buildDir/generated/openapi"

openApiGenerate {
	generatorName.set("spring")

	inputSpec.set(spec)
	outputDir.set(generatedSourcesDir)

	apiPackage.set("com.nikitaevg.datacollector.api")
	invokerPackage.set("com.nikitaevg.datacollector.invoker")
	modelPackage.set("com.nikitaevg.datacollector.model")

	configOptions.set(mapOf(
			"dateLibrary" to "java8",
			"interfaceOnly" to "true",
		))
}

openApiValidate {
	inputSpec.set(spec)
	recommend.set(true)
}

sourceSets {
	getByName("main") {
		kotlin {
			srcDir("$generatedSourcesDir/src/main/kotlin")
		}
		java {
			srcDir("$generatedSourcesDir/src/main/java")
		}
	}
}

tasks {
	val openApiGenerate by getting

	val compileKotlin by getting {
		dependsOn(openApiGenerate)
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
	enabled = false
}
