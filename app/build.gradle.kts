plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	id("kotlin-kapt")
}

android {
	namespace = "com.example.dewapermana_smkn8jember"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.example.dewapermana_smkn8jember"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	viewBinding {
		enable = true
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
	kotlinOptions {
		jvmTarget = "11"
	}
}

dependencies {
	val roomVersion = "2.5.0"
	val cameraVersion = "1.4.1"
	implementation("androidx.camera:camera-core:$cameraVersion")
	implementation("androidx.camera:camera-lifecycle:$cameraVersion")
	implementation("androidx.camera:camera-view:$cameraVersion")
	implementation("androidx.camera:camera-camera2:$cameraVersion")
	implementation("androidx.room:room-runtime:$roomVersion")
	kapt("androidx.room:room-compiler:$roomVersion")
	implementation("androidx.room:room-ktx:$roomVersion")
	implementation("androidx.exifinterface:exifinterface:1.3.6")
	implementation("com.google.android.gms:play-services-location:21.0.1")
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation("androidx.biometric:biometric:1.2.0-alpha05")
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}