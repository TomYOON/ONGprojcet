package com.example.ongprojcet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class MainAdapter (fm:FragmentManager) : FragmentStatePagerAdapter(fm){
    override fun getItem(position: Int): Fragment? {

        return when(position){
            0-> TutorialFragment_1()
            1-> TutorialFragment_2()
            2-> TutorialFragment_3()
            3-> TutorialFragment_4()
            4-> TutorialFragment_5()
            5-> TutorialFragment_6()
//            6-> TutorialFragment_7()
            else-> null
        }
    }

    override fun getCount(): Int = 6
}