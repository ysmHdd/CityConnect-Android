package com.example.tpsqlite

import java.util.Date

data class Location(
    var ville: String = "",
    var quartier: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

data class Image(
    var url: String = "",
    var caption: String = "",
    var uploadedAt: Date = Date()
)

data class Report(
    var titre: String = "",
    var description: String = "",
    var categorie: String = "",
    var sousCategorie: String? = null,
    var statut: String = "soumis",
    var priorite: Int = 3,
    var urgence: Int = 1,
    var impact: Int = 1,
    var scorePrioriteIA: Int? = null,
    var nbSoutiens: Int = 0,
    var nbCommentaires: Int = 0,
    var localisation: Location = Location(),
    var citoyenId: String = "",
    var agentId: String? = null,
    var dateResolution: Date? = null,
    var images: List<Image> = listOf(),
    var tags: List<String> = listOf(),
    var estAnonyme: Boolean = false,
    var dateSoumission: Date = Date(),
    var dateModification: Date = Date()
) {
    fun calculerPriorite(): Int {
        val baseScore = priorite * 20
        val urgencyBonus = urgence * 10
        val impactBonus = impact * 8
        val supportBonus = minOf(nbSoutiens * 2, 20)
        return baseScore + urgencyBonus + impactBonus + supportBonus
    }

    val ageJours: Int
        get() = ((Date().time - dateSoumission.time) / (1000 * 60 * 60 * 24)).toInt()
}
