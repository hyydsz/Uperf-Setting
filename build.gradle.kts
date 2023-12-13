// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
}
true // Needed to make the Suppress annotation work for the plugins block