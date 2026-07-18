import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

fun loadProperties(fileName: String): Properties {
    val props = Properties()
    val file = rootProject.file(fileName)
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

android {
    namespace = "com.aplikasi.asanekaldadipisne"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        val prodProps = loadProperties("production.properties")
        val keystorePassword = prodProps.getProperty("KEYSTORE_PASSWORD")
            ?: throw GradleException("KEYSTORE_PASSWORD tidak ditemukan di production.properties!")

        create("release") {
            storeFile = rootProject.file("release.keystore")
            storePassword = keystorePassword
            keyAlias = "releaseKey"
            keyPassword = keystorePassword
        }
    }

    buildTypes {
        getByName("debug") {
            val devProps = loadProperties("local.properties")
            val devUrl = devProps.getProperty("ODOO_URL")
                ?: throw GradleException("⚠️ ODOO_URL tidak ditemukan di local.properties!")

            buildConfigField("String", "ODOO_URL", "\"$devUrl\"")

            isMinifyEnabled = false
        }

        getByName("release") {
            val prodProps = loadProperties("production.properties")
            val prodUrl = prodProps.getProperty("ODOO_URL")
                ?: throw GradleException("⚠️ ODOO_URL tidak ditemukan di production.properties!")

            buildConfigField("String", "ODOO_URL", "\"$prodUrl\"")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    defaultConfig {
        applicationId = "com.aplikasi.sarikembarpos"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}