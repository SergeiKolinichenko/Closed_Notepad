package info.sergeikolinichenko.closednotepad.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Version 1 kept notes and removed notes in two tables keyed by the creation time.
// Version 2 keeps every note in one table with a type, a trash mark and list items.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`type` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, " +
                "`colorIndex` INTEGER NOT NULL, `isLocked` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `removedAt` INTEGER)"
        )
        db.execSQL(
            "INSERT INTO `notes_new` (`type`, `title`, `body`, `colorIndex`, `isLocked`, `isPinned`, " +
                "`createdAt`, `updatedAt`, `removedAt`) " +
                "SELECT 'TEXT', `titleNote`, `itselfNote`, `colorIndex`, `isLocked`, 0, " +
                "`timeStamp`, `timeStamp`, NULL FROM `notes`"
        )
        db.execSQL(
            "INSERT INTO `notes_new` (`type`, `title`, `body`, `colorIndex`, `isLocked`, `isPinned`, " +
                "`createdAt`, `updatedAt`, `removedAt`) " +
                "SELECT 'TEXT', `titleEntry`, `itselfEntry`, `colorIndex`, `isLocked`, 0, " +
                "`timeStamp`, `timeStamp`, `timeRemove` FROM `removed_notes`"
        )
        db.execSQL("DROP TABLE `notes`")
        db.execSQL("DROP TABLE `removed_notes`")
        db.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_removedAt` ON `notes` (`removedAt`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `note_items` (`id` TEXT NOT NULL, `noteId` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, `text` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, " +
                "`quantity` TEXT NOT NULL, `section` TEXT NOT NULL, `price` REAL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`noteId`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_items_noteId` ON `note_items` (`noteId`)")
    }
}
