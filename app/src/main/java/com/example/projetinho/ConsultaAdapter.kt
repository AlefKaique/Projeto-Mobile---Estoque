package com.example.projetinho

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConsultaAdapter(
    private val listaProdutos: MutableList<ConsultaResponse>,
    private val onItemRemover: (Int) -> Unit
) : RecyclerView.Adapter<ConsultaAdapter.ConsultaViewHolder>() {

    inner class ConsultaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.tvNome)
        val codigo: TextView = view.findViewById(R.id.tvCodigo)
        val quantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val botaoRemover: ImageButton = view.findViewById(R.id.buttonRemover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto_consulta, parent, false) // Seu XML de item
        return ConsultaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        val produto = listaProdutos[position]
        holder.nome.text = produto.nome
        holder.codigo.text = produto.codigo_barras
        holder.quantidade.text = produto.quantidade.toString()

        holder.botaoRemover.setOnClickListener {
            onItemRemover(position)
        }
    }

    override fun getItemCount(): Int = listaProdutos.size
}

