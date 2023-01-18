package com.example.reporterapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.reporterapp.R
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.SubCategory

class SubCategorySpinnerAdapter(private val context: Context) : BaseAdapter() {

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var subCategory = emptyList<SubCategory>()

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

        vh.label.text = subCategory[position].subCatName
        return view
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return subCategory.size
    }

    internal fun setSubCategory(subCategories: List<SubCategory>) {
        this.subCategory = subCategories
        notifyDataSetChanged()
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView = row?.findViewById(R.id.txtDropDownLabel) as TextView
    }
}