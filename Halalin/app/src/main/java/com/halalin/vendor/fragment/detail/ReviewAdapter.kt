package com.halalin.vendor.fragment.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.halalin.databinding.ItemReviewBinding
import com.halalin.vendor.review.model.Review

class ReviewAdapter(
    private var reviewList: List<Review> = emptyList()
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    class DiffUtilCallback(
        private val oldList: List<Review>,
        private val newList: List<Review>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList[oldItemPosition].rating != newList[newItemPosition].rating) return false
            if (oldList[oldItemPosition].date != newList[newItemPosition].date) return false
            if (oldList[oldItemPosition].comment != newList[newItemPosition].comment) return false
            return true
        }
    }

    fun updateReviewList(reviewList: List<Review>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(this.reviewList, reviewList))
        this.reviewList = reviewList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = reviewList.size

    private fun getReviewAt(position: Int) = reviewList[position]

    class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = getReviewAt(position)
        holder.binding.imageViewUserProfilePicture.load(review.user!!.profilePictureUrl) {
            transformations(CircleCropTransformation())
        }
        holder.binding.textViewUserDisplayName.text = review.user.displayName
        holder.binding.ratingBarRating.rating = review.rating!!.toFloat()
        holder.binding.textViewDate.text = review.date.toString()
        holder.binding.textViewComment.text = review.comment
    }
}
