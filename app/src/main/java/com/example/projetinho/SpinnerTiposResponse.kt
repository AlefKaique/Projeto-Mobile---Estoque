package com.example.projetinho

import com.google.gson.annotations.SerializedName

data class SpinnerTiposResponse(
    @SerializedName("TP_Id")
    val id: Int,

    @SerializedName("TP_Desc")
    val descricao: String
)