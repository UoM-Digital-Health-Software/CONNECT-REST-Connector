description = "Kafka connector for Fitbit API source"

dependencies {
    api(project(":kafka-connect-rest-source"))
    api("io.confluent:kafka-connect-avro-converter:${Versions.confluent}")
    api("org.radarbase:radar-schemas-commons:${Versions.radarSchemas}")
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.google.firebase:firebase-admin:${Versions.firebaseAdmin}")

    implementation("io.ktor:ktor-client-auth:${Versions.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
    implementation("io.ktor:ktor-serialization-jackson:${Versions.ktor}")
    implementation("io.ktor:ktor-client-cio-jvm:${Versions.ktor}")

    // Included in connector runtime
    compileOnly("org.apache.kafka:connect-api:${Versions.kafka}")
    compileOnly(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.apache.kafka:connect-api:${Versions.kafka}")
}