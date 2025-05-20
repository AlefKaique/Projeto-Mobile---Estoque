package com.example.projetinho

data class CadastroResponse(val descProduto: String,
                            val codigoBarras: String,
                            val qtd: Int,
                            val preco: Double,
                            val marca: String,
                            val tp_Id: Int
)