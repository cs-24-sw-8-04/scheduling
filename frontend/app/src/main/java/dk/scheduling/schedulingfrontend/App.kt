package dk.scheduling.schedulingfrontend

import android.app.Application
import dk.scheduling.schedulingfrontend.module.AppModule
import dk.scheduling.schedulingfrontend.module.IAppModule

class App : Application() {
    companion object {
        lateinit var appModule: IAppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
    }
}
