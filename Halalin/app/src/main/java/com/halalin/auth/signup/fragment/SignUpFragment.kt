package com.halalin.auth.signup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.auth.model.User
import com.halalin.auth.signup.fragment.ViewModel.SignUpMessage
import com.halalin.databinding.FragmentSignUpBinding
import com.halalin.util.EventObserver
import com.halalin.util.requireInput
import kotlin.random.Random

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.signUpMessage.observe(viewLifecycleOwner, EventObserver {
            checkSignUpMessage(it)
        })
    }

    private fun setupViews() {
        binding.editTextPassword.doAfterTextChanged {
            binding.textInputLayoutPassword.error =
                if (it.toString().length < 8) getString(R.string.fragment_sign_up_weak_password_message)
                else null
        }

        binding.editTextPasswordConfirmation.doAfterTextChanged {
            binding.textInputLayoutPasswordConfirmation.error =
                if (it.toString() == binding.editTextPassword.text.toString()) null
                else getString(R.string.fragment_sign_up_confirmation_password_doesnt_match_message)
        }

        binding.buttonSignUp.setOnClickListener { signUp() }
        binding.buttonSignIn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun signUp() {
        val displayName = binding.editTextDisplayName.requireInput(
            binding.textInputLayoutDisplayName
        ) ?: return

        val emailAddress = binding.editTextEmailAddress.requireInput(
            binding.textInputLayoutEmailAddress
        ) ?: return

        binding.editTextPassword.text.toString().let {
            if (it.isNotEmpty() && it.length < 8) return
        }

        val password = binding.editTextPassword.requireInput(
            binding.textInputLayoutPassword
        ) ?: return

        binding.editTextPasswordConfirmation.text.toString().let {
            if (it.isNotEmpty() && it != password) return
        }

        binding.editTextPasswordConfirmation.requireInput(
            binding.textInputLayoutPasswordConfirmation
        ) ?: return

        viewModel.signUp(
            User(
                displayName = displayName,
                emailAddress = emailAddress,
                password = password,
                profilePictureUrl = "https://i.pravatar.cc/250?img=${Random.nextInt(1, 71)}}"
            )
        )
    }

    private fun checkSignUpMessage(message: SignUpMessage) {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        var isLoading = false
        var buttonSignUpTextResId = R.string.sign_up
        when (message) {
            SignUpMessage.EMAIL_ADDRESS_ALREADY_REGISTERED ->
                showSnackbar(R.string.fragment_sign_up_email_address_already_registered_message)
            SignUpMessage.EMAIL_ADDRESS_MALFORMED ->
                showSnackbar(R.string.fragment_sign_up_email_address_malformed_message)
            SignUpMessage.FAILURE ->
                showSnackbar(R.string.fragment_sign_up_failure_message)
            SignUpMessage.LOADING -> {
                isLoading = true
                buttonSignUpTextResId = R.string.fragment_sign_up_loading_message
            }
            SignUpMessage.SUCCESS -> findNavController().navigate(
                SignUpFragmentDirections.actionFinishAuthenticate()
            )
        }
        binding.buttonSignUp.isEnabled = !isLoading
        binding.buttonSignUp.setText(buttonSignUpTextResId)
        binding.buttonSignIn.isCheckable = !isLoading
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
