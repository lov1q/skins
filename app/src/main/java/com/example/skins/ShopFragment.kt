package com.example.skins

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShopFragment : Fragment() {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapterItems: ArrayAdapter<String>



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
        // Добавляем отступы между элементами
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing) // Например, 16dp
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view) // позиция элемента
                val column = position % 2 // столбец (0 или 1)

                val includeEdge = true
                if (includeEdge) {
                    outRect.left = spacing - column * spacing / 2
                    outRect.right = (column + 1) * spacing / 2
                    if (position < 2) { // верхний ряд
                        outRect.top = spacing
                    }
                    outRect.bottom = spacing // отступ снизу
                } else {
                    outRect.left = column * spacing / 2
                    outRect.right = spacing - (column + 1) * spacing / 2
                    if (position >= 2) {
                        outRect.top = spacing // отступ сверху
                    }
                }
            }
        })
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Данные для AutoCompleteTextView
        val items = arrayOf("Material", "Design", "Components", "Android", "5.0 Lollipop")
        // Инициализируем AutoCompleteTextView
        autoCompleteTextView = view.findViewById(R.id.collection2)
        // Создаем адаптер
        adapterItems = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)
        // Привязываем адаптер к AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapterItems)
    }
}