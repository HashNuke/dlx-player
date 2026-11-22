plugins {
    id("com.android.application")
}

android {
    namespace = "app.dlxplayer.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.dlxplayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":shared"))
}
