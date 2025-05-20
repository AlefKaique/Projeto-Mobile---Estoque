package com.example.projetinho

import com.google.gson.annotations.SerializedName

data class EditarConsultaResponse(
    @SerializedName("CP_Desc")
    val descricao: String,

    @SerializedName("CP_Preco")
    val preco: String?,  // mudou para String para testar

    @SerializedName("CP_Marca")
    val marca: String?,

    @SerializedName("CP_Qtd")
    val quantidade: Int,

    @SerializedName("TP_Id")
    val tipo: Int
)
