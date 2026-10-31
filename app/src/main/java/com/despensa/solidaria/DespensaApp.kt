package com.despensa.solidaria

import android.app.Application
import com.despensa.solidaria.data.Graph

class DespensaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.iniciar(this)
    }
}
