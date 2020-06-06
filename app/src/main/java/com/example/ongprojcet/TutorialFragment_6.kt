package com.example.ongprojcet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tutorial_6.*
import kotlinx.android.synthetic.main.fragment_tutorial_6.startButton

import java.util.ArrayList


class TutorialFragment_6 : Fragment() {
    lateinit var asyncTask: currentTask



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
        return inflater.inflate(R.layout.fragment_tutorial_6, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //홈버튼 누리고 돌아오면 onViewCreated로 안가고 resume으로 옴.
        //그래서 onResume에서 asyncTask 호출
        asyncTask = currentTask(requireContext(),notification,startButton, progressBar)
        asyncTask.execute()  //start asyncTask

        startButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

    }

    //AsyncTask
    class currentTask(
        mContext: Context,
        notification: TextView,
        startButton: Button,
        progressBar: ProgressBar

    ):AsyncTask<Void, Void, Void>() {

        lateinit var mContext:Context
        lateinit var notification :TextView
        lateinit var startButton : Button
        lateinit var progressBar: ProgressBar


        lateinit var pref: SharedPreferences
        lateinit var editor: SharedPreferences.Editor

        var offsetZ = 0f
        var offsetX = 0f
        var zFloat = 0f
        var xFloat = 0f
        var xArray = ArrayList<Float>()
        var zArray = ArrayList<Float>()
        var secCount = 0


        var isBluetoothRunning:Boolean = false;

        init {
            this.mContext = mContext
            pref = this.mContext.getSharedPreferences("checkFirst", 0)
            editor = pref.edit()
            this.notification = notification
            this.startButton = startButton
            this.progressBar = progressBar




        }

        fun calOffsetX(x_array : ArrayList<Float>): Float {
            var x_mean = x_array.sum()/x_array.size
            var x_offset  = (-(0.7373/0.0053 + x_mean)).toFloat()
            return x_offset
        }
        //몸의 각도를 구하는 공식에 z_offset을 더하여 계산했을 때 0이 나오는 x_offset을 찾는 함수
        fun calOffsetZ(z_array : ArrayList<Float>): Float {
            return ((1.9747/0.064) - (z_array.sum()/z_array.size)).toFloat()
        }


        override fun doInBackground(vararg p0: Void?): Void? {
            Log.v("SHP_isBluetoothRunning홈", pref.getBoolean("isBluetoothRunning", false).toString())
            while(!isCancelled){
                //현재 값 가져오기
                isBluetoothRunning = pref.getBoolean("isBluetoothRunning", false);

                if(isBluetoothRunning) {
//                    currentLR = pref.getFloat("xFloat", (-200.0).toFloat())
//                    currentFB = pref.getFloat("currentFB", (-200.0).toFloat())
                    xFloat = pref.getFloat("xFloat",0f)
                    zFloat = pref.getFloat("zFloat", 0f)

                    xArray.add(xFloat)
                    zArray.add(zFloat)



                    Thread.sleep(1000)

                    notification.text = "이 자세를 약 ${10-secCount}초간 유지해 주세요."
                    secCount +=1
                    if(secCount == 10) {
                        publishProgress()
                        this.cancel(true)





                    }

                }
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {

            notification.text ="완료되었습니다."
            startButton.visibility = View.VISIBLE
            offsetX = calOffsetX(xArray)
            offsetZ = calOffsetZ(zArray)
            editor.putFloat("offsetX",offsetX).apply()
            editor.putFloat("offsetZ",offsetZ).apply()
            progressBar.visibility =View.GONE


            super.onProgressUpdate(*values)
        }
    }
}

