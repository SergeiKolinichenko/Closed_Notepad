package info.sergeikolinichenko.closednotepad.presentation.di

import androidx.work.ListenableWorker
import dagger.MapKey
import kotlin.reflect.KClass

/** Created by Sergei Kolinichenko on 04.12.2022 at 17:38 (GMT+3) **/

@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)
