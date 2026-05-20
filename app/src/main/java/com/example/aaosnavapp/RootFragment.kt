package com.example.aaosnavapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class RootFragment : Fragment(R.layout.fragment_root) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btnNavigate).setOnClickListener {
            findNavController().navigate(R.id.action_rootFragment_to_secondFragment)
        }
    }
}
