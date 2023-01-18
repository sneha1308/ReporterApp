package com.example.reporterapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.reporterapp.R
import com.example.reporterapp.database.table.Category

class CategorySpinnerAdapter(private val context: Context) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var category = emptyList<Category>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.view_drop_down_menu, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        // setting adapter item height programatically.

        val params = view.layoutParams
        params.height = 60
        view.layoutParams = params

        vh.label.text = category[position].catName
        return view
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return category.size
    }

    internal fun setCategory(categories: List<Category>) {
        this.category = categories
        notifyDataSetChanged()
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView = row?.findViewById(R.id.txtDropDownLabel) as TextView
    }
}