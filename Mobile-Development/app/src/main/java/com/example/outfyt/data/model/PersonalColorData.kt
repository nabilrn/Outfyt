package com.example.outfyt.data.model

data class PersonalColorData(
    val imageUrl: String?,
    val colorType: String?,
    val genderCategory: String?,
    val recommendedColors: List<ColorRecommendation>
)

data class ColorRecommendation(val hexCode: String)
