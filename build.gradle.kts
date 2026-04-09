import org.radarbase.gradle.plugin.radarKotlin

plugins {
    alias(libs.plugins.radar.root.project)
    alias(libs.plugins.radar.dependency.management)
    alias(libs.plugins.radar.kotlin) apply false
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
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")

    // --- Vulnerability fixes start ---
    dependencies {
        plugins.withType<JavaPlugin> {
            constraints {
                add("implementation", rootProject.libs.jackson.bom) {
                    because("Force safe version of Jackson across all modules")
                }
                add("implementation", rootProject.libs.commons.lang3) {
                    because("Force safe version of commons-lang3 across all modules")
                }
            }
        }
    }
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // Substitute the old group/module with drop-in replacement
            substitute(module("org.lz4:lz4-java"))
                .using(module(rootProject.libs.lz4.get().toString()))
                .because("Force safe version of LZ4 across all modules")
        }
    }
    // --- Vulnerability fixes end ---

    radarKotlin {
        log4j2Version.set(rootProject.libs.versions.log4j2)
        sentryEnabled.set(true)
        openTelemetryAgentEnabled.set(false)
    }
}
