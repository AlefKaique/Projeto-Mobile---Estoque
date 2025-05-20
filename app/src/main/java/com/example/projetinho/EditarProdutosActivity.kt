package com.example.projetinho

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProdutoApi {
    @GET("/meu_projeto_api/buscar_produto_editar.php")
    fun buscarProdutoPorCodigo(
        @Query("codigo_barras") codigo_barras: String
    ): Call<List<EditarConsultaResponse>>

    @GET("/meu_projeto_api/buscar_tipos_produto_spinner.php")
    fun listarTiposProduto(): Call<List<SpinnerTiposResponse>>

    @POST("/meu_projeto_api/atualizar_produto.php")
    fun atualizarProduto(@Body produto: EditarProdutoRequest): Call<Void>

    @POST("/meu_projeto_api/deletar_produto.php")
    fun deletarProduto(@Body body: Map<String, String>): Call<Void>
}

class EditarProdutosActivity : AppCompatActivity() {

    private lateinit var spinnerTipos: Spinner
    private lateinit var tiposAdapter: SpinnerTiposAdapter
    private val listaTipos = mutableListOf<SpinnerTiposResponse>()

    private lateinit var editCodigoBarras: EditText
    private lateinit var editDesc: EditText
    private lateinit var editPreco: EditText
    private lateinit var editMarca: EditText
    private lateinit var editQtd: EditText
    private lateinit var btAlterar: Button
    private lateinit var btExcluir: Button

    private lateinit var api: ProdutoApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_produtos)

        spinnerTipos = findViewById(R.id.spinnerTipos)
        editCodigoBarras = findViewById(R.id.editCodigoBarras)
        editDesc = findViewById(R.id.editDesc)
        editPreco = findViewById(R.id.editPreco)
        editMarca = findViewById(R.id.editMarca)
        editQtd = findViewById(R.id.editQtd)
        btAlterar = findViewById(R.id.btCadastro)
        btExcluir = findViewById(R.id.btExcluir)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.18.12/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ProdutoApi::class.java)

        tiposAdapter = SpinnerTiposAdapter(this, listaTipos)
        spinnerTipos.adapter = tiposAdapter

        carregarTipos()

        desativarCampos()

        editCodigoBarras.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val codigo = editCodigoBarras.text.toString().trim()
                if (codigo.isNotEmpty()) {
                    buscarProduto(codigo)
                }
                true
            } else {
                false
            }
        }

        findViewById<ImageButton>(R.id.btVoltar).apply {
            setOnClickListener { voltar() }
        }

        btAlterar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmar alteração")
                .setMessage("Tem certeza que deseja atualizar este produto?")
                .setPositiveButton("Sim") { _, _ -> atualizarProduto() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btExcluir.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Tem certeza que deseja deletar este produto?")
                .setPositiveButton("Sim") { _, _ -> deletarProduto() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()

        val edtCodigoBarras = findViewById<EditText>(R.id.editCodigoBarras)
        edtCodigoBarras.requestFocus()

        // Abre o teclado automaticamente
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtCodigoBarras, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun voltar(){
        val irParaTela = Intent(this@EditarProdutosActivity, HomeActivity::class.java)
        startActivity(irParaTela)
    }

    private fun carregarTipos() {

        api.listarTiposProduto().enqueue(object : Callback<List<SpinnerTiposResponse>> {
            override fun onResponse(
                call: Call<List<SpinnerTiposResponse>>,
                response: Response<List<SpinnerTiposResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    listaTipos.clear()
                    listaTipos.addAll(response.body()!!)
                    tiposAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@EditarProdutosActivity, "Falha ao carregar tipos de produto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SpinnerTiposResponse>>, t: Throwable) {
                Toast.makeText(this@EditarProdutosActivity, "Erro ao carregar tipos: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarProduto(codigo: String) {
        api.buscarProdutoPorCodigo(codigo).enqueue(object : Callback<List<EditarConsultaResponse>> {
            override fun onResponse(call: Call<List<EditarConsultaResponse>>, response: Response<List<EditarConsultaResponse>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val produto = response.body()!![0]
                    val precoDouble = produto.preco?.toDoubleOrNull() ?: 0.0
                    Log.d("EditarProdutos", "Produto recebido: $produto")

                    editDesc.setText(produto.descricao ?: "")

                    editPreco.setText(String.format("%.2f", precoDouble))

                    editMarca.setText(produto.marca ?: "")

                    editQtd.setText(produto.quantidade?.toString() ?: "0")

                    val pos = listaTipos.indexOfFirst { it.id == produto.tipo }
                    spinnerTipos.setSelection(if (pos >= 0) pos else 0)

                    btAlterar.alpha = 1.0f
                    btExcluir.alpha = 1.0f

                    ativarCampos()
                } else {
                    limparCampos()
                    desativarCampos()
                    Toast.makeText(this@EditarProdutosActivity, "Produto não encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<EditarConsultaResponse>>, t: Throwable) {
                Toast.makeText(this@EditarProdutosActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                desativarCampos()
            }
        })
    }
    private fun atualizarProduto() {
        val descricao = editDesc.text.toString().trim()
        val preco = editPreco.text.toString().toDoubleOrNull() ?: 0.0
        val marca = editMarca.text.toString().trim()
        val quantidade = editQtd.text.toString().toIntOrNull() ?: 0
        val tipoSelecionado = spinnerTipos.selectedItem as SpinnerTiposResponse
        val tipo = tipoSelecionado.id
        val codigoBarras = editCodigoBarras.text.toString().trim()

        val produtoAtualizado = EditarProdutoRequest(
            descricao = descricao,
            preco = preco,
            marca = marca,
            quantidade = quantidade,
            tipo = tipo,
            codigo_barras = codigoBarras
        )

        if (codigoBarras.isEmpty() || codigoBarras.length < 13) {
            Toast.makeText(this, "Código de barras inválido", Toast.LENGTH_SHORT).show()
            return
        }

        api.atualizarProduto(produtoAtualizado).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    limparCampos()
                    desativarCampos()
                    Toast.makeText(this@EditarProdutosActivity, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EditarProdutosActivity, "Erro ao atualizar produto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditarProdutosActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletarProduto() {
        val codigoBarras = editCodigoBarras.text.toString().trim()

        if (codigoBarras.isEmpty() || codigoBarras.length < 13) {
            Toast.makeText(this, "Código de barras inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf("codigo_barras" to codigoBarras)

        api.deletarProduto(body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditarProdutosActivity, "Produto deletado com sucesso!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    desativarCampos()
                } else {
                    Toast.makeText(this@EditarProdutosActivity, "Erro ao deletar produto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditarProdutosActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun limparCampos() {
        editCodigoBarras.setText("")
        editDesc.setText("")
        editPreco.setText("")
        editMarca.setText("")
        editQtd.setText("")
        spinnerTipos.setSelection(0)
        btExcluir.alpha = 1.0f
        btAlterar.alpha = 1.0f
    }

    private fun desativarCampos() {
        btExcluir.alpha = 0.5f
        btAlterar.alpha = 0.5f
        editDesc.isEnabled = false
        editPreco.isEnabled = false
        editMarca.isEnabled = false
        editQtd.isEnabled = false
        spinnerTipos.isEnabled = false
        btAlterar.isEnabled = false
        btExcluir.isEnabled = false
    }

    private fun ativarCampos() {
        editDesc.isEnabled = true
        editPreco.isEnabled = true
        editMarca.isEnabled = true
        editQtd.isEnabled = true
        spinnerTipos.isEnabled = true
        btAlterar.isEnabled = true
        btExcluir.isEnabled = true
    }
}

