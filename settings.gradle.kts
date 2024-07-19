rootProject.name = "kafka-connect-rest-source"
include(":kafka-connect-fitbit-source")
include(":kafka-connect-rest-source")
include(":kafka-connect-oura-source")
include(":oura-library")

pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/UoM-Digital-Health-Software/CONNECT-RADAR-Schemas")
            credentials {
                username =  "jindrich.gorner@manchester.ac.uk"
                password =  System.getenv("key_package")
            }
        }
        gradlePluginPortal()
        mavenCentral()

    }
}
