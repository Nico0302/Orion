import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.intellij") version "1.10.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

group = "de.tum.www1.artemis.plugin.intellij"

repositories {
    mavenCentral()
}

dependencies {
    // JSON parsing
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2023.1")
    plugins.set(listOf("Git4Idea", "maven", "PythonCore:231.8109.144"))
}

tasks {
    patchPluginXml {
        // Last 2 digits of the year and the major version digit, 211-211.* equals (20)21.1.*
        // See https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild.set("231")
        // Orion Plugin version. Needs to be incremented for every new release!
        version.set("1.2.2-alpha.2")
        changeNotes.set(
            """<p>
            <h1>Version Upgrade</h1>
            <h2>Improvements</h2>
            <ul>
                <li>Add support for assessment of exams, complaints and more feedback requests</li>
                <li>Add support structured grading criteria</li>
                <li>Fix an issue with artemis urls with trailing slashes</li>
                <li>Fix an issue with quotes in feedback comments</li>
                <li>Update dependencies</li>
            </ul>
        </p>"""
        )
    }

    publishPlugin {
        token.set("<your_token>")
    }
}
