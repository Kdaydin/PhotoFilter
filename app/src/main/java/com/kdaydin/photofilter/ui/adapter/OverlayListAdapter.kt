package com.kdaydin.photofilter.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kdaydin.photofilter.data.entities.Overlay
import com.kdaydin.photofilter.databinding.ItemOverlayBinding
import com.kdaydin.photofilter.ui.listener.OverlaySelectionListener

class OverlayListAdapter(
    val overlays: List<Overlay>,
    val listener: OverlaySelectionListener
) :
    RecyclerView.Adapter<OverlayListAdapter.OverlayViewHolder>() {

    var selectedOverlayId: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        val binding = ItemOverlayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OverlayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {
        holder.bind(overlays[position])
    }

    override fun getItemCount(): Int = overlays.size

    inner class OverlayViewHolder(val binding: ItemOverlayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Overlay) {
            with(binding) {
                this.overlay = item
                this.clickListener = listener
                executePendingBindings()
                this.root.setOnClickListener {
                    this@OverlayListAdapter.selectedOverlayId = item.overlayId ?: 0
                    this.clickListener?.onOverlaySelected(item)
                    notifyDataSetChanged()
                }
                this.root.isSelected = item.overlayId == selectedOverlayId
            }
        }
    }
}