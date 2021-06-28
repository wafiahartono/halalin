package com.halalin.message.room.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.halalin.databinding.FragmentMessageRoomBinding
import com.halalin.util.topLevelDestinationIdList

class MessageRoomFragment : Fragment() {
    private var _binding: FragmentMessageRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageRoomBinding.inflate(inflater, container, false)
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
