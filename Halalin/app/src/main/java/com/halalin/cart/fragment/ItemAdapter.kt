package com.halalin.cart.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.halalin.cart.model.Item
import com.halalin.databinding.ItemCartItemBinding
import java.text.NumberFormat
import java.util.*

class ItemAdapter(
    private val itemUpdateListener: (item: Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    private var itemList: MutableList<Item> = mutableListOf()

    class DiffUtilCallback(
        private val oldList: List<Item>,
        private val newList: List<Item>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList[oldItemPosition].quantity != newList[newItemPosition].quantity) return false
            if (oldList[oldItemPosition].product!!.id != newList[newItemPosition].product!!.id) return false
            if (oldList[oldItemPosition].product!!.name != newList[newItemPosition].product!!.name) return false
            if (oldList[oldItemPosition].product!!.price != newList[newItemPosition].product!!.price) return false
            if (oldList[oldItemPosition].vendor!!.id != newList[newItemPosition].vendor!!.id) return false
            if (oldList[oldItemPosition].vendor!!.name != newList[newItemPosition].vendor!!.name) return false
            return true
        }
    }

    fun updateItemList(itemList: List<Item>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.itemList, itemList))
        this.itemList = itemList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateItemQuantity(itemId: String, quantity: Int) {
        for (i in itemList.indices) {
            if (itemList[i].id == itemId) {
                itemList[i] = itemList[i].copy(quantity = quantity)
                //TODO test with no payload
                notifyItemChanged(i, quantity)
                break
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun getItemAt(position: Int) = itemList[position]

    class ViewHolder(
        val binding: ItemCartItemBinding,
        private val itemQuantityUpdateListener: (position: Int, increment: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.textInputLayoutQuantity.setStartIconOnClickListener {
                itemQuantityUpdateListener(adapterPosition, false)
            }
            binding.textInputLayoutQuantity.setEndIconOnClickListener {
                itemQuantityUpdateListener(adapterPosition, true)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) { position, increment ->
        val item = getItemAt(position)
        itemUpdateListener(item.copy(quantity = item.quantity!! + if (increment) 1 else -1))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItemAt(position)
        holder.binding.imageViewProductImage.load(item.product!!.imageUrl)
        holder.binding.textViewProductName.text = item.product.name
        holder.binding.textViewVendorName.text = item.vendor!!.name
        holder.binding.textViewSubtotal.text = NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(item.product.price!! * item.quantity!!)
        holder.binding.editTextQuantity.setText(item.quantity.toString())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] is Int) {
            val item = getItemAt(position)
            holder.binding.textViewSubtotal.text = NumberFormat
                .getCurrencyInstance(Locale("id", "ID"))
                .format(item.product!!.price!! * item.quantity!!)
            holder.binding.editTextQuantity.setText(item.quantity.toString())
        } else {
            onBindViewHolder(holder, position)
        }
    }
}
