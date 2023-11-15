package com.example.linarkursach

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class Map : ComponentActivity() {
    private val MAPKIT_API_KEY = "ba1e4ee2-a331-4269-8e6b-085954773601"
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        setContentView(R.layout.map)
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.mapview)

        val Button2 = findViewById<Button>(R.id.secondButton)

        Button2.setOnClickListener {
            finish()
        }
        mapView?.map?.move(
            CameraPosition(Point(55.796127, 49.106414), 12.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 5F),
            null
        )
    }

}
