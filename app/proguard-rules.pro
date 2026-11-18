# Reglas de R8 para la versión release de Entorno.
# Hilt, Firebase, Mapbox, Coil y kotlinx.serialization traen sus propias reglas
# (consumer rules); aquí solo va lo específico de la app.

# Información útil para leer los stack traces de crashes
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Firestore convierte documentos <-> DTOs por reflexión (nombres de campos y
# constructor vacío). Si R8 los renombra o los quita, los datos llegan vacíos.
-keep class co.edu.uniquindio.entorno.data.model.** { *; }

# Enums del dominio: se guardan y leen por nombre (valueOf / name)
-keepclassmembers enum co.edu.uniquindio.entorno.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# WorkManager (dependencia transitiva) guarda su estado en una base Room.
# Room instancia XxxDatabase_Impl por reflexión; sin esta regla R8 la borra y la
# app se cierra al arrancar ("Failed to create an instance of WorkDatabase").
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-dontwarn androidx.room.paging.**

# Cloudinary sube las fotos con WorkManager, que crea sus Workers e InputMergers por
# reflexión (constructor vacío / (Context, WorkerParameters)). Sin esto la subida falla en
# silencio y "Publicando..." no termina nunca.
-keep class * extends androidx.work.InputMerger { public <init>(); }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.OverwritingInputMerger { *; }
-keep class androidx.work.ArrayCreatingInputMerger { *; }

# Cloudinary elige su adaptador de red por reflexión (UploaderStrategy);
# sin esto falla con "Can't find Cloudinary platform adapter".
-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**

# Modelos del dominio (por si se serializan o se pasan entre pantallas)
-keep class co.edu.uniquindio.entorno.domain.model.** { *; }
