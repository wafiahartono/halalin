package com.halalin.vendor.product.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.cart.model.Item
import com.halalin.databinding.FragmentProductBinding
import com.halalin.util.*
import com.halalin.vendor.model.Vendor
import com.halalin.vendor.product.model.Product
import com.stfalcon.imageviewer.StfalconImageViewer

class ProductFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()
    private val args: ProductFragmentArgs by navArgs()

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchProductList(args.vendorId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.productList.observe(viewLifecycleOwner, {
            if (it != null) checkProductListResource(it)
        })
    }

    private fun setupViews() {
        binding.toolbar.title = args.vendorName

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_cart -> {
                    //TODO implementation
                    Snackbar.make(binding.root, "Not implemented", Snackbar.LENGTH_SHORT).show()
                }
            }
            return@setOnMenuItemClickListener true
        }

        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        binding.recyclerViewProduct.adapter =
            ProductAdapter { product, root ->
                if (root) {
                    StfalconImageViewer.Builder<String>(
                        requireContext(),
                        listOf(product.imageUrl)
                    ) { imageView, image -> imageView.load(image) }
                        .show()
                } else {
                    AddCartItemFragment(
                        Item(product = product, vendor = Vendor(id = args.vendorId))
                    ).show(requireActivity().supportFragmentManager, null)
                }
            }

        binding.recyclerViewProduct.addItemDecoration(
            MarginItemDecoration(
                MarginItemDecoration.Orientation.VERTICAL,
                1,
                resources.getDimensionPixelSize(R.dimen.default_recycler_view_item_spacing)
            )
        )

        binding.recyclerViewProduct.setHasFixedSize(true)
    }

    private fun checkProductListResource(resource: Resource<List<Product>>) {
        var contentVisibility = View.INVISIBLE
        when (resource) {
            is Resource.Failure ->
                binding.layoutState.setErrorState(
                    R.string.fragment_product_list_fetch_product_list_failure_message,
                    R.string.retry
                ) { viewModel.refreshFetchProductList() }
            is Resource.Loading ->
                binding.layoutState.setLoadingState()
            is Resource.Success ->
                if (resource.data.isEmpty()) {
                    binding.layoutState.setEmptyState(
                        R.string.fragment_product_list_fetch_product_list_empty_message,
                        R.string.refresh
                    ) { viewModel.refreshFetchProductList() }
                } else {
                    contentVisibility = View.VISIBLE
                    binding.layoutState.clearLayout()
                    (binding.recyclerViewProduct.adapter as ProductAdapter).updateProductList(
                        resource.data
                    )
                }
        }
        binding.constraintLayoutContent.visibility = contentVisibility
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
