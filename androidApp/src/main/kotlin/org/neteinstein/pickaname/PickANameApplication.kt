package org.neteinstein.pickaname

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.neteinstein.pickaname.di.appModules

class PickANameApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Required once before any PdfTextExtractor usage - lets pdfbox-android load its
        // bundled font/glyph resources from assets.
        PDFBoxResourceLoader.init(applicationContext)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PickANameApplication)
            // The whole graph - shared modules plus Android's platformModule() - is assembled in
            // composeApp, so this shell and webApp's main() register exactly the same thing.
            modules(appModules())
        }
    }
}
