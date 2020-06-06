package com.example.ongprojcet

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SplashActivity : AppCompatActivity(){

    val SPLASH_VIEW_TIME : Long = 1000 //3초간 스플래시 화면 보여줌 //test를 위해 1초로 잠시 바꿈

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //delay를 위한 핸들러
        Handler().postDelayed({
            val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()

            val checkFirst: Boolean = pref.getBoolean("checkFirst", false);

            //**
            editor.putBoolean("isGoTutorial", false).apply() //튜토리얼 페이지에 안갔을 경우(처음이 아닐경우) 메인 액티비티에서 서비스 켜주고, 갔을 경우 튜토리얼액티비티에서 서비스를 켯을거고 메인에는 안 키기 위해 필요한 변수
            //**

            if(checkFirst == false){

                editor.putBoolean("checkFirst", true)
                editor.commit()

                editor.putInt("point", 1000).apply() //** 일단 1000
                editor.putInt("goodTime",0).apply()  //%% 좋은 자세로 앉아 있었던 시간 초기화

                val intent = Intent(this, PermissionActivity::class.java)
                startActivity(intent)
            }
            else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, SPLASH_VIEW_TIME)
    }
}