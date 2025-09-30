package com.songajae.amgi.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.songajae.amgi.R
import com.songajae.amgi.data.packs.ContentPack

class PackAdapter : ListAdapter<ContentPack, PackAdapter.PackViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pack, parent, false)
        return PackViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val description: TextView = view.findViewById(R.id.tvDescription)
        private val chapters: TextView = view.findViewById(R.id.tvChapters)

        fun bind(item: ContentPack) {
            title.text = item.title
            description.text = item.description.ifBlank { "설명이 제공되지 않았어요." }
            chapters.text = itemView.context.getString(R.string.pack_chapter_count, item.chapterCount)
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ContentPack>() {
            override fun areItemsTheSame(oldItem: ContentPack, newItem: ContentPack): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ContentPack, newItem: ContentPack): Boolean = oldItem == newItem
        }
    }
}