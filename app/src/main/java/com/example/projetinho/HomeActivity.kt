package com.example.projetinho

import android.os.Bundle
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Tela não diusponivel no momento.
        val TelaIndispoinivel = findViewById<ImageButton>(R.id.btEntradaMaterial)
        TelaIndispoinivel.alpha = 0.5f

        findViewById<ImageButton>(R.id.btnCadastroProduto).apply {
            setOnClickListener { irParaTelaCadastrarProduto() }
        }
        findViewById<ImageButton>(R.id.btTelaVendas).apply {
            setOnClickListener { irParaTelaVendasProduto() }
        }
        findViewById<ImageButton>(R.id.btConsultar).apply {
            setOnClickListener { irParaTelaConsultaProduto() }
        }
        findViewById<ImageButton>(R.id.btEditar).apply {
            setOnClickListener { irParaTelaEditarProduto() }
        }
    }

    private fun irParaTelaCadastrarProduto(){
        val irParaTela = Intent(this@HomeActivity, CadastrarProdutoActivity::class.java)
        startActivity(irParaTela)
    }

    private fun irParaTelaVendasProduto(){
        val irParaTela = Intent(this@HomeActivity, VendasActivity::class.java)
        startActivity(irParaTela)
    }
    private fun irParaTelaConsultaProduto(){
        val irParaTela = Intent(this@HomeActivity, ConsultaProduto::class.java)
        startActivity(irParaTela)
    }
    private fun irParaTelaEditarProduto(){
        val irParaTela = Intent(this@HomeActivity, EditarProdutosActivity::class.java)
        startActivity(irParaTela)
    }

}
