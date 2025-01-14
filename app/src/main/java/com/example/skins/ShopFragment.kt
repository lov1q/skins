package com.example.skins

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
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
                    recyclerView.post { // Используем post для отложенного выполнения
                        loadSkins()
                    }
                }
            }
        })

        val textInputLayout: TextInputLayout = view.findViewById(R.id.textField)
        val textInputEditText: TextInputEditText = textInputLayout.editText as TextInputEditText

        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { searchSkinsByName(it.toString()) } // Вызываем поиск при каждом изменении текста
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadSkins()
        return view
    }

    private var cachedSkinUrls: List<String>? = null

    private fun getSkinUrls(): List<String> {
        if (cachedSkinUrls == null) {
            cachedSkinUrls = (0..5000 step 500).map { start ->
                "https://steamcommunity.com/market/search/render/?start=$start&count=500&appid=730&norender=1"
            }
            Log.d("ShopFragment", "Список URL создан: $cachedSkinUrls")
        }
        return cachedSkinUrls!!
    }




    private fun loadSkins() {
        val cachedSkins = loadSkinsFromPreferences()

        if (cachedSkins != null) {
            Log.d("ShopFragment", "Загружаем данные из SharedPreferences")
            originalSkinsList.addAll(cachedSkins)
            skinsAdapter.notifyDataSetChanged()
            return
        }

        Log.d("ShopFragment", "Данные не найдены, выполняем запросы")
        isLoading = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urls = getSkinUrls()
                val deferredSkins = urls.mapIndexed { index, url ->
                    async {
                        if (index > 0) delay(7000)
                        loadSkinData(url)
                    }
                }
                val allSkins = deferredSkins.awaitAll().flatten()

                withContext(Dispatchers.Main) {
                    originalSkinsList.addAll(allSkins)
                    saveSkinsToPreferences(allSkins)
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




    private fun loadSkinData(url: String): List<Skins> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("ShopFragment", "Response from $url: $response")
            parseSkinsFromResponse(response)
        } else {
            Log.e("ShopFragment", "Error fetching data from $url. Response code: ${connection.responseCode}")
            emptyList()
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

    private fun searchSkinsByName(query: String) {
        // Если запрос пустой, показываем весь список
        if (query.isEmpty()) {
            skinsAdapter.updateList(originalSkinsList) // Показываем весь список
            return
        }

        // Фильтруем список по названию
        val filteredList = originalSkinsList.filter { skin ->
            skin.dataTitle.contains(query, ignoreCase = true) // Игнорируем регистр
        }

        // Обновляем адаптер с отфильтрованными данными
        skinsAdapter.updateList(ArrayList(filteredList))
    }


    private fun filterSkinsByType(selectedType: String) {
        if (selectedType == "All") {
            skinsAdapter.updateList(originalSkinsList)
            return
        }
        val filteredList = originalSkinsList.filter { skin ->
            val skinType = skin.dataType
            skinType.contains(selectedType, ignoreCase = true)
        }
        skinsAdapter.updateList(ArrayList(filteredList))
    }

    private fun filterSkinsByExterior(selectedExterior: String) {
        if (selectedExterior == "None") {
            skinsAdapter.updateList(originalSkinsList)
            return
        }
        val filteredList = originalSkinsList.filter { skin ->
            val skinExterior = skin.dataTitle
            skinExterior.contains(selectedExterior, ignoreCase = true)
        }
        Log.e("ggg", filteredList.toString())
        skinsAdapter.updateList(ArrayList(filteredList))
    }

    private fun filterSkinsByGun(selectedGun: String) {
        if (selectedGun == "None") {
            skinsAdapter.updateList(originalSkinsList)
            return
        }
        val filteredList = originalSkinsList.filter { skin ->
            val skinGun = skin.dataTitle
            skinGun.contains(selectedGun, ignoreCase = true)
        }
        Log.e("ggg", filteredList.toString())
        skinsAdapter.updateList(ArrayList(filteredList))
    }

    private fun filterSkinsByQualitys(selectedQuality: String) {
        if (selectedQuality == "All") {
            skinsAdapter.updateList(originalSkinsList)
            return
        }
        val filteredList = originalSkinsList.filter { skin ->
            val skinQuality = skin.dataType

            skinQuality.contains(selectedQuality, ignoreCase = true)
        }
        Log.e("ggg", filteredList.toString())
        skinsAdapter.updateList(ArrayList(filteredList))
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
            val selectedType = typeItems[position]
            filterSkinsByType(selectedType)
        }
        autoCompleteTextView2.setOnItemClickListener { _, _, position, _ ->
            val selectedExterior = exteriorItems[position]
            filterSkinsByExterior(selectedExterior)
        }
        autoCompleteTextView1.setOnItemClickListener { _, _, position, _ ->
            val selectedGun = gunsItems[position]
            filterSkinsByGun(selectedGun)
        }
        autoCompleteTextView3.setOnItemClickListener { _, _, position, _ ->
            val selectedQuality = qualityItems[position]
            filterSkinsByQualitys(selectedQuality)
        }
    }

    private fun saveSkinsToPreferences(skins: List<Skins>) {
        val sharedPreferences = requireContext().getSharedPreferences("skins_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(skins)
        editor.putString("skins_list", json)
        editor.apply()
    }

    private fun loadSkinsFromPreferences(): List<Skins>? {
        val sharedPreferences = requireContext().getSharedPreferences("skins_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("skins_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<Skins>>() {}.type
            Gson().fromJson(json, type)
        } else {
            null
        }
    }

    private fun clearSkinsFromPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("skins_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("skins_list").apply()
    }

}