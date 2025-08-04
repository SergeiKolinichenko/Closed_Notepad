package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import android.app.backup.BackupManager
import dagger.Module
import dagger.Provides

/** Created by Sergei Kolinichenko on 25.11.2022 at 20:04 (GMT+3) **/

@Module
class AppModule {

    @Provides
    fun provideBackupManager(application: Application): BackupManager {
        return BackupManager(application)
    }
}