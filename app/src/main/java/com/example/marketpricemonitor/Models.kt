package com.example.marketpricemonitor

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("query") val query: String,
    @SerializedName("count") val count: Int,
    @SerializedName("items") val items: List<Product>
)

data class Product(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("price_rub") val priceRub: Double,
    @SerializedName("supplier") val supplier: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("feedbacks") val feedbacks: Int?
)