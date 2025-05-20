package com.example.projetinho

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerTiposAdapter(
    context: Context,
    private val tipos: List<SpinnerTiposResponse>
) : ArrayAdapter<SpinnerTiposResponse>(context, android.R.layout.simple_spinner_item, tipos) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    // Texto no item selecionado do spinner
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        val tipo = tipos[position]
        view.text = "${tipo.id} - ${tipo.descricao}"
        return view
    }

    // Texto em cada item da lista dropdown
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        val tipo = tipos[position]
        view.text = "${tipo.id} - ${tipo.descricao}"
        return view
    }
}

