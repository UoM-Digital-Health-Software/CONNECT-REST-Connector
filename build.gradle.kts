import org.radarbase.gradle.plugin.radarKotlin

plugins {
    id("org.radarbase.radar-root-project") version Versions.radarCommons
    id("org.radarbase.radar-dependency-management") version Versions.radarCommons
    id("org.radarbase.radar-kotlin") version Versions.radarCommons apply false
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.

    maven {
        url = uri("https://maven.pkg.github.com/UoM-Digital-Health-Software/CONNECT-RADAR-Schemas")
        credentials {
            username =  "jindrich.gorner@manchester.ac.uk"
            password =  System.getenv("key_package")
        }
    }
    mavenCentral()
}

description = "Kafka connector for REST API sources"

radarRootProject {
    projectVersion.set(Versions.project)
    gradleVersion.set(Versions.wrapper)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")

    radarKotlin {
        javaVersion.set(Versions.java)
        kotlinVersion.set(Versions.kotlin)
        slf4jVersion.set(Versions.slf4j)
        log4j2Version.set(Versions.log4j2)
        junitVersion.set(Versions.junit)
    }
}
