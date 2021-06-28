package com.halalin.vendor.fragment.search.filter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionManager
import coil.Coil
import coil.request.LoadRequest
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.databinding.FragmentSearchFilterBinding
import com.halalin.databinding.ItemServiceChipBinding
import com.halalin.service.model.Service
import com.halalin.util.Resource
import com.halalin.util.logd
import com.halalin.vendor.fragment.search.ViewModel
import com.halalin.vendor.model.SearchFilter

class SearchFilterFragment : DialogFragment() {
    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    override fun getTheme() = R.style.SearchFilterDialogFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logd("onCreateDialog viewModel.serviceList.value: ${viewModel.serviceList.value}")
        if (viewModel.serviceList.value !is Resource.Success) viewModel.fetchServiceList()
        return super.onCreateDialog(savedInstanceState).apply {
            enterTransition = androidx.navigation.ui.R.anim.nav_default_enter_anim
            exitTransition = androidx.navigation.ui.R.anim.nav_default_exit_anim
            reenterTransition = androidx.navigation.ui.R.anim.nav_default_pop_enter_anim
            returnTransition = androidx.navigation.ui.R.anim.nav_default_pop_exit_anim
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.serviceList.observe(viewLifecycleOwner, { checkServiceListResource(it) })
    }

    private fun checkServiceListResource(resource: Resource<List<Service>>) {
        when (resource) {
            is Resource.Failure -> Snackbar
                .make(
                    binding.root,
                    R.string.dialog_fragment_search_filter_fetch_service_list_failure_message,
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(R.string.retry) { viewModel.fetchServiceList() }
                .show()
            is Resource.Success -> updateServiceListViews(resource.data)
        }
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        binding.buttonApply.setOnClickListener {
            val serviceId = binding.chipGroupServiceFilter.let { chipGroup ->
                if (chipGroup.checkedChipId == View.NO_ID) null
                else chipGroup.findViewById<Chip>(chipGroup.checkedChipId).tag as String
            }
            val sort = SearchFilter.Sort(
                SearchFilter.Sort.Field.values()[binding.spinnerSortField.selectedItemPosition],
                SearchFilter.Sort.Order.values()[binding.spinnerSortOrder.selectedItemPosition]
            )
            viewModel.searchVendor(SearchFilter(serviceId, sort))
            dismiss()
        }

        binding.spinnerSortField.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.vendor_search_filter_sort_field,
            android.R.layout.simple_list_item_1
        ).apply { setDropDownViewResource(android.R.layout.simple_list_item_1) }

        binding.spinnerSortOrder.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.vendor_search_filter_sort_order,
            android.R.layout.simple_list_item_1
        ).apply { setDropDownViewResource(android.R.layout.simple_list_item_1) }
    }

    private fun updateServiceListViews(serviceList: List<Service>) {
        TransitionManager.beginDelayedTransition(binding.chipGroupServiceFilter)
        serviceList.forEach { service ->
            ItemServiceChipBinding
                .inflate(layoutInflater, binding.chipGroupServiceFilter, true)
                .root.apply {
                    isCheckable = true
                    tag = service.id
                    text = service.name
                    Coil.imageLoader(context).execute(
                        LoadRequest.Builder(context)
                            .allowHardware(false)
                            .data(service.iconUrl)
                            .target { drawable -> chipIcon = drawable }
                            .build()
                    )
                }
        }
        updateSearchFilterViews(viewModel.getSearchFilter() ?: SearchFilter(null, null))
    }

    private fun updateSearchFilterViews(searchFilter: SearchFilter) {
        if (searchFilter.serviceId != null) {
            for (chip in binding.chipGroupServiceFilter.children) {
                if (chip is Chip && chip.tag == searchFilter.serviceId) {
                    chip.isChecked = true
                    break
                }
            }
        }
        binding.spinnerSortField.setSelection(
            searchFilter.sort?.field?.ordinal ?: SearchFilter.Sort.Field.NAME.ordinal
        )
        binding.spinnerSortOrder.setSelection(
            searchFilter.sort?.order?.ordinal ?: SearchFilter.Sort.Order.ASC.ordinal
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
