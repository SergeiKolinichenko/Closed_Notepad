package info.sergeikolinichenko.closednotepad.presentation.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Created by Sergei Kolinichenko on 25.11.2022 at 20:04 (GMT+3) **/

@Module
class AppModule {

    // Saves started from a screen keep running after the screen is closed
    @Provides
    @ApplicationScope
    @AppCoroutineScope
    fun provideAppCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
