package com.halalin.favorite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.halalin.R
import com.halalin.databinding.FragmentFavoriteBinding
import com.halalin.util.MarginItemDecoration
import com.halalin.util.clearLayout
import com.halalin.util.setEmptyState
import com.halalin.util.topLevelDestinationIdList
import com.halalin.vendor.fragment.search.VendorAdapter
import com.halalin.vendor.model.Vendor

class FavoriteFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.favoriteVendorList.observe(viewLifecycleOwner, { checkFavoriteVendorList(it) })
    }

    private fun setupViews() {
        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        binding.recyclerViewVendor.adapter = VendorAdapter {
            findNavController().navigate(
                FavoriteFragmentDirections.actionViewVendorDetail(it.id!!, it.name!!)
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

    private fun checkFavoriteVendorList(list: List<Vendor>) {
        var contentVisibility = View.INVISIBLE
        if (list.isEmpty()) {
            binding.layoutState.setEmptyState(
                R.string.fragment_favorite_fetch_favorite_vendor_list_empty_message
            )
        } else {
            contentVisibility = View.VISIBLE
            binding.layoutState.clearLayout()
            (binding.recyclerViewVendor.adapter as VendorAdapter).updateVendorList(list)
        }
        binding.constraintLayoutContent.visibility = contentVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
