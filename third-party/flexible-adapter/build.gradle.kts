plugins {
    alias(mihonx.plugins.android.library)
}

android {
    namespace = "eu.davidea.flexibleadapter"
    buildFeatures { buildConfig = true }
    defaultConfig { buildConfigField("String", "VERSION_NAME", "\"5.1.0\"") }
}

dependencies {
    api(libs.androidx.recyclerView)
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
}
