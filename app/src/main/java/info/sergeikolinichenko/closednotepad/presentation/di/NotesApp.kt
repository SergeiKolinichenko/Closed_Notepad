package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:04 (GMT+3) **/

class NotesApp: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

}