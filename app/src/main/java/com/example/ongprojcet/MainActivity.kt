package com.example.ongprojcet

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notification_layout.*
import java.io.File


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(applicationContext)
    }

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val channelId = "com.example.ongprojcet"
    private val description = "Test notification"


    private val notificationHandler2: NotificationHandler by lazy {
        NotificationHandler(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        //**
        //튜토리얼 갔었는지 확인
        val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
        val isGoTutorial: Boolean = pref.getBoolean("isGoTutorial", false);
        if(!isGoTutorial) {
            //튜토리얼 안갔을 때에만 서비스 실행. 갔을 때에는 튜토리얼에서 이미 실행함
            var serviceClass = MyService::class.java
            var serviceIntent = Intent(applicationContext, serviceClass)
            var notiService = NotificationService::class.java
            var serviceIntent2 = Intent(applicationContext, notiService)
            startService(serviceIntent2)
            startService(serviceIntent)

        }
        //**



        val bottomNavigationView = findViewById<View>(R.id.bottomNavigationView) as BottomNavigationView
        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_layout, homeFragment).commit()
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {

        when (p0.itemId) {
            R.id.tab1 -> {
                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_layout, homeFragment).commit()
            }
            R.id.tab2 -> {
                val postureFragment = PostureFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_layout, postureFragment).commit()
            }
            R.id.tab3 -> {
                val monitoringFragment = MonitoringFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_layout, monitoringFragment).commit()
            }
            R.id.tab4 -> {
                val donationFragment = DonationFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_layout, donationFragment).commit()
            }
        }
        return true
    }
}

