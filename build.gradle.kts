import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

fun loadProperties(file: String) {
	val propertiesFile = File(rootProject.projectDir, file)
	if (propertiesFile.exists()) {
		val properties = Properties()
		properties.load(FileInputStream(propertiesFile))
		for( (k, v) in properties.entries){
			extra[k.toString()] = if (v.toString().isNotEmpty()) v else null
		}
	}
}

loadProperties("src/main/resources/application.properties")

var buildProfile = System.getProperty("BUILD_PROFILE", "")
if(buildProfile.isEmpty()) buildProfile = System.getenv("BUILD_PROFILE")
extra["buildProfile"] = buildProfile
when (buildProfile) {
	"local" -> loadProperties("src/main/resources/application-local.properties")
	"prod" -> loadProperties("src/main/resources/application-prod.properties")
	"test" -> loadProperties("src/main/resources/application-test.properties")
	"ci" -> loadProperties("src/main/resources/application-ci.properties")
}

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
	implementation("org.postgresql:postgresql")
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
	liquibaseRuntime("org.postgresql:postgresql")

	jooqCodegen("org.hsqldb:hsqldb")
	jooqCodegen("org.postgresql:postgresql")
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
			 val args = mutableMapOf(
					"changelog-file" to "${property("spring.liquibase.change-log")}",
					"url" to "${property("spring.datasource.url")}",
					"username" to "${property("spring.datasource.username")}",
					"searchPath" to "src/main/resources/",
			)
			property("spring.datasource.password").let { if(it != null) args["password"] = "$it" }
			arguments = args
		}
	}
}

jooq {
	configuration {
		jdbc {
			url = "${property("spring.datasource.url")}"
			user = "${property("spring.datasource.username")}"
			property("spring.datasource.password").let {
				if(it != null) password = "$it"
			}
		}
		generator {
			name = "org.jooq.codegen.JavaGenerator"
			database {
				includes = "Stock_Symbol|Stock_Historical_Price|Stock_Dividends|Stock_Splits"
				withInputSchema("${property("spring.datasource.input.schema")}")
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
