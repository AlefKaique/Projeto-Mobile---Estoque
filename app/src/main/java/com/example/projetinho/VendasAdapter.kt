package com.example.projetinho

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VendasAdapter(
    private val listaItens: MutableList<VendasResponse>,
    private val onItemRemover: (Int) -> Unit
) : RecyclerView.Adapter<VendasAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.tvNome)
        val codigo: TextView = view.findViewById(R.id.tvCodigo)
        val preco: TextView = view.findViewById(R.id.tvPreco)           // novo
        val quantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val botaoRemover: ImageButton = view.findViewById(R.id.buttonRemover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = listaItens[position]
        holder.nome.text = item.nome
        holder.codigo.text = item.codigo_barras
        holder.preco.text = String.format("R$ %.2f", item.preco)       // setando preço
        holder.quantidade.text = item.quantidade.toString()

        holder.botaoRemover.setOnClickListener {
            onItemRemover(position)
        }
    }

    override fun getItemCount(): Int = listaItens.size
}
