package com.example.ongprojcet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_monitoring.*
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

//class MonitoringFragment : Fragment(){
//    override fun onAttach(context: Context?) {
//        Log.v("monitoring_fragment", "Attach1")
//        super.onAttach(context)
//        Log.v("monitoring_fragment", "Attach2")
//    }
//
//    override fun onCreateView(
//        inflater: View,
//        container: Bundle?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_monitoring, container, false)
//    }
//}

class MonitoringFragment : Fragment(){


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
        return inflater.inflate(R.layout.fragment_monitoring, container, false)
    }

    override fun onResume() {
        Log.v("fragment", "onResume")
        asyncTask = currentTask2(requireContext(), chart1)
        asyncTask.execute()  //start asyncTask




        super.onResume()
    }






    //AsyncTask
    class currentTask2(
        mContext: Context,
        chart1: RadarChart


        ): AsyncTask<Void, Void, Void>() {

        lateinit var mContext: Context

        lateinit var chart1 : RadarChart
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
            this.chart1 = chart1
            this.mContext.getSystemService(Context.NOTIFICATION_SERVICE)
            this.intent = Intent(mContext,PostureFragment::class.java)

        }


        fun createChart(posture : Posture){
            var labels =
                arrayOf("front","front_right","right","back_right","back","back_left","left","front_left")
//            arrayOf("앞+오른쪽","앞","앞+왼쪽","왼 쪽","good","오른쪽","뒤 + 왼쪽","뒷 쪽","뒤+오른쪽")
            var radarChart: RadarChart? = null
            radarChart = chart1
            var postures  = posture.postures
            val dataVals = ArrayList<RadarEntry>()
            dataVals.add(RadarEntry(postures[1].toFloat()))
            dataVals.add(RadarEntry(postures[0].toFloat()))
            dataVals.add(RadarEntry(postures[5].toFloat()))
            dataVals.add(RadarEntry(postures[8].toFloat()))
            dataVals.add(RadarEntry(postures[7].toFloat()))
            dataVals.add(RadarEntry(postures[6].toFloat()))
            dataVals.add(RadarEntry(postures[3].toFloat()))
            dataVals.add(RadarEntry(postures[2].toFloat()))

            Log.v("postureData",posture.testAlarm())

            val dataSet = RadarDataSet(dataVals, "이름")

            dataSet.color = Color.RED
            dataSet.setDrawFilled(true)
            radarChart.setBackgroundColor(Color.WHITE)
            radarChart.description.isEnabled = false
            radarChart.webLineWidth = 1f
            radarChart.webColor = Color.BLACK
            radarChart.webLineWidth = 1f
            radarChart.webColorInner = Color.BLUE
            radarChart.webAlpha = 100
            radarChart.isRotationEnabled = false
            val data = RadarData()

            //만든 데이터를 그래프에 추가시켜준다.
            //for(index in )
            data.addDataSet(dataSet)

            val xAxis = radarChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.textSize = 9f
            xAxis.xOffset = 0f
            xAxis.yOffset = 0f
            xAxis.textColor = Color.BLACK
            radarChart.data = data
            radarChart.invalidate()
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


                    //UI 갱신
                    if(secCount % 3 == 0) {
                        publishProgress()
                    }


                }

            }
            return null
        }


        override fun onProgressUpdate(vararg values: Void?) {

            createChart(posture)


            super.onProgressUpdate(*values)
        }
    }


}