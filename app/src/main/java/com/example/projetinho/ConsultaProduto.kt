package com.example.projetinho

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class ConsultaProduto : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConsultaAdapter
    private val listaProdutos = mutableListOf<ConsultaResponse>()

    interface ProdutoApi {
        @GET("/meu_projeto_api/buscar_produto_consulta.php")
        fun buscarProdutoPorCodigo(
            @Query("codigo_barras") codigo_barras: String
        ): Call<List<ConsultaResponse>>
    }

    private lateinit var produtoApi: ProdutoApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_produto)
        enableEdgeToEdge()

        val etCodigoBarras = findViewById<EditText>(R.id.editTextCodigoBarras)
        val btBuscar = findViewById<ImageButton>(R.id.btItens)
        recyclerView = findViewById(R.id.recyclerView)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.18.12/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        produtoApi = retrofit.create(ProdutoApi::class.java)

        adapter = ConsultaAdapter(listaProdutos) { position ->
            listaProdutos.removeAt(position)
            adapter.notifyItemRemoved(position)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btBuscar.setOnClickListener {
            val codigo = etCodigoBarras.text.toString().trim()
            if (codigo.isEmpty()) {
                Toast.makeText(this, "Digite o código de barras", Toast.LENGTH_SHORT).show()
            } else {
                buscarProduto(codigo)
            }
        }
        findViewById<ImageButton>(R.id.btVoltar).apply {
            setOnClickListener { voltar() }
        }
    }

    override fun onResume() {
        super.onResume()

        val edtCodigoBarras = findViewById<EditText>(R.id.editTextCodigoBarras)
        edtCodigoBarras.requestFocus()

        // Abre o teclado automaticamente
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtCodigoBarras, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun buscarProduto(codigo: String) {
        produtoApi.buscarProdutoPorCodigo(codigo).enqueue(object : Callback<List<ConsultaResponse>> {
            override fun onResponse(
                call: Call<List<ConsultaResponse>>,
                response: Response<List<ConsultaResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val produtosRecebidos = response.body()!!
                    if (produtosRecebidos.isNotEmpty()) {
                        listaProdutos.clear()
                        listaProdutos.addAll(produtosRecebidos)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@ConsultaProduto, "Produto não encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ConsultaProduto, "Erro na resposta do servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ConsultaResponse>>, t: Throwable) {
                Toast.makeText(this@ConsultaProduto, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun voltar(){
        val irParaTela = Intent(this@ConsultaProduto, HomeActivity::class.java)
        startActivity(irParaTela)
    }
}
