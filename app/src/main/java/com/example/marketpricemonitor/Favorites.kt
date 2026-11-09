package com.example.marketpricemonitor

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Favorites {
    private const val PREFS = "favorites_prefs"
    private const val KEY = "favorites_list"

    fun load(context: Context): MutableList<Product> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Product>>() {}.type
        return Gson().fromJson(json, type) ?: mutableListOf()
    }

    fun save(context: Context, items: List<Product>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, Gson().toJson(items)).apply()
    }

    fun toggle(context: Context, product: Product): Boolean {
        val items = load(context)
        val existing = items.find { it.id == product.id }
        return if (existing != null) {
            items.remove(existing)
            save(context, items)
            false
        } else {
            items.add(product)
            save(context, items)
            true
        }
    }

    fun isFavorite(context: Context, id: Long): Boolean {
        return load(context).any { it.id == id }
    }
}