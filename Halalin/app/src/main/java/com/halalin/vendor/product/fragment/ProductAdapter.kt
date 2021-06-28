package com.halalin.vendor.product.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.halalin.databinding.ItemProductBinding
import com.halalin.vendor.product.model.Product
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private var productList: List<Product> = emptyList(),
    private val itemClickListener: (product: Product, root: Boolean) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    class DiffUtilCallback(
        private val oldList: List<Product>,
        private val newList: List<Product>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList[oldItemPosition].price != newList[newItemPosition].price) return false
            if (oldList[oldItemPosition].name != newList[newItemPosition].name) return false
            if (oldList[oldItemPosition].description != newList[newItemPosition].description) return false
            return true
        }
    }

    fun updateProductList(productList: List<Product>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.productList, productList))
        this.productList = productList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getProductAt(position: Int) = productList[position]

    override fun getItemCount(): Int = productList.size

    class ViewHolder(
        val binding: ItemProductBinding,
        itemClickListener: (position: Int, root: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemClickListener(adapterPosition, true)
            }
            binding.imageViewImage.setOnClickListener {
                itemClickListener(adapterPosition, false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) { position, root ->
        itemClickListener(getProductAt(position), root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getProductAt(position)
        holder.binding.imageViewImage.load(product.imageUrl)
        holder.binding.textViewPrice.text = NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(product.price)
        holder.binding.textViewName.text = product.name
        holder.binding.textViewDescription.text = product.description
    }
}
