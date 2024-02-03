plugins {
    id("build.version")
    id("build.java")
    id("build.kotlin")
    id("build.test")
    id("build.coverage")
    id("build.publishing")
    alias(libs.plugins.nexusPublish)
}

dependencies {
    api(libs.slf4j.api)
    api(libs.jetbrains.annotations)
    implementation(libs.snakeyaml)
    implementation(libs.gson)
    implementation(libs.icu4j)
    testImplementation(libs.logback.classic)
    testImplementation(libs.spock.core)
    testImplementation(libs.awaitility)
    integrationTestImplementation(libs.jimfs)
}

nexusPublishing {
    repositories {
        sonatype {
            System.getenv("OSSRH_STAGING_PROFILE_ID")?.let { stagingProfileId = it }
            System.getenv("OSSRH_USERNAME")?.let { username.set(it) }
            System.getenv("OSSRH_PASSWORD")?.let { password.set(it) }
        }
    }
}