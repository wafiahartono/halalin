package com.halalin.vendor.product.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.cart.model.Item
import com.halalin.databinding.FragmentAddCartItemBinding
import com.halalin.util.EventObserver
import com.halalin.util.RangeInputFilter
import com.halalin.util.Resource
import java.text.NumberFormat
import java.util.*

class AddCartItemFragment(private val item: Item) : BottomSheetDialogFragment() {
    private var _binding: FragmentAddCartItemBinding? = null
    private val binding get() = _binding!!

    private val quantity = MutableLiveData(1)

    private val viewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddCartItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quantity.observe(viewLifecycleOwner, {
            binding.textViewSubtotal.text = NumberFormat
                .getCurrencyInstance(Locale("id", "ID"))
                .format(item.product!!.price!!.times(it))
            if (binding.editTextQuantity.text.toString().toIntOrNull() != it)
                binding.editTextQuantity.setText(it.toString())
        })

        binding.textViewProductName.text = item.product!!.name

        binding.textViewSubtotal.text = NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(item.product.price)

        binding.textInputLayoutQuantity.setStartIconOnClickListener {
            if (quantity.value?.minus(1) == 0) return@setStartIconOnClickListener
            quantity.value = quantity.value?.minus(1)
        }

        binding.textInputLayoutQuantity.setEndIconOnClickListener {
            if (quantity.value?.plus(1) == 101) return@setEndIconOnClickListener
            quantity.value = quantity.value?.plus(1)
        }

        binding.editTextQuantity.filters = arrayOf(RangeInputFilter(1, 100))

        binding.editTextQuantity.doAfterTextChanged {
            val value = it.toString().toIntOrNull()
            if (value != null && value != quantity.value) quantity.value = value
        }

        binding.buttonAddToCart.setOnClickListener {
            viewModel.addItemToCart(item.copy(quantity = quantity.value))
        }

        viewModel.addItemToCartResult.observe(viewLifecycleOwner,
            EventObserver {
                val messageTextResId = when (it) {
                    is Resource.Failure ->
                        R.string.dialog_fragment_add_cart_item_failure_message
                    is Resource.Success ->
                        R.string.dialog_fragment_add_cart_item_success_message
                    else -> R.string.unexpected_error_message
                }
                Snackbar.make(binding.root, messageTextResId, Snackbar.LENGTH_LONG).show()
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
