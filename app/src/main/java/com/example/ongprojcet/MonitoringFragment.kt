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
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_monitoring.*
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*

class MonitoringFragment : Fragment(){

    lateinit var asyncTask: currentTask2

    override fun onPause() {
        //프레그먼트 바뀔 때 asynctask 종료
        Log.v("fragment", "onPause")
        asyncTask.cancel(true)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
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
        //asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        lateinit var saveResult: SharedPreferences
        lateinit var saveResultEditor: SharedPreferences.Editor

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
            saveResult = this.mContext.getSharedPreferences("saveResult",0)
            saveResultEditor = saveResult.edit()

            this.chart1 = chart1
            this.mContext.getSystemService(Context.NOTIFICATION_SERVICE)
            this.intent = Intent(mContext,PostureFragment::class.java)
        }

        var postures  = FloatArray(9){0f}
        fun createChart(posture : Posture){
            var labels =
                arrayOf("front","front_right","right","back_right","back","back_left","left","front_left")
//            arrayOf("앞+오른쪽","앞","앞+왼쪽","왼 쪽","good","오른쪽","뒤 + 왼쪽","뒷 쪽","뒤+오른쪽")
            var radarChart: RadarChart? = null
            radarChart = chart1
            //var postures  = IntArray(8){0}
            postures[0]  = saveResult.getFloat("front_left",0f)
            postures[1]  = saveResult.getFloat("front",0f)
            postures[2]  = saveResult.getFloat("front_right",0f)
            postures[3]  = saveResult.getFloat("left",0f)
            postures[5]  = saveResult.getFloat("right",0f)
            postures[6]  = saveResult.getFloat("back_left",0f)
            postures[7]  = saveResult.getFloat("back",0f)
            postures[8]  = saveResult.getFloat("back_right",0f)
            val dataVals = ArrayList<RadarEntry>()

            dataVals.add(RadarEntry(postures[1]))
            dataVals.add(RadarEntry(postures[0]))
            dataVals.add(RadarEntry(postures[5]))
            dataVals.add(RadarEntry(postures[8]))
            dataVals.add(RadarEntry(postures[7]))
            dataVals.add(RadarEntry(postures[6]))
            dataVals.add(RadarEntry(postures[3]))
            dataVals.add(RadarEntry(postures[2]))

            Log.v("postureData",posture.testAlarm())
            val dataSet = RadarDataSet(dataVals, "User's State")

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
                    if(secCount % 5 == 0) {
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