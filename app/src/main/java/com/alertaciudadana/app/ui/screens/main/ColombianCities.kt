package com.alertaciudadana.app.ui.screens.main

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

data class ColombianCity(
    val name: String,
    val center: GeoPoint,
    val bounds: BoundingBox
)

object ColombianCities {
    val all = listOf(
        ColombianCity("Bogotá", GeoPoint(4.7110, -74.0721), BoundingBox(4.90, -73.95, 4.48, -74.25)),
        ColombianCity("Medellín", GeoPoint(6.2442, -75.5812), BoundingBox(6.38, -75.45, 6.10, -75.72)),
        ColombianCity("Cali", GeoPoint(3.4516, -76.5320), BoundingBox(3.58, -76.42, 3.30, -76.65)),
        ColombianCity("Barranquilla", GeoPoint(10.9685, -74.7813), BoundingBox(11.08, -74.65, 10.85, -74.90)),
        ColombianCity("Cartagena", GeoPoint(10.3910, -75.4794), BoundingBox(10.52, -75.35, 10.28, -75.62)),
        ColombianCity("Bucaramanga", GeoPoint(7.1193, -73.1227), BoundingBox(7.25, -73.02, 7.02, -73.22)),
        ColombianCity("Pereira", GeoPoint(4.8087, -75.6906), BoundingBox(4.92, -75.58, 4.68, -75.80)),
        ColombianCity("Manizales", GeoPoint(5.0703, -75.5138), BoundingBox(5.17, -75.42, 4.96, -75.61)),
        ColombianCity("Pasto", GeoPoint(1.2136, -77.2811), BoundingBox(1.30, -77.20, 1.12, -77.38)),
        ColombianCity("Ibagué", GeoPoint(4.4389, -75.2322), BoundingBox(4.56, -75.12, 4.31, -75.35)),
        ColombianCity("Santa Marta", GeoPoint(11.2408, -74.1990), BoundingBox(11.38, -74.05, 11.08, -74.35)),
        ColombianCity("Cúcuta", GeoPoint(7.8891, -72.4967), BoundingBox(8.02, -72.38, 7.76, -72.62)),
        ColombianCity("Villavicencio", GeoPoint(4.1420, -73.6266), BoundingBox(4.27, -73.51, 4.02, -73.76)),
        ColombianCity("Armenia", GeoPoint(4.5339, -75.6811), BoundingBox(4.63, -75.58, 4.43, -75.79)),
        ColombianCity("Neiva", GeoPoint(2.9273, -75.2819), BoundingBox(3.05, -75.18, 2.81, -75.39)),
        ColombianCity("Montería", GeoPoint(8.7479, -75.8814), BoundingBox(8.86, -75.77, 8.63, -76.01)),
        ColombianCity("Sincelejo", GeoPoint(9.3047, -75.3978), BoundingBox(9.40, -75.30, 9.20, -75.50)),
        ColombianCity("Valledupar", GeoPoint(10.4631, -73.2532), BoundingBox(10.58, -73.13, 10.34, -73.38)),
        ColombianCity("Popayán", GeoPoint(2.4448, -76.6147), BoundingBox(2.56, -76.51, 2.33, -76.73)),
        ColombianCity("Tunja", GeoPoint(5.5353, -73.3678), BoundingBox(5.65, -73.27, 5.42, -73.49)),
        ColombianCity("Florencia", GeoPoint(1.6144, -75.6062), BoundingBox(1.71, -75.51, 1.52, -75.71)),
        ColombianCity("Riohacha", GeoPoint(11.5444, -72.9072), BoundingBox(11.65, -72.80, 11.44, -73.03)),
        ColombianCity("Quibdó", GeoPoint(5.6947, -76.6611), BoundingBox(5.80, -76.56, 5.59, -76.77)),
        ColombianCity("Arauca", GeoPoint(7.0847, -70.7591), BoundingBox(7.18, -70.66, 6.98, -70.87)),
        ColombianCity("Yopal", GeoPoint(5.3378, -72.3959), BoundingBox(5.45, -72.28, 5.22, -72.52)),
        ColombianCity("Mocoa", GeoPoint(1.1528, -76.6521), BoundingBox(1.25, -76.56, 1.06, -76.75)),
        ColombianCity("San Andrés", GeoPoint(12.5847, -81.7006), BoundingBox(12.66, -81.63, 12.51, -81.78))
    )

    fun findForLocation(latitude: Double, longitude: Double): ColombianCity? {
        val point = GeoPoint(latitude, longitude)
        return all.firstOrNull { it.bounds.contains(point) }
            ?: all.minByOrNull { it.center.distanceToAsDouble(point) }
    }
}
