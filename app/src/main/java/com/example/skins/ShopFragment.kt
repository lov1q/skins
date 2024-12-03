package com.example.skins

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShopFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим разметку фрагмента
        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        // Подготовка данных для адаптера
        val dataset = buildList()

        // Инициализация RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)

        // Устанавливаем GridLayoutManager с 2 столбцами
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager

        // Подключаем адаптер
        recyclerView.adapter = AdapterSkins(dataset)

        return view
    }

    private fun buildList(): ArrayList<Skins> {
        val list = arrayListOf<Skins>()
        list.add(Skins(
            R.drawable.tradeit,
            "Tradeitt",
            "https://tradeit.gg/ru/csgo/store",
            "sdfsdf"
        ))
        list.add(Skins(
            R.drawable.lisskins,
            "Lis-skins",
            "https://lis-skins.com/ru/market",
            "dadasd"
        ))

        return list
    }
}