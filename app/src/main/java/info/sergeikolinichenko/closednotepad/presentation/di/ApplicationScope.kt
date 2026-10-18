package info.sergeikolinichenko.closednotepad.presentation.di

import javax.inject.Qualifier
import javax.inject.Scope

/** Created by Sergei Kolinichenko on 23.11.2022 at 21:19 (GMT+3) **/

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

// Marks the coroutine scope that lives as long as the application
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AppCoroutineScope
