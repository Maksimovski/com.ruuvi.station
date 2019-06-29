package com.ruuvi.station.feature.addtag

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ruuvi.station.R
import com.ruuvi.station.databinding.ItemAddTagBinding
import com.ruuvi.station.model.RuuviTag

class AddTagAdapter(var context: Context, val onItemClickListener: (RuuviTag) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {
    private var tagList: MutableList<RuuviTag> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_add_tag, parent, false))
    }

    override fun getItemCount(): Int = tagList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        bindItem(holder, tagList[position])
    }

    private fun bindItem(holder: ItemViewHolder, tag: RuuviTag) {
        holder.binder?.address?.text = tag.id
        holder.binder?.rssi?.text = String.format(context.resources.getString(R.string.signal_reading), tag.rssi)
        when {
            tag.rssi < -80 -> holder.binder?.signalIcon?.setImageResource(R.drawable.icon_connection_1)
            tag.rssi < -50 -> holder.binder?.signalIcon?.setImageResource(R.drawable.icon_connection_2)
            else -> holder.binder?.signalIcon?.setImageResource(R.drawable.icon_connection_3)
        }
        holder.binder?.root?.setOnClickListener {
            tag.defaultBackground = getKindaRandomBackground()
            tag.update()
            onItemClickListener(tag)
        }
    }

    fun addTagList(tags: List<RuuviTag>) {
        tags.forEach {
            Log.i(AddTagActivity.TAG, "${it.id}")
        }

        tagList.clear()
        tagList.addAll(tags)
        notifyDataSetChanged()
    }

    fun addTag(tag: RuuviTag) {
        tagList.add(tag)
        notifyDataSetChanged()
    }

    fun removeTag(tag: RuuviTag) {
        tagList.remove(tag)
        notifyDataSetChanged()
    }

    private fun isBackgroundInUse(tags: List<RuuviTag>, background: Int): Boolean {
        tags.forEach { tag ->
            if (tag.defaultBackground == background) return true
        }
        return false
    }

    private fun getKindaRandomBackground(): Int {
        var bg = (Math.random() * 9.0).toInt()
        for (i in 0..99) {
            if (!isBackgroundInUse(tagList, bg)) {
                return bg
            }
            bg = (Math.random() * 9.0).toInt()
        }
        return bg
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var binder: ItemAddTagBinding? = DataBindingUtil.bind<ItemAddTagBinding>(itemView)
}