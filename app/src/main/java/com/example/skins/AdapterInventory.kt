package com.example.skins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skins.AdapterSkins.ViewHolderClass

class AdapterInventory (private val dataList: ArrayList<YourinventClass>): RecyclerView.Adapter<AdapterInventory.ViewHolderClass>() {

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.rvImage.setImageResource(currentItem.dataImage)
        holder.rvTitle.text = currentItem.dataTitle
        holder.rvLink.text = currentItem.dataExterior
        holder.rvCost.text = currentItem.dataCost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skins, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvImage: ImageView = itemView.findViewById(R.id.image)
        val rvTitle: TextView = itemView.findViewById(R.id.title)
        val rvLink: TextView = itemView.findViewById(R.id.exterior)
        val rvCost: TextView = itemView.findViewById(R.id.cost)
    }
}