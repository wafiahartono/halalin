package com.halalin.home.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.halalin.databinding.ItemServiceBinding
import com.halalin.service.model.Service

class ServiceAdapter(
    private val itemClickListener: (service: Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {
    private var serviceList: List<Service> = emptyList()

    class DiffUtilCallback(
        private val oldList: List<Service>,
        private val newList: List<Service>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].name == newList[newItemPosition].name
        }
    }

    fun updateServiceList(serviceList: List<Service>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.serviceList, serviceList))
        this.serviceList = serviceList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = serviceList.size

    private fun getServiceAt(position: Int) = serviceList[position]

    class ViewHolder(
        val binding: ItemServiceBinding,
        itemClickListener: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { itemClickListener(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) { itemClickListener(getServiceAt(it)) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = getServiceAt(position)
        holder.binding.imageViewImage.load(service.imageUrl)
        holder.binding.imageViewIcon.load(service.iconUrl)
        holder.binding.textViewName.text = service.name
    }
}
