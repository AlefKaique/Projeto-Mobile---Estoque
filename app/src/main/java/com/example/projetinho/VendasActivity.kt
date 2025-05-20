package com.example.projetinho

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

class VendasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VendasAdapter
    private val listaItens = mutableListOf<VendasResponse>()
    private lateinit var etValorTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendas)

        enableEdgeToEdge()

        etValorTotal = findViewById(R.id.editValorTotal)
        val etCodigoBarras = findViewById<EditText>(R.id.editTextCodigoBarras)
        val etQuantidade = findViewById<EditText>(R.id.editTextQtd)
        val btItens = findViewById<ImageButton>(R.id.btItens)
        val btVendas = findViewById<Button>(R.id.btVendas)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = VendasAdapter(listaItens) { item ->
            listaItens.removeAt(item)
            adapter.notifyDataSetChanged()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btVoltar).apply {
            setOnClickListener { voltar() }
        }

        btItens.setOnClickListener {
            val codigo = etCodigoBarras.text.toString()
            val quantidadeTexto = etQuantidade.text.toString()
            val quantidadeSolicitada = quantidadeTexto.toIntOrNull()

            if (codigo.isNotBlank() && quantidadeTexto.isNotBlank()) {
                if (quantidadeSolicitada != null) {

                    val jaAdicionado = listaItens.any { it.codigo_barras == codigo }
                    if (jaAdicionado) {
                        Toast.makeText(
                            this,
                            "Este produto já foi adicionado à lista.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://192.168.18.12/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ApiService::class.java)
                    val call = apiService.buscar(codigo, quantidadeSolicitada)

                    call.enqueue(object : Callback<List<VendasResponse>> {
                        override fun onResponse(call: Call<List<VendasResponse>>, response: Response<List<VendasResponse>>) {
                            if (response.isSuccessful) {
                                val itensRecebidos = response.body() ?: emptyList()

                                if (itensRecebidos.isNotEmpty()) {
                                    val item = itensRecebidos[0]
                                    if (quantidadeSolicitada <= item.quantidade) {
                                        val itemModificado = item.copy(quantidade = quantidadeSolicitada)
                                        listaItens.add(itemModificado)
                                        adapter.notifyDataSetChanged()
                                        atualizarValorTotal()
                                        etCodigoBarras.text.clear()
                                        etQuantidade.text.clear()
                                        etCodigoBarras.requestFocus()// Atualiza o total após adicionar item
                                    } else {
                                        Toast.makeText(
                                            this@VendasActivity,
                                            "Quantidade solicitada (${quantidadeSolicitada}) é maior que a disponível (${item.quantidade})",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(this@VendasActivity, "Quantidade não disponível", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@VendasActivity, "Erro ao buscar produto", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<List<VendasResponse>>, t: Throwable) {
                            Toast.makeText(this@VendasActivity, "Falha: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha o código de barras e a quantidade", Toast.LENGTH_SHORT).show()
            }
        }

        btVendas.setOnClickListener {
            if (listaItens.isNotEmpty()) {
                val itensFormatados = listaItens.joinToString(",") { "${it.codigo_barras}:${it.quantidade}" }

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.18.12/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiService = retrofit.create(ApiService::class.java)
                val call = apiService.finalizarVenda(itensFormatados)

                call.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            val jsonResponse = response.body()

                            val status = jsonResponse?.get("status")?.asString ?: "erro"
                            if (status == "ok") {
                                Toast.makeText(this@VendasActivity, "Venda finalizada com sucesso", Toast.LENGTH_SHORT).show()
                                listaItens.clear()
                                adapter.notifyDataSetChanged()
                                atualizarValorTotal() // Zera o total após finalizar
                            } else {
                                val mensagens = jsonResponse?.getAsJsonArray("mensagens") ?: JsonArray()
                                val mensagensErro = mensagens.joinToString("\n") { it.asJsonObject.get("mensagem").asString }
                                Toast.makeText(this@VendasActivity, "Erro ao finalizar a venda:\n$mensagensErro", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@VendasActivity, "Erro ao comunicar com o servidor", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Toast.makeText(this@VendasActivity, "Falha: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Nenhum item na venda", Toast.LENGTH_SHORT).show()
            }
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

    private fun atualizarValorTotal() {
        val total = listaItens.sumOf { it.preco * it.quantidade }
        etValorTotal.text = String.format("%.2f", total)
    }

    private fun voltar() {
        val irParaTela = Intent(this@VendasActivity, HomeActivity::class.java)
        startActivity(irParaTela)
    }

    interface ApiService {
        @GET("/meu_projeto_api/buscar_produto.php")
        fun buscar(
            @Query("codigo_barras") codigo_barras: String,
            @Query("quantidade") quantidade: Int
        ): Call<List<VendasResponse>>

        @POST("/meu_projeto_api/finalizar_venda.php")
        @FormUrlEncoded
        fun finalizarVenda(
            @Field("itens") itens: String
        ): Call<JsonObject>
    }
}