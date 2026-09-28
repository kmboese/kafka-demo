import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot") apply false
}

subprojects {
    group = "com.transactionprocessor"
    version = "0.0.1-SNAPSHOT"

    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            // Target is Java 27; 25 (LTS) until Temurin 27 images are published.
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testImplementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
