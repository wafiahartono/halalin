package com.halalin.auth.signin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.halalin.R
import com.halalin.auth.signin.fragment.ViewModel.SignInMessage
import com.halalin.databinding.FragmentSignInBinding
import com.halalin.util.EventObserver
import com.halalin.util.requireInput

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignIn.setOnClickListener { signIn() }
        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionSignUp())
        }
        binding.buttonSignInAsGuest.setOnClickListener { viewModel.signInAsGuest() }

        viewModel.signInMessage.observe(viewLifecycleOwner, EventObserver {
            checkSignInMessage(it)
        })
    }

    private fun signIn() {
        val emailAddress = binding.editTextEmailAddress.requireInput(
            binding.textInputLayoutEmailAddress
        ) ?: return

        val password = binding.editTextPassword.requireInput(
            binding.textInputLayoutPassword
        ) ?: return

        viewModel.signIn(emailAddress, password)
    }

    private fun checkSignInMessage(message: SignInMessage) {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        var isLoading = false
        var buttonSignInTextResId = R.string.sign_in
        when (message) {
            SignInMessage.FAILURE ->
                showSnackbar(R.string.fragment_sign_in_failure_message)
            SignInMessage.INVALID_CREDENTIALS ->
                showSnackbar(R.string.fragment_sign_in_invalid_credentials_message)
            SignInMessage.LOADING -> {
                isLoading = true
                buttonSignInTextResId = R.string.fragment_sign_in_loading_message
            }
            SignInMessage.SUCCESS -> findNavController().navigate(
                SignInFragmentDirections.actionFinishAuthenticate()
            )
        }
        binding.buttonSignIn.isEnabled = !isLoading
        binding.buttonSignIn.setText(buttonSignInTextResId)
        binding.buttonSignUp.isEnabled = !isLoading
        binding.buttonSignInAsGuest.isEnabled = !isLoading
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
