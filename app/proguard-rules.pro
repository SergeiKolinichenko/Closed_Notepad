# Room, Dagger, WorkManager and Compose ship their own consumer rules.
# Workers are created by RemovedNoteListWorkerFactory through their class name.
-keep class * extends androidx.work.ListenableWorker
