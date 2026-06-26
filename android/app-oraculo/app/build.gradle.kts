plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.oraculo.app"

    compileSdk = 35

    defaultConfig {
        applicationId = "com.oraculo.app"

        minSdk = 30
        targetSdk = 35

        versionCode = 16
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        /*
         * Room schema export.
         *
         * Permite guardar el esquema de la base local para control de versiones.
         */
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://api-oraculo.sus.pe/\""
            )
        }

        release {
            isMinifyEnabled = false

            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://api-oraculo.sus.pe/\""
            )

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")

    implementation("androidx.activity:activity-ktx:1.9.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.airbnb.android:lottie:6.4.0")

    /*
 * Room - persistencia local para conversaciones y mensajes.
 *
 * Se usará para:
 * - guardar conversaciones locales
 * - guardar mensajes del usuario y del Oráculo
 * - guardar request_id de respuestas streaming
 * - guardar feedback local
 * - preparar sincronización futura con /feedback
 */
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}