package com.example.ongprojcet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
//import android.support.v4.app.ActivityCompat
//import android.support.v4.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.scan.ScanSettings
import com.example.ongprojcet.BLE.applyUISchedulers
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_bluetooth.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.listitem_device.view.*
//위에있는 줄이 추가되었다.
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onErrorReturnNull
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit


class BluetoothActivity : AppCompatActivity() {

    private val addressSubject: PublishSubject<BleDevice> by lazy { PublishSubject.create<BleDevice>() }
    private val deviceList: MutableList<RxBleDevice> by lazy { mutableListOf<RxBleDevice>() }
    private val rxBleClient: RxBleClient by lazy { RxBleClient.create(this) }
    private val subscription: CompositeSubscription by lazy { CompositeSubscription() }
    private val viewSubscription: CompositeSubscription by lazy { CompositeSubscription() }

    private var isSearching: Boolean = false

    private val PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 300
    private val writePermssion = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        setLayout()
        ActivityCompat.requestPermissions(this, writePermssion, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
    }

    override fun onResume() {
        super.onResume()
        if (!isSearching) {
            scan_text.visibility = View.VISIBLE
            scan_progress.visibility = View.GONE
        }
        viewSubscribe()
    }

    override fun onStop() {
        super.onStop()
        subscription.clear()
        viewSubscription.clear()
    }

    // 블루투스 검색
    fun scanDevices() {
        if (isSearching) {
            return
        isSearching = true
        deviceList.clear()
    }
        recycler_view.adapter?.notifyDataSetChanged()
        scan_text.visibility = View.GONE
        scan_progress.visibility = View.VISIBLE
        subscription.clear()
        subscription.add(Observable.timer(30, TimeUnit.SECONDS)
            .onErrorReturnNull()
            .applyUISchedulers()
            .doOnUnsubscribe {
                isSearching = false
            }
            .doOnCompleted {
                subscription.clear()
                isSearching = false
                scan_text.visibility = View.VISIBLE
                scan_progress.visibility = View.GONE
            }
            .subscribe())

        subscription.add(
            rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                    //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                    .build())
                .applyUISchedulers()
//                        여기서 필터를 적용해서 list에 넣어준다.

                .filter { it.bleDevice.name != null && (it.bleDevice.name!!.toLowerCase().startsWith("welt") || it.bleDevice.name!!.contains("welt") || it.bleDevice.name!!.toLowerCase().startsWith("ilmo") || it.bleDevice.name!!.toLowerCase().startsWith("beanpole")) }
                .doOnUnsubscribe {
                    isSearching = false
                }
                .subscribe({
                    updateDevice(it.bleDevice)
                }, {
                        error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Error during scanning devices.", Toast.LENGTH_SHORT).show()
                    isSearching = false
                    scan_text.visibility = View.VISIBLE
                    scan_progress.visibility = View.GONE
                }, {
                    isSearching = false
                    scan_text.visibility = View.VISIBLE
                    scan_progress.visibility = View.GONE
                }))


    }

    fun setLayout() {

        val llm = LinearLayoutManager(this)

        llm.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = llm
        recycler_view.adapter = DeviceAdapter()
        scan.setOnClickListener { scanDevices() }
    }

    fun updateDevice(device: RxBleDevice) {
        if (deviceList.size != 0) {
            for (idx in (0..deviceList.size - 1)) {
                if (deviceList[idx].macAddress == device.macAddress) {
                    return
                }
            }
        }
        deviceList.add(device)
        recycler_view.adapter?.notifyDataSetChanged()
    }

    fun viewSubscribe() {
        viewSubscription.add(
            addressSubject
                .observeOn(AndroidSchedulers.mainThread())
                .onBackpressureDrop()
                .subscribe({

                    val intent = Intent(this, TutorialSettingActivity::class.java)
//                            intent는 클래스를 실체화 한것.
//                            println("viewSubscibe in")
                    intent.putExtra("device", it)

                    //**
                    //블루투스 디바이스 공유 변수로 저장
                    val pref: SharedPreferences = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE)
                    var deviceData: BleDevice = it

                    val editor: SharedPreferences.Editor = pref.edit()
                    var gson = Gson()
                    var json:String = gson.toJson(deviceData)
                    editor.putString("device", json)
                    editor.commit()
                    //**

//                            intent.putExtra("device", PaperParcels.wrap<BleDevice>(it))

//                            intent.putExtra("device", PaperParcels.wrap(it) as Parcelable?)

//                            intent.putExtra("device", PaperParcels.wrap(it) as Parcelable)
//                            intent.putExtra("device", PaperParcels.wrap(it) as Parcelable)
//                            intent.putParcelable("device", PaperParcels.wrap(it))
//                            BleDeviceParcel이 뭐하는 건지 모르겠다.
//                            여기서 BleDeviceParcel이란 넘겨줄 데이터값을 의미한다. 체크액티비티로 이값을 넘겨줘야한다.
//                            그럼 이값이란 무엇일까?
//                            블루투스 기계겠지 그래서 it : bleDevice라서 그냥 it을 넘겨보자.
//                            it을 넘겨준결과 factory 어플을 중단합니다 라는 메시지가 나옴.
//                            선언이 되어있지않아서 오류 발생한다.


                    startActivity(intent)
//                            intent로 다른 액티비티로 넘기는 것이다.
                    finish()
                })
        )
    }

    inner class DeviceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

            fun bind(device: RxBleDevice) {

                val addressArr = device.macAddress.split(":")

                v.tv_inch.text = "${device.name} ${addressArr[1]}${addressArr[0]}"
                v.address.text = device.macAddress
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder as ViewHolder

            v.bind(deviceList[position])
            v.itemView.setOnClickListener {
                addressSubject.onNext(BleDevice(deviceList[position].macAddress,
                    deviceList[position].name.toString()
                ))
            }
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val v = LayoutInflater.from(parent.context).inflate(R.layout.listitem_device, null)

            return ViewHolder(v)
        }
    }
}

