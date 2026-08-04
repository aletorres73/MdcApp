# --- Reglas Generales de Android ---
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# --- Kotlin Serialization ---
# Keep all Serializable classes and their companion objects
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * extends kotlinx.serialization.KSerializer {
    public static ** INSTANCE;
}
# Don't obfuscate SerialName names
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# --- Firebase / Firestore ---
-keep class com.google.firebase.** { *; }
-keep class dev.gitlive.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn dev.gitlive.firebase.**

# --- Napier (Logging) ---
-keep class io.github.aakira.napier.** { *; }

# --- Koin ---
-keep class org.koin.** { *; }

# --- Modelos de Dominio (Data classes de Firestore) ---
# Mantenemos todos los modelos para que Firestore pueda mapearlos correctamente
-keep class com.mdcapp.domain.entities.** { *; }
-keep class com.mdcapp.data.remote.** { *; }
