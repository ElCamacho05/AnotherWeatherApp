// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.android.library") version "7.4.2" apply false

    // AÑADE ESTO: Declaramos que usaremos el plugin de Google Services en algún módulo
    id("com.google.gms.google-services") version "4.4.0" apply false // Verifica tu versión
}