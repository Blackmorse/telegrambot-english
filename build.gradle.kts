import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.10"
        id("application")
}

group = "com.blackmorse"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.telegram:telegrambots:6.0.1")
	implementation("com.typesafe.akka:akka-persistence-typed_2.13:2.6.19")
	implementation("ch.qos.logback:logback-classic:1.2.11")
	implementation("com.typesafe.akka:akka-serialization-jackson_2.13:2.6.19")
	implementation("com.typesafe.akka:akka-persistence-cassandra_2.13:1.0.5")

	implementation("com.typesafe.akka:akka-coordination_2.13:2.6.19")
	implementation("com.typesafe.akka:akka-cluster_2.13:2.6.19")
	implementation("com.typesafe.akka:akka-cluster-tools_2.13:2.6.19")

	implementation("com.esri.geometry:esri-geometry-api:2.2.4")
	implementation("org.apache.tinkerpop:gremlin-core:3.6.0")
	implementation("org.apache.tinkerpop:tinkergraph-gremlin:3.6.0")
	implementation("org.apache.tinkerpop:gremlin-driver:3.6.0")




	implementation("org.fusesource.leveldbjni:leveldbjni-all:1.8")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
    mainClass.set("com.blackmorse.telegrambotenglish.EnglishBotKt")
}

