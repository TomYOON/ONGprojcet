package com.example.ongprojcet

import android.content.Context
import com.polidea.rxandroidble.RxBleClient
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

object BLE {
    //연결
    enum class Action { PASSWORD, PASSWORD_WRITE, DEMO, INIT, RAW_ACCELEROMETER }

    val controlPointUUID: UUID = UUID.fromString("2d86686a-53dc-25b3-0c4a-f0e10c8dee20")
    val firmwareUUID: UUID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb")
    val notifyUUID: UUID = UUID.fromString("5a87b4ef-3bfa-76a8-e642-92933c31434f")

    private val rxBleClient: RxBleClient by lazy { RxBleClient.create(context!!) }

    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
    }

    fun client(): RxBleClient {
        return rxBleClient
    }

    fun byteFactory(a: Action, data: IntArray?): ByteArray {
        val b = ByteArray(8)
        b[1] = 0x00
        when (a) {
            Action.PASSWORD -> {
                b[0] = 0x10
                b[2] = 0xFF.toByte()
                b[3] = 0xFF.toByte()
                b[4] = 0xFF.toByte()
                b[5] = 0xFF.toByte()
            }
            Action.PASSWORD_WRITE -> {
                b[0] = 0x12
                b[2] = 0xFF.toByte()
                b[3] = 0xFF.toByte()
                b[4] = 0xFF.toByte()
                b[5] = 0xFF.toByte()
            }
            Action.DEMO -> {
                b[0] = 0x1E
                b[2] = 0x01
            }
            Action.INIT -> {
                b[0] = 0x22
                b[2] = data!![0].toByte()
                b[3] = data[1].toByte()
                b[4] = data[2].toByte()
                b[5] = data[3].toByte()
            }
            Action.RAW_ACCELEROMETER -> {
                b[0] = 0x21
                b[2] = 0x01
            }
        }
        return b
    }

    // 값을 받아옴. byte로
    fun readRawDataCombine(data: ByteArray): HashMap<String, Any> {
        var x1 = data[0].toInt() and 0xff
        var x2 = data[1].toInt() and 0xff


        var y1 = data[2].toInt() and 0xff
        var y2 = data[3].toInt() and 0xff
        var z1 = data[4].toInt() and 0xff
        var z2 = data[5].toInt() and 0xff

        var x = (x1 shl 24 or (x2 shl 16)) / (65536)
        var y = (y1 shl 24 or (y2 shl 16)) / (65536)
        var z = (z1 shl 24 or (z2 shl 16)) / (65536)

        val map = HashMap<String, Any>()
        map.put("x", x)
        map.put("y", y)
        map.put("z", z)

        return map
    }

    fun <T> Observable<T>.applyUISchedulers(): Observable<T> { //rx자바가 쓸 메소드를 정해줌
        return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}