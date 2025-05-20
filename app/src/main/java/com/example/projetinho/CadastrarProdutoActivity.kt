package com.example.projetinho

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CadastrarProdutoActivity : AppCompatActivity() {

    private lateinit var descEdit: EditText
    private lateinit var codigoBarrasEdit: EditText
    private lateinit var precoEdit: EditText
    private lateinit var marcaEdit: EditText
    private lateinit var quantidadeEdit: EditText
    private lateinit var tipoEdit: Spinner

    private val listaTipos = mutableListOf<SpinnerTiposResponse>()
    private lateinit var tiposAdapter: SpinnerTiposAdapter

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_produto)

        descEdit = findViewById(R.id.editDesc)
        codigoBarrasEdit = findViewById(R.id.editCodigoBarras)
        precoEdit = findViewById(R.id.editPreco)
        marcaEdit = findViewById(R.id.editMarca)
        quantidadeEdit = findViewById(R.id.editQtd)
        tipoEdit = findViewById(R.id.spinnerTipos)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.18.12/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        tiposAdapter = SpinnerTiposAdapter(this, listaTipos)
        tipoEdit.adapter = tiposAdapter

        carregarTipos()

        val cadastrarButton: Button = findViewById(R.id.btCadastro)
        cadastrarButton.setOnClickListener {
            blockCadastro()
        }

        findViewById<ImageButton>(R.id.btVoltar).setOnClickListener {
            voltar()
        }
    }

    private fun voltar() {
        val intent = Intent(this@CadastrarProdutoActivity, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun carregarTipos() {
        apiService.listarTiposProduto().enqueue(object : Callback<List<SpinnerTiposResponse>> {
            override fun onResponse(
                call: Call<List<SpinnerTiposResponse>>,
                response: Response<List<SpinnerTiposResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    listaTipos.clear()
                    listaTipos.addAll(response.body()!!)
                    tiposAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CadastrarProdutoActivity, "Erro ao carregar tipos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SpinnerTiposResponse>>, t: Throwable) {
                Toast.makeText(this@CadastrarProdutoActivity, "Falha: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun blockCadastro() {
        val descProduto = descEdit.text.toString()
        val codigoBarras = codigoBarrasEdit.text.toString().trim()
        val precoProduto = precoEdit.text.toString().toDoubleOrNull()
        val marcaProduto = marcaEdit.text.toString()
        val qtdProduto = quantidadeEdit.text.toString().toIntOrNull()
        val tipoSelecionado = tipoEdit.selectedItem as? SpinnerTiposResponse

        if (codigoBarras.length != 13 || !codigoBarras.all { it.isDigit() }) {
            Toast.makeText(this, "O código de barras deve ter exatamente 13 caracteres numéricos.", Toast.LENGTH_LONG).show()
            return
        }

        if (precoProduto == null || qtdProduto == null || tipoSelecionado == null) {
            Toast.makeText(this, "Preencha todos os campos corretamente.", Toast.LENGTH_LONG).show()
            return
        }

        val tipoProduto = tipoSelecionado.id

        val call = apiService.cadastro(descProduto, codigoBarras, precoProduto, marcaProduto, qtdProduto, tipoProduto)
        call.enqueue(object : Callback<CadastroResponse> {
            override fun onResponse(call: Call<CadastroResponse>, response: Response<CadastroResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@CadastrarProdutoActivity, "Produto cadastrado com sucesso", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@CadastrarProdutoActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("Cadastro", "Erro: ${response.errorBody()?.string()}")
                    Toast.makeText(this@CadastrarProdutoActivity, "Erro no cadastro", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CadastroResponse>, t: Throwable) {
                Toast.makeText(this@CadastrarProdutoActivity, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @GET("/meu_projeto_api/cadastro.php")
        fun cadastro(
            @Query("desc") desc: String,
            @Query("codigoBarra") codigoBarra: String,
            @Query("preco") preco: Double,
            @Query("marca") marca: String,
            @Query("quantidade") quantidade: Int,
            @Query("tipo") tipo: Int
        ): Call<CadastroResponse>

        @GET("/meu_projeto_api/buscar_tipos_produto_spinner.php")
        fun listarTiposProduto(): Call<List<SpinnerTiposResponse>>
    }
}