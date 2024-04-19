buildscript{
    dependencies{
        classpath("com.google.gms:google-services:4.4.1")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id ("com.android.library") version "8.0.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}