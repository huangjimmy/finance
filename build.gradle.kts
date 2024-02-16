import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.2"
	id("org.liquibase.gradle") version "2.2.1"
	id("io.spring.dependency-management") version "1.1.4"
	id("dev.hilla") version "2.5.5"
	id("org.jooq.jooq-codegen-gradle") version "3.19.3"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
}

group = "sh.huang"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

extra["hillaVersion"] = "2.5.5"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-graphql:3.2.2")
	implementation("org.springframework.boot:spring-boot-starter-jooq:3.2.2")
	implementation("org.springframework.boot:spring-boot-starter-web:3.2.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("dev.hilla:hilla-react-spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.liquibase:liquibase-core")
	implementation("com.google.code.gson:gson")
	implementation("io.ktor:ktor-client-core:2.3.8")
	implementation("io.ktor:ktor-client-json:2.3.8")
	implementation("io.ktor:ktor-client-serialization:2.3.8")
	implementation("io.ktor:ktor-client-apache5:2.3.8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
	implementation("org.jooq:jooq:3.19.3")
	runtimeOnly("org.hsqldb:hsqldb")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework:spring-webflux")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("io.mockk:mockk:1.13.9")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
	testImplementation("io.ktor:ktor-client-mock:2.3.8")

	liquibaseRuntime("org.liquibase:liquibase-core")
	liquibaseRuntime("info.picocli:picocli:4.6.1")
	liquibaseRuntime("org.hsqldb:hsqldb")

	jooqCodegen("org.hsqldb:hsqldb")
}

dependencyManagement {
	imports {
		mavenBom("dev.hilla:hilla-bom:${property("hillaVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

liquibase {
	activities {
		create("main") {
			arguments = mapOf(
					"changelog-file" to "db/changelog/changelog.sql",
					"url" to "jdbc:hsqldb:file:./data/finance",
					"username" to "sa",
					"searchPath" to "src/main/resources/",
			)
		}
	}
}

jooq {
	configuration {
		jdbc {
			url = "jdbc:hsqldb:file:./data/finance"
			user = "sa"
			password = ""
		}
		generator {
			name = "org.jooq.codegen.JavaGenerator"
			database {
				name = "org.jooq.meta.hsqldb.HSQLDBDatabase"
				includes = "Stock_Symbol|Stock_Historical_Price|Stock_Dividends|Stock_Splits"
				withInputSchema("PUBLIC")
			}
			generate {
				withDaos(true)
				withSpringDao(true)
				withSpringAnnotations(true)
				withJooqVersionReference(true)
				withPojos(true)
			}
			target {
				packageName = "sh.huang.finance.generated"
				directory = "src/main/java/"
			}
		}
	}
}
