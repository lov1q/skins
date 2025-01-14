package com.example.skins

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ShopFragment : Fragment() {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var autoCompleteTextView1: AutoCompleteTextView
    private lateinit var autoCompleteTextView2: AutoCompleteTextView
    private lateinit var autoCompleteTextView3: AutoCompleteTextView
    private lateinit var adapterType: ArrayAdapter<String>
    private lateinit var adapterExteriors: ArrayAdapter<String>
    private lateinit var adapterQualitys: ArrayAdapter<String>
    private lateinit var adapterGuns: ArrayAdapter<String>
    private var isLoading = false
    private lateinit var skinsAdapter: AdapterSkins
    private val originalSkinsList = arrayListOf<Skins>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleView)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager
        skinsAdapter = AdapterSkins(originalSkinsList)
        recyclerView.adapter = skinsAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val column = position % 2
                outRect.left = spacing - column * spacing / 2
                outRect.right = (column + 1) * spacing / 2
                if (position < 2) outRect.top = spacing
                outRect.bottom = spacing
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 1) {
                    loadSkins()
                }
            }
        })

        loadSkins() // Загрузка первой страницы
        return view
    }

    fun getSkinUrls(): List<String> {
        val counts = listOf(500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500) // Количество скинов для каждой ссылки
        val starts = listOf(0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000) // Позиции начала для каждой ссылки
        //val baseUrl = "https://steamcommunity.com/market/search/render/?search_descriptions=0&sort_column=default&sort_dir=desc&appid=730&norender=1"
        val baseUrl = "https://steamcommunity.com/market/search/render/?start=$starts&count=$counts&appid=730&norender=1"


        val urls = mutableListOf<String>()
        for (i in counts.indices) {
            val url = "$baseUrl&count=${counts[i]}&start=${starts[i]}"
            urls.add(url)
        }
        return urls
    }


    private fun loadSkins() {
        isLoading = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urls = getSkinUrls() // Получаем список всех URL-ов
                val allSkins = mutableListOf<Skins>()

                for (url in urls) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        Log.d("ShopFragment", "Response: $response")

                        // Парсим полученные данные
                        val skins = parseSkinsFromResponse(response)

                        // Добавляем в общий список
                        allSkins.addAll(skins)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                        return@launch // Прерываем выполнение, если ошибка загрузки
                    }
                }

                withContext(Dispatchers.Main) {
                    // Обновляем адаптер с новыми данными
                    originalSkinsList.addAll(allSkins)
                    skinsAdapter.notifyDataSetChanged()
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ShopFragment", "Ошибка: $e")
                    Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            }
        }
    }


    private fun extractType(assetDescription: JSONObject): String {
        return assetDescription.optString("type", "Unknown")
    }

    private fun parseSkinsFromResponse(response: String): List<Skins> {
        val skins = arrayListOf<Skins>()
        val jsonObject = JSONObject(response)
        val startUrl = "https://community.cloudflare.steamstatic.com/economy/image/"
        val results = jsonObject.getJSONArray("results")
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val assetDescription = item.optJSONObject("asset_description") ?: continue

            val title = item.optString("name", "Unknown Item")
            val exterior = extractType(assetDescription)
            val cost = item.optString("sale_price_text", "N/A")
            val image = assetDescription.optString("icon_url","N/A")
            val fullimage = "$startUrl$image"
            skins.add(Skins(fullimage, title, exterior, cost))
        }
        return skins
    }

    private fun searchShop(){

    }

    private fun filterSkinsByType(selectedType: String) {
        // Если выбрано "Все категории", показываем весь список скинов
        if (selectedType == "All") {
            skinsAdapter.updateList(originalSkinsList) // Показываем весь список
            return
        }

        // Фильтруем список скинов по ключевому слову, которое выбрал пользователь
        val filteredList = originalSkinsList.filter { skin ->
            // Ищем выбранное ключевое слово в строке типа скина
            val skinType = skin.dataType

            // Проверяем, содержится ли выбранное ключевое слово в строке типа скина (не зависит от позиции)
            skinType.contains(selectedType, ignoreCase = true) // игнорируем регистр
        }

        // Обновляем адаптер с отфильтрованными данными
        skinsAdapter.updateList(ArrayList(filteredList)) // Здесь мы обновляем список, а не добавляем элементы
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeItems = resources.getStringArray(R.array.dropdown_items)
        val gunsItems = resources.getStringArray(R.array.cs2_item_list)
        val exteriorItems = resources.getStringArray(R.array.item_exteriors)
        val qualityItems = resources.getStringArray(R.array.item_quality)
        autoCompleteTextView = view.findViewById(R.id.collection1)
        autoCompleteTextView1 = view.findViewById(R.id.gun1)
        autoCompleteTextView2 = view.findViewById(R.id.exterior1)
        autoCompleteTextView3 = view.findViewById(R.id.quality1)
        adapterType = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            typeItems
        )
        adapterGuns = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            gunsItems
        )
        adapterExteriors = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            exteriorItems
        )
        adapterQualitys = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            qualityItems
        )
        autoCompleteTextView.setAdapter(adapterType)
        autoCompleteTextView1.setAdapter(adapterGuns)
        autoCompleteTextView2.setAdapter(adapterExteriors)
        autoCompleteTextView3.setAdapter(adapterQualitys)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedType = typeItems[position] // Получаем выбранный тип
            filterSkinsByType(selectedType) // Вызываем фильтрацию
        }
    }
}