package com.example.ongprojcet

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi


class NotificationService: Service() {
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var asyncTask: currentTask3

    var checkFirst = false
    var LR = 0f
    var FB = 0f
    var posture = Posture()
    var isBluetoothRunning = false
    var secCount = 0
    var alarmInterval = 5

    val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(applicationContext)
    }





    //val checkFirst: Boolean = pref.getBoolean("checkFirst", false);

    //editor.putBoolean("checkFirst", true)
    //editor.commit()

    //editor.putInt("point", 1000).apply() //** 일단 1000

    override fun onCreate() {
        super.onCreate()

        pref = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
        editor = pref.edit()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("notification","Running")

        checkFirst = pref.getBoolean("checkFirst",false)
        isBluetoothRunning = pref.getBoolean("isBluetoothRunning", false);
        asyncTask = currentTask3(applicationContext)
//        asyncTask.execute()

//        while(true){
//        if(checkFirst && isBluetoothRunning) {
//            LR = pref.getFloat("currentLR", 0f)
//            FB = pref.getFloat("currentRB", 0f)
//
//            posture.putData(LR.toInt(), FB.toInt())
//
//            Thread.sleep(1000)
//            secCount += 1
//
//
//            var intent2 = Intent(applicationContext, MainActivity::class.java)
//
//            if (secCount % alarmInterval == 0) {
//                var msg = posture.getIntervalAlarm()
//
//                notificationHandler.notify(msg, intent2)
//
//
//                Log.v("checkAlarm", msg)
//                Log.v("checkPosture", posture.testAlarm())
////                        posture.resetData()
//
//            }
//            if (posture.isBadContinue()) {
//                var msg = posture.getContinueAlarm()
//
//                notificationHandler.notify(msg, intent2)
//
//                posture.badPostureContinueCount = 0 // 연속 데이터 초기화
//
//            }
//        }

//        }

        return super.onStartCommand(intent, flags, startId)
    }

    class currentTask3(
        mContext: Context

    ): AsyncTask<Void, Void, Void>() {

        lateinit var mContext: Context


        var alarmInterval = 10 //알람주기 test용


        var posture = Posture()
        var secCount = 0
        lateinit var pref: SharedPreferences
        lateinit var editor: SharedPreferences.Editor


        private val notificationHandler: NotificationHandler by lazy {
            NotificationHandler(mContext)
        }

        var currentLR = 0f
        var currentFB = 0f
        lateinit var intent: Intent


        var isBluetoothRunning: Boolean = false;

        init {
            this.mContext = mContext
            pref = this.mContext.getSharedPreferences("checkFirst", 0)
            editor = pref.edit()


            this.intent = Intent(mContext, MainActivity::class.java)

        }


        override fun doInBackground(vararg p0: Void?): Void? {
            Log.v("notification","inBackground")
            while (true) {
                //현재 값 가져오기
                isBluetoothRunning = pref.getBoolean("isBluetoothRunning", false);


                if (isBluetoothRunning) {
                    currentLR = pref.getFloat("currentLR", (-200.0).toFloat())
                    currentFB = pref.getFloat("currentFB", (-200.0).toFloat())

                    posture.putData(currentLR.toInt(), currentFB.toInt())

                    Thread.sleep(1000)
                    secCount += 1

                    if (secCount % alarmInterval == 0) {
                        var msg = posture.getIntervalAlarm()
                        notificationHandler.notify(msg, intent)

                        Log.v("checkAlarm", msg)
                        Log.v("checkPosture", posture.testAlarm())
//                        posture.resetData()

                    }
                    if (posture.isBadContinue()) {
                        var msg = posture.getContinueAlarm()
                        notificationHandler.notify(msg, intent)
                        posture.badPostureContinueCount = 0 // 연속 데이터 초기화

                    }


                }

            }
            return null
        }
    }





    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

}