plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "com.example.aegischest"
version = "1.0.0-26.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:26.2.build.2611-stable")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25) // Updating to 25 for modern Java / Purpur 26.2
}
