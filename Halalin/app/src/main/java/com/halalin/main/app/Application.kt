package com.halalin.main.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder

class Application : Application(), ImageLoaderFactory {
    override fun newImageLoader() = ImageLoader.Builder(this)
        .componentRegistry {
            add(SvgDecoder(this@Application))
        }
        .build()
}
