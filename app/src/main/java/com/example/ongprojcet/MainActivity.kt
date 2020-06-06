package com.example.ongprojcet


import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(applicationContext)
    }

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder



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
            startService(serviceIntent)

            var notiService = NotificationService::class.java
            var serviceIntent2 = Intent(applicationContext, notiService)
            startService(serviceIntent2)


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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.example_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item1 -> {
                val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = pref.edit()

                editor.putFloat("offsetX",0f).apply() //기존 저장값 리셋
                editor.putFloat("offsetZ",0f).apply()

                val intent = Intent(this, TutorialSettingActivity::class.java)
                intent.putExtra("fromMain",true)
                startActivity(intent)
                true
            }
            R.id.item2 -> {
                Toast.makeText(this, "Item 2 selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.item3 -> {
                Toast.makeText(this, "Item 3 selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.subitem1 -> {
                Toast.makeText(this, "Sub Item 1 selected", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.subitem2 -> {
                Toast.makeText(this, "Sub Item 2 selected", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

