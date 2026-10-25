package co.edu.uniquindio.entorno.features.map

import co.edu.uniquindio.entorno.domain.model.Report
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import kotlin.math.sqrt

/**
 * Peso de un reporte en el mapa de calor. Siempre es mayor que 0 (un reporte sin votos
 * igual se ve) y crece con los votos "Es importante". La raíz evita que un solo reporte
 * muy votado apague al resto.
 */
fun Report.heatWeight(): Double = 1.0 + sqrt(importantCount.toDouble())

/** Un punto por reporte, con su peso normalizado (0 a 1) en la propiedad "weight". */
fun List<Report>.toHeatFeatures(): FeatureCollection {
    val maxWeight = maxOfOrNull { it.heatWeight() } ?: 1.0
    return FeatureCollection.fromFeatures(
        map { report ->
            Feature.fromGeometry(report.location.toPoint()).also {
                it.addNumberProperty("weight", report.heatWeight() / maxWeight)
            }
        }
    )
}
