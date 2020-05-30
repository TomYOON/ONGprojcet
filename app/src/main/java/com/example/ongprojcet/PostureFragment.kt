package com.example.ongprojcet

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_posture.*


class PostureFragment : Fragment(){


    lateinit var asyncTask: currentTask2





    override fun onPause() {
        //프레그먼트 바뀔 때 asynctask 종료
        Log.v("fragment", "onPause")
        asyncTask.cancel(true)
        super.onPause()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.v("fragment", "onViewCreated")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_posture, container, false)
    }

    override fun onResume() {
        Log.v("fragment", "onResume")
        asyncTask = currentTask2(requireContext(),xyImgView,lrangle,fbangle)
        asyncTask.execute()  //start asyncTask




      super.onResume()
    }






    //AsyncTask
    class currentTask2(
        mContext: Context,
        xyImgView: ImageView,
        lrangle: TextView,
        fbangle: TextView

    ): AsyncTask<Void, Void, Void>() {

        lateinit var mContext: Context
        lateinit var xyImgView: ImageView
        lateinit var lrangle: TextView
        lateinit var fbangle: TextView

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
        lateinit var intent : Intent




        var isBluetoothRunning:Boolean = false;

        init {
            this.mContext = mContext
            pref = this.mContext.getSharedPreferences("checkFirst", 0)
            editor = pref.edit()
            this.lrangle = lrangle
            this.fbangle = fbangle
            this.xyImgView = xyImgView
            this.mContext.getSystemService(Context.NOTIFICATION_SERVICE)
            this.intent = Intent(mContext,PostureFragment::class.java)

        }




        override fun doInBackground(vararg p0: Void?): Void? {
            Log.v("SHP_isBluetoothRunning홈", pref.getBoolean("isBluetoothRunning", false).toString())
            while(!isCancelled){
                //현재 값 가져오기
                isBluetoothRunning = pref.getBoolean("isBluetoothRunning", false);

                if(isBluetoothRunning) {
                    currentLR = pref.getFloat("currentLR", (-200.0).toFloat())
                    currentFB = pref.getFloat("currentFB", (-200.0).toFloat())

                    posture.putData(currentLR.toInt(),currentFB.toInt())

                    Thread.sleep(1000)
                    secCount += 1

                    if(secCount % alarmInterval==0){ //일정 간격으로 알람
                        var msg = posture.getIntervalAlarm()
                        notificationHandler.notify(msg,intent)

                        Log.v("checkAlarm",msg)
                        Log.v("checkPosture",posture.testAlarm())
//                        posture.resetData()

                    }
                    if(posture.isBadContinue()){  //연속적으로 나쁜자세 취할경우 알람
                        var msg = posture.getContinueAlarm()
                        notificationHandler.notify(msg,intent)
                        posture.badPostureContinueCount = 0 // 연속 데이터 초기화

                    }

                    publishProgress()  //update UI


                    }

                }
            return null
            }


        override fun onProgressUpdate(vararg values: Void?) {

            if (-5 <= currentLR.toInt() && currentLR.toInt() <= 10 && -10 <= currentFB.toInt() && currentFB.toInt() <= 10) {

                xyImgView.setBackgroundResource(R.drawable.center);
                xyImgView.visibility = View.VISIBLE

            } else if (currentLR.toInt() < -10 && -10 <= currentFB.toInt() && currentFB.toInt() <= 10) {

                xyImgView.setBackgroundResource(R.drawable.left);
                xyImgView.visibility = View.VISIBLE

            } else if (10 < currentLR.toInt() && -10 <= currentFB.toInt() && currentFB.toInt() <= 10) {

                xyImgView.setBackgroundResource(R.drawable.right);
                xyImgView.visibility = View.VISIBLE

            } else if (currentLR.toInt() < -10 && 10 < currentFB.toInt()) {

                xyImgView.setBackgroundResource(R.drawable.leftup);
                xyImgView.visibility = View.VISIBLE

            } else if (-10 <= currentLR.toInt() && currentLR.toInt() <= 10 && 10 < currentFB.toInt()) {

                xyImgView.setBackgroundResource(R.drawable.up);
                xyImgView.visibility = View.VISIBLE

            } else if (10 < currentLR.toInt() && 10 < currentFB.toInt()) {

                xyImgView.setBackgroundResource(R.drawable.rightup);
                xyImgView.visibility = View.VISIBLE

            } else if (currentLR.toInt() < -10 && currentFB.toInt() < -10) {

                xyImgView.setBackgroundResource(R.drawable.leftdown);
                xyImgView.visibility = View.VISIBLE

            } else if (-10 <= currentLR.toInt() && currentLR.toInt() <= 10 && currentFB.toInt() < -10) {

                xyImgView.setBackgroundResource(R.drawable.down);
                xyImgView.visibility = View.VISIBLE
            } else if (10 < currentLR.toInt() && currentFB.toInt() < -10) {

                xyImgView.setBackgroundResource(R.drawable.rightdown);
                xyImgView.visibility = View.VISIBLE
            } else {

                //Toast.makeText(applicationContext, "측정되지않습니다", Toast.LENGTH_SHORT).show()
                xyImgView.setBackgroundResource(R.drawable.nullpoint);
                xyImgView.visibility = View.VISIBLE

            }
            lrangle.text = "좌우 기울기 각도 : " + currentLR + " (±5°)"
            fbangle.text = "앞뒤 기울기 각도 : " + currentFB + " (±5°)"


            super.onProgressUpdate(*values)
        }
        }


}

