# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# -------------------------------------------------------------------------------
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn info.sergeikolinichenko.closednotepad.database.AppDatabase$Companion
-dontwarn info.sergeikolinichenko.closednotepad.database.NotesDao
-dontwarn info.sergeikolinichenko.closednotepad.preferences.SharedPrefNotes$Companion
-dontwarn info.sergeikolinichenko.closednotepad.preferences.SharedPrefNotes
-dontwarn info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl_Factory
-dontwarn info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl_Factory
-dontwarn info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl_Factory
-dontwarn info.sergeikolinichenko.closednotepad.utils.NoteMapper_Factory
-dontwarn info.sergeikolinichenko.closednotepad.utils.RemovedNoteMapper_Factory
-dontwarn info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorkerFactory
-dontwarn info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorker_Factory_Factory