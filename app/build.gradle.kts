val nav_version = "2.9.5" // libs.versions.toml 반영된 최신 안정 버전

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"  // 반드시 버전 포함
    id("com.google.gms.google-services")
}

// 이전에 추가했던 repositories 블록을 삭제합니다.
// 프로젝트의 전역 설정 파일인 'settings.gradle.kts'에서 이미 정의하고 있기 때문입니다.
// repositories {
//     google()
//     mavenCentral()
// }

val googleServicesJson = sequence {
    val rootFile = file("google-services.json")
    if (rootFile.exists()) yield(rootFile)

    val srcDir = file("src")
    if (srcDir.isDirectory) {
        srcDir.listFiles { file -> file.isDirectory }?.forEach { variantDir ->
            val variantFile = variantDir.resolve("google-services.json")
            if (variantFile.exists()) yield(variantFile)
        }
    }
}.firstOrNull()

if (googleServicesJson != null) {
    logger.lifecycle(
        "Applying Google Services plugin using ${googleServicesJson.relativeTo(projectDir)}"
    )
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn(
        "Skipping Google Services plugin; no google-services.json found. Firebase-dependent features will be disabled."
    )
}

android {
    namespace = "com.songajae.amgi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.songajae.amgi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // composeOptions 블록은 Kotlin 2.0 이상부터 제거해야 합니다.
}

dependencies {
    // AndroidX core 세트
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    // Lifecycle 2.9.4 (libs.versions.toml 기준)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")  // 선택적으로 최신 버전 맞추기 가능
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")  // 선택적

    // Navigation 2.9.5 반영 (버전 카탈로그)
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // implementation("androidx.navigation:navigation-compose:$nav_version") // Compose 사용 시 주석 해제

    // Firebase (BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // 보안/저장
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // UI
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Jetpack Compose (버전 카탈로그 반영)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 중복 선언으로 삭제
    // implementation(libs.androidx.navigation.fragment.ktx)
    // implementation(libs.androidx.navigation.ui.ktx)
}

/*
configurations.all {
    resolutionStrategy.force(
        "androidx.lifecycle:lifecycle-common:2.9.4",
        "androidx.lifecycle:lifecycle-runtime-ktx:2.9.4",
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4",
        "androidx.lifecycle:lifecycle-livedata-ktx:2.9.4"
    )
}
*/
