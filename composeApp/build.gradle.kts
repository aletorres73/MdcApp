import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)

    id("com.google.gms.google-services")
    alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activity.compose)
            implementation(project.dependencies.platform(libs.android.firebase.bom))
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime)
            implementation(libs.gitlive.firebase.crashlytics)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
//            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.auth)
            implementation(libs.gitlive.firebase.analytics)
            implementation(libs.gitlive.firebase.storage)
//            implementation(libs.gitlive.firebase.crashlytics)
            implementation(libs.napier)

            implementation(libs.koin.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serilization.json)
            implementation(libs.androidx.navigation.compose)
//            implementation(libs.material.icons.extended) esta librería es una pija, rompe todo

//            implementation(libs.compose.foundation.desktop)
//            implementation(libs.compose.material3.desktop)
//            implementation(libs.compose.runtime.desktop)
//            implementation(libs.compose.ui.text.desktop)
//            implementation(libs.compose.ui.util.desktop)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
//            implementation(compose.components.uiToolingPreview)
//            implementation(libs.compose.ui.tooling)
//            implementation(libs.compose.ui.util)
//            implementation(libs.compose.foundation.desktop)
//            implementation(libs.compose.material3.desktop)
//            implementation(libs.compose.runtime.desktop)
//            implementation(libs.compose.ui.text.desktop)
//            implementation(libs.compose.ui.util.desktop)
        }
    }
    task("testClasses")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

android {
    namespace = "com.mdcapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mdcapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "4.3.1"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // Nomenclatura personalizada para el APK
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val fileName = "MDCapp-v${versionName}.apk"
            output.outputFileName = fileName
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.mdcapp.MainKt"

        nativeDistributions {
            windows {
                includeAllModules = true
            }
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MDCApp"
            packageVersion = "1.0.0"
        }
    }
}

