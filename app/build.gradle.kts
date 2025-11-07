plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase
    kotlin("android")
}

android {
    namespace = "com.example.projetoindividual"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.projetoindividual"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.work:work-runtime:2.9.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.3")
    implementation("androidx.navigation:navigation-ui:2.7.3")

    // Calendário
    implementation("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support", module = "support-compat")
    }

    // SQL Server JDBC
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx") // opcional se usares Firestore

    // Testes
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

