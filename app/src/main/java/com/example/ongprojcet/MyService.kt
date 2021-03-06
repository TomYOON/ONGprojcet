package com.example.ongprojcet

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ongprojcet.BLE.applyUISchedulers
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.exceptions.BleCharacteristicNotFoundException
import rx.lang.kotlin.onError
import rx.subscriptions.CompositeSubscription
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class MyService: Service() {
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var now:String

    lateinit var saveResult: SharedPreferences
    lateinit var saveResultEditor: SharedPreferences.Editor


    //자세과련
    private val subscription: CompositeSubscription by lazy { CompositeSubscription() }
    private var connected: Boolean = false
    private var connecting: Boolean = false
    private var deviceConn: RxBleConnection? = null
    private var reconnect: Boolean = false
    private lateinit var device: RxBleDevice
    private lateinit var deviceData: BleDevice
    var isPasswordInit = true
    var startBle = false
    lateinit var ringtoneUri: Uri
    lateinit var ringtone: Ringtone


    //알림 관련
    lateinit var settingPref : SharedPreferences


    val notificationHandler: NotificationHandler by lazy {
        NotificationHandler(applicationContext)
    }
    var posture = Posture()
    var alarmInterval = 10
    var currentTime = 0L
    var setNotify = false
    var setNotiftyRealtime = false
    var isNotify = false


    var offsetX = 0f
    var offsetZ = 0f
    var goodTime = 0 //%%
    var point =0 //%%

    //val checkFirst: Boolean = pref.getBoolean("checkFirst", false);

    //editor.putBoolean("checkFirst", true)
    //editor.commit()

    //editor.putInt("point", 1000).apply() //** 일단 1000

    override fun onCreate() {
        super.onCreate()
        Log.v("SHP_isServiceRunning", "서비스 onCreate")

        //init
        pref = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
        editor = pref.edit()

        saveResult = getSharedPreferences("saveResult", Activity.MODE_PRIVATE)
        saveResultEditor = saveResult.edit()

        settingPref = PreferenceManager.getDefaultSharedPreferences(this) //settingPref는 전역에서 파일이름 없이 사용가능함
        setNotify = settingPref.getBoolean("key_switch_notification",false)
        setNotiftyRealtime = settingPref.getBoolean("key_switch_notification_realtime", false)

        var strAlarm = settingPref.getString("key_set_interval","10분")
        alarmInterval = alarmIntervalParsing(strAlarm!!)


        offsetX = pref.getFloat("offsetX",0f)
        offsetZ = pref.getFloat("offsetZ",0f)
        Log.v("offsetX",offsetX.toString())
        Log.v("offsetZ",offsetZ.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //서미스 키고 처음에 블투 초기화하기 시간에 로딩 이미지 출력을 위해
        editor.putBoolean("isBluetoothRunning", false).apply()
        Log.v("SHP_isServiceRunning", "서비스 onStartCommand")
        Log.v("SHP_isBluetoothRunning", pref.getBoolean("isBluetoothRunning", false).toString())

        //자세 측정
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (ringtoneUri == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)  //X
        }

        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)

        var gson = Gson()
        var json: String? = pref.getString("device", "")
        deviceData = gson.fromJson(json, BleDevice::class.java)
        setLayout()

        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setLayout() {
        /*
        if (!connecting) {
            if (connected) {
                factoryInit()
            } else if (reconnect) {
                connectDevice()
            }
        }

         */

        if (!startBle) {
            connectDevice()
        } else {
            subscription.clear()  //X
        }
        startBle = !startBle
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectDevice() {
        connected = false
        connecting = true
        reconnect = false
        device = BLE.client().getBleDevice(deviceData.address)  //여기서 멈춤

        subscription.clear()

        //연결 시작
        subscription.add(
            device.establishConnection(false)
                .retry(1)
                .delay(5000, TimeUnit.MILLISECONDS)
                .applyUISchedulers()
                .subscribe({
                    deviceConn = it
                    firmware()
                } //성공했을때
                    //실패했을때
                    , {
                        onConnectionError(it)
                        it.printStackTrace()
                    })
        )


        subscription.add(device.observeConnectionStateChanges().subscribe {

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun firmware() {
        subscription.add(
            deviceConn!!.readCharacteristic(BLE.firmwareUUID)
                .applyUISchedulers()
                .map { String(it) }
                .subscribe({
                    password()
                    isPasswordInit = false // password 뺐는지 표시
                }, {
                    onConnectionError(it)
                    it.printStackTrace()
                })
        )
    }

    // 비번 체크
    @RequiresApi(Build.VERSION_CODES.O)
    fun password() {
        var data = intArrayOf(0xFF, 0xFF, 0xFF, 0xFF)

        subscription.add(
            deviceConn!!.writeCharacteristic(BLE.controlPointUUID, BLE.byteFactory(BLE.Action.PASSWORD, data))
                .flatMap {
                    deviceConn!!.writeCharacteristic(
                        BLE.controlPointUUID,
                        BLE.byteFactory(BLE.Action.PASSWORD_WRITE, data)
                    )
                }
                .applyUISchedulers()
                .subscribe({
                    readRawDataStart()
                }, {

                    onConnectionError(it)
                    it.printStackTrace()
                })
        )
    }

    fun factoryInit() {
        var data = intArrayOf(0xFF, 0xFF, 0xFF, 0xFF)

        deviceConn!!.writeCharacteristic(BLE.controlPointUUID, BLE.byteFactory(BLE.Action.INIT, data))
            .applyUISchedulers()
            .subscribe({
                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
               // finish()
            }, {
                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
                // finish()
            })
    }

    // 가속도 x,y,z 받아서 표시하는 곳
    @RequiresApi(Build.VERSION_CODES.O)
    fun readRawDataStart() {

        connected = true
        connecting = false
        var startTime = System.currentTimeMillis()
        var intent = Intent(this, MainActivity::class.java)

//        setNotify = sps.getBoolean("key_add_notification", false)








        subscription.add(
            deviceConn!!.writeCharacteristic(BLE.controlPointUUID, BLE.byteFactory(BLE.Action.RAW_ACCELEROMETER, null))
                .flatMap { deviceConn!!.setupNotification(BLE.notifyUUID) }
                .flatMap { it }
                .applyUISchedulers()
                .onError { it.printStackTrace() }
//                        15초뒤에 꺼지게. 실패함.
//                        .timeout(15, TimeUnit.SECONDS)
                .subscribe {
                    var rawCom = BLE.readRawDataCombine(it)

                    //x,y,z 받아옴
                    var x = rawCom["x"].toString()
                    var y = rawCom["y"].toString()
                    var z = rawCom["z"].toString()

                    //test를 위해 일단 여기다가 가져다 둠. 추후에 지워도됨
                    offsetX = pref.getFloat("offsetX",0f)
                    offsetZ = pref.getFloat("offsetZ",0f)
//                    Log.v("offsetX",offsetX.toString())
//                    Log.v("offsetZ",offsetZ.toString())



                    //@@@@@@ 이부분 써야됨
                    //raw_x.text = "x : " + x
                    //raw_y.text = "y : " + y
                    //raw_z.text = "z : " + z
                    //raw_z.text = "z : " + rawCom["z"].toString()

                    var xint = x.toFloat() + offsetX
                    var zint = z.toFloat() + offsetZ
                    var currentLR = ((xint) * 0.0053 + 0.7373) * -1
                    var currentFB = (zint) * (-0.0064) + 1.9747

                    putData(currentLR.toInt(),currentFB.toInt())
                    //current time **
                    //2020년 5월 16일 오후 8시 25분 30초 -> 2020-05-16-20-25-30
                    var nowDate = LocalDate.now()
                    var sNowDate= nowDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    var nowTime = LocalTime.now()
                    var sNowTime = nowTime.format(DateTimeFormatter.ofPattern("HH-mm-ss"))
                    now = sNowDate + "-" + sNowTime  //변수 이름

                    //여기서 변수 저장 **
                    //editor.putFloat("LR-"+now, currentLR.toFloat()).apply()
                    //editor.putFloat("FB-"+now, currentFB.toFloat()).apply()

                    editor.putBoolean("isBluetoothRunning", true).apply()

                    //련재 값은 따로 계속 갱신
                    editor.putFloat("currentLR", currentLR.toFloat()).apply()
                    editor.putFloat("currentFB", currentFB.toFloat()).apply()

                    //%% 포인트 적립 (실제로는 5분에 1p지만 여기서는 잘 보여주기 위해서 일단 1분에 1p)
                    //1초에 블루투스 인식 5번 됨
                    goodTime = pref.getInt("goodTime",0)
                    if(currentLR.toFloat()<=10.0f && currentFB.toFloat()<=10.0f){
                        //좌우 앞뒤 모두 바른 자세이면 (바른자세라는건 둘다 10도 내)
                        goodTime++
                        if(goodTime >= 300){  //5분이면 1500. 여기선는 1분으로 할거니까 300
                            //5분 단위로 1p 적립 (1시간에 12p)
                            //1초에 5번정도 블투 인식됨. 1분이면 300번. 5분이면 1500
                            point = pref.getInt("point",0)
                            point++
                            editor.putInt("point", point).apply()
                            goodTime = 0  //다시 처음부터 시작
                            Toast.makeText(this, "기부 포인트 +1", Toast.LENGTH_SHORT).show()



                        }
                        editor.putInt("goodTime", goodTime).apply()
                    }
//                    Log.v("SHP_바른자세시간", goodTime.toString())
//                    Log.v("SHP_포인트", point.toString())
                    //%%

                    editor.putFloat("xFloat", xint).apply()
                    editor.putFloat("zFloat",zint).apply()

                    Log.v("SHP_현재시분초", now)
//                    Log.v("SHP_LR"+now, currentLR.toString())
//                    Log.v("SHP_FB"+now, currentFB.toString())

                    //lrangle.text = "좌우 기울기 각도 : " + currentLR.toInt() + " (±5°)"
                    //fbangle.text = "앞뒤 기울기 각도 : " + currentFB.toInt() + " (±5°)"


                    var strAlarm = settingPref.getString("key_set_interval","10분")
                    alarmInterval = alarmIntervalParsing(strAlarm!!) //10분 -> 10초로 설정해둠
                    setNotify = settingPref.getBoolean("key_switch_notification",false) //Listener 기술을 아직 못써서 일단 계속 갱신시킴
                    setNotiftyRealtime = settingPref.getBoolean("key_switch_notification_realtime", false)
                    posture.putData(currentLR.toInt(), currentFB.toInt(),1)

                    var currentTime = System.currentTimeMillis()
                    var timeGap = (currentTime - startTime)/1000
                    if(setNotify){

                        Log.v("timeGap",timeGap.toString())
                        if(timeGap.toInt() % alarmInterval == 0) {
                            var msg = posture.getIntervalAlarm()
                            notificationHandler.notify(msg, intent)

                            Log.v("checkAlarm", msg)
                            Log.v("checkPosture", posture.testAlarm())
                        }

                    }
                    if(setNotiftyRealtime) {
                        if (posture.isBadContinue(15)) {
                            var msg = posture.getContinueAlarm()
                            notificationHandler.notify(msg, intent)
                            posture.badPostureContinueCount = 0 // 연속 데이터 초기화
                        }
                    }
//                    if(isNotify){
//                        if(timeGap.toInt() % alarmInterval+1 == 0) {
//                            isNotify = false
//                        }
//                    }




                }
        )
    }

    val abnormalThreshold = 10
    var postures = FloatArray(9){0f}
    fun checkPosture(lrAngle: Int, fbAngle: Int): Int {
        if(lrAngle > abnormalThreshold){  //오른쪽으로 기울었을 경우
            if(fbAngle > abnormalThreshold) { //오른쪽 + 앞
                return 0
            }
            else if(fbAngle < -abnormalThreshold) { //오른쪽 + 뒤
                return 8
            }
            return 5  //오른쪽만
        } else if(lrAngle < -abnormalThreshold) { //왼쪽인 경우
            if(fbAngle > abnormalThreshold) { //왼쪽 + 앞
                return 2
            }
            else if(fbAngle < -abnormalThreshold) { // 왼쪽 + 뒤
                return 6
            }
            return 3 //왼쪽만
        }

        if(fbAngle > abnormalThreshold) return 1 //앞 쪽만
        else if (fbAngle < -abnormalThreshold) return 7 //뒷쪽만

        return 4 // 정상
    }

    fun putData(lrAngle: Int, fbAngle:Int)  {
        var index = checkPosture(lrAngle,fbAngle)
        postures[index] += 0.2f
        saveResultEditor.putBoolean("isBluetoothRunning", true).apply()
        //련재 값은 따로 계속 갱신
        saveResultEditor.putFloat("front_left", postures[0].toFloat()).apply()
        saveResultEditor.putFloat("front", postures[1].toFloat()).apply()
        saveResultEditor.putFloat("front_right", postures[2].toFloat()).apply()
        saveResultEditor.putFloat("left", postures[3].toFloat()).apply()
        saveResultEditor.putFloat("right", postures[5].toFloat()).apply()
        saveResultEditor.putFloat("back_left", postures[6].toFloat()).apply()
        saveResultEditor.putFloat("back", postures[7].toFloat()).apply()
        saveResultEditor.putFloat("back_right", postures[8].toFloat()).apply()
    }
    // 블루투스 연결 에러 처리
    fun onConnectionError(t: Throwable) {
        connected = false
        connecting = false
        reconnect = true
    }

    override fun onDestroy() {
        //editor.putBoolean("isServiceRunning", false).apply()
        Log.v("SHP_isServiceRunning", "false")
        Log.v("SHP_isServiceRunning", "서비스 종료")
        editor.putBoolean("isBluetoothRunning", false).apply()
        Log.v("SHP_isBluetoothRunning", pref.getBoolean("isBluetoothRunning", false).toString())
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }



    fun alarmIntervalParsing(strInterval : String) : Int{ //일단 test를 위해 시간을 낮춤
        //    <item>10분</item>
        //    <item>30분</item>
        //    <item>1시간</item>
        //    <item>2시간</item>
        if(strInterval == "10분") return 10
        if(strInterval == "30분") return 30
        if(strInterval == "1시간") return 60
        if(strInterval == "2시간") return 120
        return 10
    }

}