package org.neteinstein.pickaname.next

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.neteinstein.pickaname.di.appModules

/** Same startup as `:app`'s `PickANameApplication`, for the side-by-side KMP shell. */
class PickANameNextApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PickANameNextApplication)
            modules(appModules())
        }
    }
}
