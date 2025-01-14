package com.example.skins

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdapterSkins (private val dataList: ArrayList<Skins>): RecyclerView.Adapter<AdapterSkins.ViewHolderClass>() {


    // Метод для обновления данных
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<Skins>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged() // Уведомляем RecyclerView об изменениях
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        Glide.with(holder.rvImage.context)
            .load(currentItem.dataImage) // Здесь передаётся строка URL
            .placeholder(R.drawable.cssss) // Заглушка, пока изображение загружается
            .error(R.drawable.vnimanie) // Изображение при ошибке
            .into(holder.rvImage) // ImageView, куда загружается изображение
        holder.rvTitle.text = currentItem.dataTitle
        holder.rvLink.text = currentItem.dataType
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