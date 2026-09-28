pluginManagement {
    plugins {
        id("org.springframework.boot") version "4.1.1"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "transaction-processor"

include("data-model", "api-gateway")
