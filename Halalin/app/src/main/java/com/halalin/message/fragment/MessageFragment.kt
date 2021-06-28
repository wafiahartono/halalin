package com.halalin.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.halalin.databinding.FragmentMesssageBinding
import com.halalin.util.topLevelDestinationIdList

class MessageFragment : Fragment() {
    private var _binding: FragmentMesssageBinding? = null
    private val binding: FragmentMesssageBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMesssageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setupWithNavController(
            findNavController(),
            AppBarConfiguration(topLevelDestinationIdList)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
