package com.example.projetinho

import com.google.gson.annotations.SerializedName

data class VendasResponse(
    @SerializedName("CP_Desc")
    val nome: String,

    @SerializedName("CP_CodigoBarras")
    val codigo_barras: String,

    @SerializedName("CP_Preco")
    val preco: Double,

    @SerializedName("CP_Qtd")
    val quantidade: Int
)