package com.halalin.vendor.fragment.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.halalin.R
import com.halalin.databinding.ItemVendorBinding
import com.halalin.vendor.model.Vendor

class VendorAdapter(
    private var itemClickListener: (vendor: Vendor) -> Unit
) : RecyclerView.Adapter<VendorAdapter.ViewHolder>() {
    private var vendorList: List<Vendor> = emptyList()

    class DiffUtilCallback(
        private val oldList: List<Vendor>,
        private val newList: List<Vendor>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList[oldItemPosition].name != newList[newItemPosition].name) return false
            if (oldList[oldItemPosition].rating != newList[newItemPosition].rating) return false
            return true
        }
    }

    fun updateVendorList(vendorList: List<Vendor>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.vendorList, vendorList))
        this.vendorList = vendorList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = vendorList.size

    private fun getVendorAt(position: Int) = vendorList[position]

    class ViewHolder(
        val binding: ItemVendorBinding,
        itemClickListener: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { itemClickListener(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVendorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) { itemClickListener(getVendorAt(it)) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vendor = getVendorAt(position)
        holder.binding.imageViewLogo.load(vendor.logoUrl)
        holder.binding.textViewName.text = vendor.name
        holder.binding.textViewLocation.text = vendor.location
        holder.binding.ratingBarRating.rating = vendor.rating!!
        holder.binding.textViewReviewNumber.text = holder.binding.root.context.getString(
            R.string.template_vendor_review_number,
            vendor.reviewNumber!!
        )
    }
}
