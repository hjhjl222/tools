import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // 版本由宿主工程的根 build.gradle.kts（或独立构建时的 settings.gradle.kts pluginManagement）提供
    id("com.android.library")
    // 宿主工程用 AGP 9 内置 Kotlin，KGP 只登记版本不应用；独立构建时在下面手动应用。
    id("org.jetbrains.kotlin.android") apply false
}

// 独立构建（AGP 8.x，没有内置 Kotlin）时才自己应用 KGP。
if (extensions.findByName("kotlin") == null) {
    apply(plugin = "org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bhh.tools"
    // 与宿主 App 保持一致：本树 SDK 是 API 36.1（AGP 8.13.2 / 9.3.3 都支持 compileSdkMinor）
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        minSdk = 29

        consumerProguardFiles("consumer-rules.pro")
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
}

// 不用 `kotlin { }` 类型安全访问器：宿主工程里 KGP 是由 AGP 内置 Kotlin 注册的，
// 只有独立构建才由脚本主动 apply，访问器不会生成。按类型取扩展两种场景都能用。
extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension>("kotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    val kotlinxCoroutinesVersion = "1.8.1"
    val retrofitVersion = "2.11.0"
    val gsonVersion = "2.10.1"
    val androidxCoreKtxVersion = "1.13.1"
    val androidxAppCompatVersion = "1.7.0"
    val androidxMaterialVersion = "1.12.0"
    val androidxConstraintsVersion = "1.2.0"
    val logbackAndroidVersion = "3.0.0"
    val slf4jVersion = "2.0.16"
    val commonsCodecVersion = "1.17.1"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion")

    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    api("com.google.code.gson:gson:$gsonVersion")
    api("androidx.core:core-ktx:$androidxCoreKtxVersion")
    api("androidx.appcompat:appcompat:$androidxAppCompatVersion")
    api("com.google.android.material:material:$androidxMaterialVersion")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintsVersion")

    // okhttp 由 retrofit 传递引入，源码直接使用了 okhttp3.* —— 显式声明避免版本漂移
    api("com.squareup.okhttp3:okhttp:4.12.0")

    // 源码使用 ch.qos.logback.*（含 android 专用 LogcatAppender）与 org.slf4j.*
    // logback-android 只带 slf4j binding，不带 slf4j-api，因此必须显式声明
    api("com.github.tony19:logback-android:$logbackAndroidVersion") {
        exclude(group = "xerces", module = "xmlParser")
    }
    api("org.slf4j:slf4j-api:$slf4jVersion")

    // 源码使用 org.apache.commons.codec.binary.Hex
    implementation("commons-codec:commons-codec:$commonsCodecVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
