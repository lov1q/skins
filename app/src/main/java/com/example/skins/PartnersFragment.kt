package com.example.skins

import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PartnersFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфлейтим разметку фрагмента
        val view = inflater.inflate(R.layout.fragment_partners, container, false)

        // Подготовка данных для адаптера
        val dataset = buildList()

        // Инициализация RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AdapterClass(dataset)

        return view
    }

    private fun buildList(): ArrayList<DataClass> {
        val list = arrayListOf<DataClass>()
        list.add(DataClass(
            R.drawable.tradeit,
            "Tradeitt",
            "https://tradeit.gg/ru/csgo/store"
        ))
        list.add(DataClass(
            R.drawable.lisskins,
            "Lis-skins",
            "https://lis-skins.com/ru/market"
        ))
        list.add(DataClass(
            R.drawable.csmoney,
            "CS.MONEY",
            "https://tradeit.gg/ru/csgo/store"
        ))
        list.add(DataClass(
            R.drawable.gg2,
            "G2",
            "https://g2esports.com/"
        ))
        list.add(DataClass(
            R.drawable.cccloud,
            "Cloud9",
            "https://cloud9.gg/"
        ))
        return list
    }
}