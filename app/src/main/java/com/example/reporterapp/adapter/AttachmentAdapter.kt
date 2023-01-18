package com.example.reporterapp.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.reporterapp.R
import com.example.reporterapp.callback.RecyclerViewListener
import com.example.reporterapp.getFileName

class AttachmentAdapter(private val recyclerViewListener: RecyclerViewListener) : RecyclerView.Adapter<AttachmentAdapter.AttachmentHolder>(){

    var attachmentList= listOf<String>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AttachmentHolder {
        val v= LayoutInflater.from(p0.context).inflate(R.layout.attachment,p0,false)
        return AttachmentHolder(v)
    }

    override fun getItemCount(): Int {
        return attachmentList.size
    }

    override fun onBindViewHolder(p0: AttachmentHolder, p1: Int) {
        p0.itemView.findViewById<TextView>(R.id.textView).text= getFileName(attachmentList[p1])
        p0.itemView.findViewById<ImageView>(R.id.imageView).setOnClickListener {
            recyclerViewListener.recyclerViewListClicked(it, p1, -1)
        }
    }

    fun setAttachment(list: List<String>){
        this.attachmentList=list.reversed()
        notifyDataSetChanged()
    }

    class AttachmentHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}