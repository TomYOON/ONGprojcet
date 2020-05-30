package com.example.ongprojcet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MonitoringFragment : Fragment(){
    override fun onAttach(context: Context?) {
        Log.v("monitoring_fragment", "Attach1")
        super.onAttach(context)
        Log.v("monitoring_fragment", "Attach2")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monitoring, container, false)
    }
}