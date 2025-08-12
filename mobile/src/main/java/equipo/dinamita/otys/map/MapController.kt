package equipo.dinamita.otys.map

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapController(
    private val context: Context,
    private val mapContainer: FrameLayout,
    private val viewToHide: View
) {
    private var mapView: MapView? = null

    fun showMap(latitude: Double, longitude: Double) {
        viewToHide.visibility = View.GONE
        mapContainer.visibility = View.VISIBLE

        if (mapView == null) {
            mapView = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
            }
            mapContainer.addView(mapView)
        }

        updateMapLocation(latitude, longitude)
    }

    fun hideMap() {
        mapContainer.visibility = View.GONE
        viewToHide.visibility = View.VISIBLE
    }

    fun updateMapLocation(latitude: Double, longitude: Double) {
        mapView?.let { map ->
            val geoPoint = GeoPoint(latitude, longitude)
            map.controller.setCenter(geoPoint)
            map.overlays.clear()

            val marker = Marker(map).apply {
                position = geoPoint
                title = "Última ubicación"
            }
            map.overlays.add(marker)
            map.invalidate()
        }
    }

    fun onResume() {
        mapView?.onResume()
    }

    fun onPause() {
        mapView?.onPause()
    }

    fun onDestroy() {
        mapView?.onDetach()
    }
}
