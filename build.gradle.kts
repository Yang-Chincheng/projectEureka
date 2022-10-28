import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    antlr
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

//java {
//    sourceCompatibility = JavaVersion.VERSION_11
//    targetCompatibility = JavaVersion.VERSION_11
//}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    antlr("org.antlr:antlr4:4.11.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-package", "org.kadf.app.eureka")
    outputDirectory = file("$outputDirectory/org/kadf/app/eureka")
}

application {
    mainClass.set("org.kadf.app.eureka.EurekacKt")
}