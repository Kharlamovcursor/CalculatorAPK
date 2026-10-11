package ru.hdlabs.electrosmeta

import android.app.Application
import ru.hdlabs.electrosmeta.di.AppContainer

class ElectrosmetaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
