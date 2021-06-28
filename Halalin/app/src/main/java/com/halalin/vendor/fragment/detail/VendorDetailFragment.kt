package com.halalin.vendor.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import coil.Coil
import coil.api.load
import coil.request.LoadRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.databinding.FragmentVendorDetailBinding
import com.halalin.databinding.ItemServiceChipBinding
import com.halalin.databinding.ItemVendorContactBinding
import com.halalin.util.*
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.review.model.Review

class VendorDetailFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()
    private val args: VendorDetailFragmentArgs by navArgs()

    private var _binding: FragmentVendorDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchVendor(args.vendorId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVendorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.vendor.observe(viewLifecycleOwner, { if (it != null) checkVendorResource(it) })
    }

    private fun setupViews() {
        binding.toolbar.title = args.vendorName

        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        if (viewModel.isGuest()) {
            binding.buttonFavorite.visibility = View.INVISIBLE
        } else {
            binding.buttonFavorite.setOnClickListener {
                viewModel.setFavorite(binding.buttonFavorite.isChecked)
                binding.buttonFavorite.isEnabled = false
            }

            viewModel.isFavorite.observe(viewLifecycleOwner, {
                if (it != null) {
                    var buttonFavoriteIsEnabled = false
                    when (it) {
                        is Resource.Success -> {
                            buttonFavoriteIsEnabled = true
                            binding.buttonFavorite.isChecked = it.data
                        }
                    }
                    binding.buttonFavorite.isEnabled = buttonFavoriteIsEnabled
                }
            })

            viewModel.setFavoriteResult.observe(viewLifecycleOwner,
                EventObserver {
                    val messageTextResId = when (it) {
                        is Resource.Failure ->
                            R.string.fragment_vendor_detail_set_favorite_failed_message
                        is Resource.Success ->
                            if (binding.buttonFavorite.isChecked) {
                                R.string.fragment_vendor_detail_added_to_favorite_message
                            } else {
                                R.string.fragment_vendor_detail_removed_from_favorite_message
                            }
                        else -> R.string.unexpected_error_message
                    }
                    binding.buttonFavorite.isEnabled = true
                    Snackbar.make(binding.root, messageTextResId, Snackbar.LENGTH_LONG).show()
                })
        }
    }

    private fun checkVendorResource(resource: Resource<Vendor?>) {
        var contentVisibility = View.INVISIBLE
        when (resource) {
            is Resource.Failure ->
                binding.layoutState.setErrorState(
                    R.string.fragment_vendor_detail_fetch_vendor_failure_message,
                    R.string.retry
                ) { viewModel.refreshFetchVendor() }
            is Resource.Loading ->
                binding.layoutState.setLoadingState()
            is Resource.Success -> {
                if (resource.data == null) {
                    binding.layoutState.setErrorState(
                        R.string.fragment_vendor_detail_fetch_vendor_not_found_message,
                        R.string.retry
                    ) { viewModel.refreshFetchVendor() }
                } else {
                    contentVisibility = View.VISIBLE
                    binding.layoutState.clearLayout()
                    updateVendorViews(resource.data)
                }
            }
        }
        binding.nestedScrollViewContent.visibility = contentVisibility
    }

    private fun updateVendorViews(vendor: Vendor) {
        binding.imageViewLogo.load(vendor.logoUrl)
        binding.textViewRating.text = getString(R.string.template_vendor_rating, vendor.rating)
        binding.ratingBarRating.rating = vendor.rating!!
        binding.textViewReviewNumber.text = getString(
            R.string.template_vendor_review_number, vendor.reviewNumber
        )
        binding.textViewName.text = vendor.name
        binding.textViewPriceRange.text = getString(R.string.price_range_symbol).repeat(
            when (vendor.priceRange) {
                Vendor.PriceRange.LOW -> 1
                Vendor.PriceRange.MEDIUM -> 2
                Vendor.PriceRange.HIGH -> 3
                else -> 1
            }
        )
        binding.textViewLocation.text = vendor.location
        binding.buttonProduct.setOnClickListener {
            findNavController().navigate(
                VendorDetailFragmentDirections.actionViewProductList(args.vendorId, args.vendorName)
            )
        }
        binding.buttonChat.setOnClickListener {
            Snackbar.make(binding.root, "Not implemented", Snackbar.LENGTH_LONG).show()
        }
        binding.textViewDescription.text = vendor.description
        vendor.serviceList!!.forEach {
            val chip = ItemServiceChipBinding
                .inflate(layoutInflater, binding.chipGroupService, true)
                .root
            Coil.imageLoader(requireContext()).execute(LoadRequest.Builder(requireContext())
                .allowHardware(false)
                .data(it.iconUrl)
                .target { drawable -> chip.chipIcon = drawable }
                .build()
            )
            chip.text = it.name
        }
        vendor.contacts!!.entries.forEachIndexed { i, it ->
            val button = ItemVendorContactBinding
                .inflate(layoutInflater, binding.linearLayoutContact, true).root
            when (it.key) {
                Vendor.Contact.FACEBOOK -> {
                    button.setIconResource(R.drawable.ic_facebook_24dp)
                    button.setText(R.string.contact_facebook)
                }
                Vendor.Contact.INSTAGRAM -> {
                    button.setIconResource(R.drawable.ic_instagram_24dp)
                    button.setText(R.string.contact_instagram)
                }
                Vendor.Contact.PHONE -> {
                    button.setIconResource(R.drawable.ic_call_24dp)
                    button.setText(R.string.contact_phone)
                }
                Vendor.Contact.WEBSITE -> {
                    button.setIconResource(R.drawable.ic_public_24dp)
                    button.setText(R.string.contact_website)
                }
                Vendor.Contact.WHATSAPP -> {
                    button.setIconResource(R.drawable.ic_whatsapp_24dp)
                    button.setText(R.string.contact_whatsapp)
                }
            }
            button.setOnClickListener { _ ->
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.fragment_vendor_detail_open_vendor_contact_dialog_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        startActivity(
                            Intent.createChooser(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Vendor.getContactUri(it.key, it.value)
                                ),
                                getString(R.string.fragment_vendor_detail_open_vendor_contact_intent_chooser_title)
                            )
                        )
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
            if (i != vendor.contacts.size - 1) {
                button.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(
                        right = resources.getDimensionPixelSize(R.dimen.item_contact_view_spacing)
                    )
                }
            }
        }

        if (vendor.latestReviewList == null) {
            binding.textViewSectionReview.visibility = View.GONE
            binding.recyclerViewReview.visibility = View.GONE
            binding.buttonShowAllReview.visibility = View.GONE
        } else {
            binding.recyclerViewReview.adapter = ReviewAdapter(vendor.latestReviewList)
            binding.recyclerViewReview.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
            binding.recyclerViewReview.addItemDecoration(
                MarginItemDecoration(
                    MarginItemDecoration.Orientation.VERTICAL,
                    1,
                    resources.getDimensionPixelSize(R.dimen.default_recycler_view_item_spacing)
                )
            )

            if (vendor.reviewNumber!! > vendor.latestReviewList.size) {
                binding.buttonShowAllReview.setOnClickListener { viewModel.fetchReviewList() }
            } else {
                binding.buttonShowAllReview.visibility = View.GONE
            }

            viewModel.reviewList.observe(viewLifecycleOwner, {
                if (it != null) checkReviewListResource(it)
            })
        }
    }

    private fun checkReviewListResource(resource: Resource<List<Review>>) {
        var buttonShowAllReviewIsEnabled = false
        var buttonShowAllReviewTextResId = R.string.show_all_review
        when (resource) {
            is Resource.Failure -> {
                buttonShowAllReviewIsEnabled = true
                Snackbar
                    .make(
                        binding.root,
                        R.string.fragment_vendor_detail_show_all_review_failure_message,
                        Snackbar.LENGTH_LONG
                    )
                    .setAction(R.string.retry) { viewModel.fetchReviewList() }
                    .show()
            }
            is Resource.Loading ->
                buttonShowAllReviewTextResId = R.string.loading
            is Resource.Success -> {
                (binding.recyclerViewReview.adapter as ReviewAdapter).updateReviewList(resource.data)
                binding.buttonShowAllReview.visibility = View.GONE
            }
        }
        binding.buttonShowAllReview.isEnabled = buttonShowAllReviewIsEnabled
        binding.buttonShowAllReview.setText(buttonShowAllReviewTextResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearResources()
    }
}
