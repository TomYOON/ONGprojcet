package com.example.ongprojcet

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
//import android.support.v4.app.ActivityCompat
//import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class PermissionActivity : AppCompatActivity() {

    //    bluetoothAdapter는 장치의 블루투스 어댑터를 반환하고, 장치가 블루투스를 지원하지 않으면 null을 반환한다.
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private var bluetooth: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        checkBluetooth()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            bluetooth = true
            checkLocation()
            return
        }
        checkBluetooth()
        return
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        checkLocation()
    }

    //    블루투스 장치가 활성상태인지 검사해야한다.
    fun checkBluetooth() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            // 블루투스를 지원하지 않거나, 지원하는데 비활성화 상태일경우 블루투스 활성화 하기 위해 동의를 구하는 다이얼로그를 출력
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            startActivityForResult(intent, 1000)
        } else {
//            블루투스를 지원하고 활성화 상태일경우.
            bluetooth = true
            checkLocation()
        }
    }

    fun checkLocation() {

        val p = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//해당 app이 특정 권한을 가지고 있는지 검사한다.
        if (p == PackageManager.PERMISSION_DENIED) {
//            해당 App이 권한이 없으면
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                2000)
        } else {
//            해당 app이 권한이 있으면
            if (isLocationEnabled()) {

                val intent = Intent(this, BluetoothActivity::class.java)

                startActivity(intent)
                finish()
            } else {

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                startActivity(intent)
            }
        }
    }

    fun isLocationEnabled(): Boolean {
//        SDK 23 버전부터는 퍼미션 관리를 해줘야된다.
//        퍼미션 관리 해주는 부분.

        var locationMode = 0
        val locationProviders: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF

        }
        locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        return !TextUtils.isEmpty(locationProviders)
    }
}