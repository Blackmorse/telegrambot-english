import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.jetbrains.kotlin.jvm") version "2.0.20-Beta1"
	id("application")
	id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.blackmorse"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
	mavenCentral()
}

val akkaVersion = "2.6.19"

dependencies {
	implementation("org.telegram:telegrambots:6.0.1")
	implementation("com.typesafe.akka:akka-persistence-typed_2.13:$akkaVersion")
	implementation("ch.qos.logback:logback-classic:1.2.11")
	implementation("com.typesafe.akka:akka-serialization-jackson_2.13:$akkaVersion")

	implementation("com.typesafe.akka:akka-coordination_2.13:$akkaVersion")
	implementation("com.typesafe.akka:akka-cluster_2.13:$akkaVersion")
	implementation("com.typesafe.akka:akka-cluster-tools_2.13:$akkaVersion")

	implementation("com.esri.geometry:esri-geometry-api:2.2.4")
	implementation("org.apache.tinkerpop:gremlin-core:3.6.0")
	implementation("org.apache.tinkerpop:tinkergraph-gremlin:3.6.0")
	implementation("org.apache.tinkerpop:gremlin-driver:3.6.0")


	implementation("com.github.dnvriend:akka-persistence-jdbc_2.13:3.5.3")

	implementation("com.typesafe.akka:akka-persistence-query_2.13:$akkaVersion")
	implementation("com.typesafe.slick:slick_2.13:3.5.1")
	implementation("com.typesafe.slick:slick-hikaricp_2.13:3.5.1")

	implementation("org.fusesource.leveldbjni:leveldbjni-all:1.8")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
    mainClass.set("com.blackmorse.telegrambotenglish.EnglishBotKt")
}

project.setProperty("mainClassName", "com.blackmorse.telegrambotenglish.EnglishBotKt")

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
	val newTransformer = com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer()
	newTransformer.resource = "reference.conf"
	transformers.add(newTransformer)
}

tasks.jar {
	manifest.attributes["Main-Class"] = "com.blackmorse.telegrambotenglish.EnglishBotKt"
}