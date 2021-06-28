package com.halalin.vendor.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.halalin.R
import com.halalin.databinding.FragmentVendorSearchBinding
import com.halalin.util.*
import com.halalin.vendor.fragment.search.filter.SearchFilterFragment
import com.halalin.vendor.model.SearchFilter
import com.halalin.vendor.model.Vendor

class VendorSearchFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()
    private val args: VendorSearchFragmentArgs by navArgs()

    private var _binding: FragmentVendorSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.searchVendor(
            SearchFilter(
                if (args.serviceIdFilter.isEmpty()) null else args.serviceIdFilter, null
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVendorSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.vendorList.observe(viewLifecycleOwner, { checkVendorListResource(it) })
    }

    private fun setupViews() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_filter -> SearchFilterFragment()
                    .show(requireActivity().supportFragmentManager, null)
            }
            return@setOnMenuItemClickListener true
        }

        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        binding.recyclerViewVendor.adapter = VendorAdapter {
            findNavController().navigate(
                VendorSearchFragmentDirections.actionViewVendorDetail(it.id!!, it.name!!)
            )
        }

        binding.recyclerViewVendor.addItemDecoration(
            MarginItemDecoration(
                MarginItemDecoration.Orientation.VERTICAL,
                2,
                resources.getDimensionPixelSize(R.dimen.default_recycler_view_item_spacing)
            )
        )

        binding.recyclerViewVendor.setHasFixedSize(true)
    }

    private fun checkVendorListResource(resource: Resource<List<Vendor>>) {
        var contentVisibility = View.INVISIBLE
        when (resource) {
            is Resource.Failure ->
                binding.layoutState.setErrorState(
                    R.string.fragment_vendor_search_fetch_vendor_list_failure_message,
                    R.string.retry
                ) { viewModel.refreshSearchVendor() }
            is Resource.Loading ->
                binding.layoutState.setLoadingState()
            is Resource.Success ->
                if (resource.data.isEmpty()) {
                    binding.layoutState.setEmptyState(
                        R.string.fragment_vendor_search_fetch_vendor_list_empty_message,
                        R.string.refresh
                    ) { viewModel.refreshSearchVendor() }
                } else {
                    contentVisibility = View.VISIBLE
                    binding.layoutState.clearLayout()
                    (binding.recyclerViewVendor.adapter as VendorAdapter).updateVendorList(resource.data)
                }
        }
        binding.constraintLayoutContent.visibility = contentVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
