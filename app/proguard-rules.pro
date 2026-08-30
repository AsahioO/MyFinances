# Rutas de navegacion tipadas: kotlinx-serialization necesita conservar los
# serializers generados de los @Serializable de ui/navigation.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.finanzas.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.finanzas.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
