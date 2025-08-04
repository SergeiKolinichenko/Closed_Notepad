package info.sergeikolinichenko.closednotepad.presentation.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/** Created by Sergei Kolinichenko on 23.11.2022 at 20:17 (GMT+3) **/

@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)
