plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "app.dlxplayer.shared"
        compileSdk = 36
        minSdk = 24
    }

    // JVM target keeps the TDD loop fast on any machine: `./gradlew :shared:jvmTest`.
    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        tvosArm64(),
        tvosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "DLXShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
        // CIO on JVM/Android: OkHttp 5.x (Ktor 3.6's default) requires
        // compileSdk 37, above AGP 9.0.0's cap of 36. Switch back to
        // ktor-client-okhttp once the project moves to AGP + SDK 37.
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// Opt-in pass-through for the reference-server smoke test: the check only
// runs when the URL is provided, e.g.
//   ./gradlew :shared:jvmTest -PjellyfinRefUrl=http://127.0.0.1:8096
tasks.withType<Test>().configureEach {
    project.findProperty("jellyfinRefUrl")?.toString()?.takeIf { it.isNotBlank() }?.let {
        environment("JELLYFIN_REF_URL", it)
    }
    project.findProperty("jellyfinRefPassword")?.toString()?.takeIf { it.isNotBlank() }?.let {
        environment("JELLYFIN_REF_PASSWORD", it)
    }
}
