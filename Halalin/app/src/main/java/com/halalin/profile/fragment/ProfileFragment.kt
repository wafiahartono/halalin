package com.halalin.profile.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.halalin.R
import com.halalin.auth.model.User
import com.halalin.databinding.FragmentProfileBinding
import com.halalin.util.topLevelDestinationIdList

class ProfileFragment : Fragment() {
    private val viewModel: ViewModel by activityViewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_sign_out -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.fragment_profile_sign_out_dialog_message)
                        .setPositiveButton(R.string.yes) { _, _ -> viewModel.signOut() }
                        .setNegativeButton(R.string.no, null)
                        .show()
                }
            }
            return@setOnMenuItemClickListener true
        }

        binding.toolbar.setupWithNavController(
            findNavController(), AppBarConfiguration(topLevelDestinationIdList)
        )

        viewModel.user.observe(viewLifecycleOwner, { updateUserViews(it) })
    }

    private fun updateUserViews(user: User?) {
        var userViewVisibility = View.VISIBLE
        var guestViewVisilibility = View.INVISIBLE
        if (user == null) {
            userViewVisibility = View.INVISIBLE
            guestViewVisilibility = View.VISIBLE
            binding.buttonSignIn.setOnClickListener { viewModel.signOut() }
        } else {
            binding.editTextDisplayName.hint = user.displayName
            binding.editTextEmailAddress.hint = user.emailAddress
        }
        binding.constraintLayoutUser.visibility = userViewVisibility
        binding.constraintLayoutGuest.visibility = guestViewVisilibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
