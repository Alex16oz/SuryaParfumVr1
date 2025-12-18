package com.surya.parfum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class KomplainAdapter(
    private val listKomplain: List<Komplain>
) : RecyclerView.Adapter<KomplainAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvOrder: TextView = itemView.findViewById(R.id.tvOrder)
        val tvAlasan: TextView = itemView.findViewById(R.id.tvAlasan)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val imgBukti: ImageView = itemView.findViewById(R.id.imgBukti)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_komplain, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val komplain = listKomplain[position]

        holder.tvNama.text = komplain.nama
        holder.tvOrder.text = "No Order: ${komplain.noOrder}"
        holder.tvAlasan.text = komplain.alasan
        holder.tvStatus.text = "Status: ${komplain.status}"

        if (komplain.fotoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(komplain.fotoUrl)
                .into(holder.imgBukti)
        } else {
            holder.imgBukti.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    override fun getItemCount(): Int = listKomplain.size
}
