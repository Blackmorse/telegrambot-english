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
val pekkoVersion = "1.0.1" // Dynamo dep uses 1.0.1

dependencies {
	implementation("org.telegram:telegrambots:6.9.7.1")
	implementation("org.apache.pekko:pekko-persistence-typed_3:$pekkoVersion")

	implementation("ch.qos.logback:logback-classic:1.5.8")
	implementation("org.apache.pekko:pekko-serialization-jackson_3:$pekkoVersion")

	implementation("org.apache.pekko:pekko-persistence-dynamodb_3:1.0.0" )

//	implementation("com.github.dnvriend:akka-persistence-jdbc_2.13:3.5.3")
//
//	implementation("com.typesafe.akka:akka-persistence-query_2.13:$akkaVersion")
//	implementation("com.typesafe.slick:slick_2.13:3.5.1")
//	implementation("com.typesafe.slick:slick-hikaricp_2.13:3.5.1")
//
//	implementation("org.fusesource.leveldbjni:leveldbjni-all:1.8")

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