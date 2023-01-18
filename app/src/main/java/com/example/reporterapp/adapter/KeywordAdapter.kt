package com.example.reporterapp.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.reporterapp.R
import android.widget.LinearLayout



class KeyAdapter : RecyclerView.Adapter<KeyAdapter.MyAdapter>() {
    
    var list = emptyList<String>()
    var isAttachment:Boolean = false
    lateinit var context: Context

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyAdapter {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.keywords_attachment, p0, false)
        return MyAdapter(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: MyAdapter, p1: Int) {
        var linearLayout = p0.itemView.findViewById<LinearLayout>(R.id.layout_keywords_atachment)
        var params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        if(isAttachment) {
            params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        linearLayout.layoutParams = params
        if(list[p1].isNotEmpty()){
            p0.itemView.findViewById<LinearLayout>(R.id.layout_keywords_atachment).background =
                ContextCompat.getDrawable(context, R.drawable.gray_rounded_corners_filled)
        }
        else{
            p0.itemView.findViewById<LinearLayout>(R.id.layout_keywords_atachment).background = null
        }
        p0.itemView.findViewById<TextView>(R.id.tvGetArticleKeyword).text=list[p1]
        p0.itemView.findViewById<ImageView>(R.id.ivClearKeywordAndAttachedFile).visibility=View.GONE
    }

    class MyAdapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    fun setData(articleList: List<String>, isAttachment: Boolean, context:Context) {
        this.list = articleList
        this.isAttachment = isAttachment
        this.context = context
        notifyDataSetChanged()
    }
}