# =========================================================
#               Общие правила проекта
# =========================================================
# Отключает обфускацию. Имена классов и методов не будут изменены.
# Это упрощает отладку, но увеличивает размер APK и облегчает реверс-инжиниринг.
# Рассмотрите возможность удаления этой строки для производственных сборок.
-dontobfuscate

# Сохраняет аннотации, необходимые для рефлексии
-keepattributes *Annotation*,Signature,EnclosingMethod

# Сохраняет все классы, реализующие Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Сохраняет имена нативных методов
-keepclasseswithmembernames class * {
    native <methods>;
}

# =========================================================
#               Правила для Kotlin и Coroutines
# =========================================================
# Сохраняет метаданные Kotlin, необходимые для рефлексии
-keep class kotlin.Metadata { *; }

# Сохраняет классы, связанные с корутинами
-keepclassmembers class ** extends kotlin.coroutines.jvm.internal.SuspendLambda {
    private <fields>;
    <methods>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# =========================================================
#               Правила для Room
# =========================================================
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Relation <fields>;
    @androidx.room.ColumnInfo <fields>;
}
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * {
  <init>(...);
}
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keep @androidx.room.TypeConverters class *
-keep class * extends androidx.room.RoomDatabase

# =========================================================
#               Правила для Ktor и kotlinx.serialization
# =========================================================
# Сохраняет классы, используемые для сериализации
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
    @kotlinx.serialization.Serializable <fields>;
}
-keep class kotlinx.serialization.** { *; }

# Сохраняет движки Ktor и их зависимости
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# =========================================================
#               Правила для org.jaudiotagger
# =========================================================
-keep class org.jaudiotagger.** { *; }
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.imageio.**
-dontwarn javax.swing.**

# =========================================================
#               Правила для gRPC и Protocol Buffers
# =========================================================
# Сохраняет сгенерированные классы сообщений Protobuf
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite$Builder { *; }

# Сохраняет классы, связанные с gRPC
-keep public class * extends io.grpc.stub.AbstractStub
-keep public class * extends io.grpc.stub.AbstractFutureStub
-keep public class * extends io.grpc.stub.AbstractAsyncStub

-keepclassmembers class * {
    @io.grpc.stub.annotations.RpcMethod <methods>;
}

# Предотвращает предупреждения для классов, которые могут отсутствовать
-dontwarn com.google.protobuf.**
-dontwarn io.grpc.netty.**
