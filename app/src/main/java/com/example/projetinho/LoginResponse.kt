package com.example.projetinho

data class LoginResponse(val usuarioId: Int,
                         val usuarioNome: String,
                         val usuarioEmail: String,
                         val usuarioSenha: String,
                         val usuarioCnpj: String
)