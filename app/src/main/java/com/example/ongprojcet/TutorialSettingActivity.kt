package com.example.ongprojcet

import androidx.appcompat.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_tutorial_setting.*
import kotlinx.android.synthetic.main.fragment_tutorial_7.*
import com.example.ongprojcet.CircleIndicator

class TutorialSettingActivity : AppCompatActivity(){
    //lateinit var deviceData: BleDevice //**


    var created = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_setting)

        //**
        //start service
        var serviceClass = MyService::class.java
        var serviceIntent = Intent(applicationContext, serviceClass)
        startService(serviceIntent)

        //튜토리얼 갔었는다고 표시
        val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putBoolean("isGoTutorial", true).apply()
        //**

        //deviceData = intent.getParcelableExtra<BleDevice>("device") //**

        var actionBar: ActionBar? = supportActionBar
        actionBar?.hide()


//        val pagerAdapter = MainAdapter(supportFragmentManager)
//        val pager = findViewById<ViewPager>(R.id.viewPager)
//
//        pager.adapter = pagerAdapter
//
//        ciMainActivity.createDotPanel(6, R.drawable.indicator_dot_off, R.drawable.indicator_dot_on, 0)
//
//
//        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
//            override fun onPageScrollStateChanged(p0: Int) {
//
//            }
//
//            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
//
//            }
//
//            override fun onPageSelected(p0: Int) {
//                ciMainActivity.selectDot(p0)
//            }
//
//        })
        this.created = true

    }

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_tutorial_setting)

        //**
        //start service
        //튜토리얼 갔었는다고 표시
        val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()



        if(intent.hasExtra("fromMain")){
            Log.v("hasExtra","fromMain")
            var serviceClass = MyService::class.java
            var serviceIntent = Intent(applicationContext, serviceClass)
            startService(serviceIntent)
        }
//        created = pref.getBoolean("fromMain",false)
//        Log.v("created", created.toString())
//        if(!created) {
//
//        }
        editor.putBoolean("isGoTutorial", true).apply()

        //**

        //deviceData = intent.getParcelableExtra<BleDevice>("device") //**

//        var actionBar: ActionBar? = supportActionBar
//        actionBar?.hide()


        val pagerAdapter = MainAdapter(supportFragmentManager)
        val pager = findViewById<ViewPager>(R.id.viewPager)

        pager.adapter = pagerAdapter

        ciMainActivity.createDotPanel(6, R.drawable.indicator_dot_off, R.drawable.indicator_dot_on, 0)


        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(p0: Int) {
                ciMainActivity.selectDot(p0)
            }

        })

    }

}