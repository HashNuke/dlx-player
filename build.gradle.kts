import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// Static analysis across every module's Kotlin sources, run via `./gradlew detekt`.
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom("shared/src", "androidApp/src")
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required = true
        sarif.required = true
        xml.required = false
        txt.required = false
    }
}

// Format check and auto-format via ktlint, run via `./gradlew spotlessCheck` / `spotlessApply`.
spotless {
    kotlin {
        target(
            "shared/src/**/*.kt",
            "androidApp/src/**/*.kt",
            "*.kts",
            "shared/*.kts",
            "androidApp/*.kts",
        )
        ktlint(libs.versions.ktlint.get())
    }
}
