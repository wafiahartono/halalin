package com.halalin.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.databinding.FragmentHomeBinding
import com.halalin.service.model.Service
import com.halalin.util.*

class HomeFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.serviceList.value !is Resource.Success) viewModel.fetchServiceList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.serviceList.observe(viewLifecycleOwner, { checkServiceListResource(it) })
    }

    private fun setupViews() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_search_vendor -> findNavController().navigate(
                    HomeFragmentDirections.actionSearchVendor("")
                )
                R.id.menu_cart -> {
                    Snackbar.make(binding.root, "Not implemented", Snackbar.LENGTH_SHORT).show()
                }
                R.id.menu_notification -> {
                    Snackbar.make(binding.root, "Not implemented", Snackbar.LENGTH_SHORT).show()
                }
            }
            return@setOnMenuItemClickListener true
        }

        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        binding.recyclerViewService.adapter = ServiceAdapter {
            findNavController().navigate(HomeFragmentDirections.actionSearchVendor(it.id!!))
        }

        binding.recyclerViewService.addItemDecoration(
            MarginItemDecoration(
                MarginItemDecoration.Orientation.VERTICAL,
                2,
                resources.getDimensionPixelSize(R.dimen.default_recycler_view_item_spacing)
            )
        )

        binding.recyclerViewService.setHasFixedSize(true)
    }

    private fun checkServiceListResource(resource: Resource<List<Service>>) {
        var contentVisibility = View.INVISIBLE
        when (resource) {
            is Resource.Failure ->
                binding.layoutState.setErrorState(
                    R.string.fragment_home_fetch_service_list_failure_message,
                    R.string.retry
                ) { viewModel.fetchServiceList() }
            is Resource.Loading ->
                binding.layoutState.setLoadingState()
            is Resource.Success -> {
                contentVisibility = View.VISIBLE
                binding.layoutState.clearLayout()
                (binding.recyclerViewService.adapter as ServiceAdapter).updateServiceList(resource.data)
            }
        }
        binding.constraintLayoutContent.visibility = contentVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
