package com.example.ongprojcet

import android.os.Parcel
import android.os.Parcelable

//import nz.bradcampbell.paperparcel.PaperParcel
//import nz.bradcampbell.paperparcel.PaperParcelable

//@PaperParcel
//data class BleDevice(
//        val address: String,
//        val name: String
//) : PaperParcelable {
//    companion object {
//      @JvmField val CREATOR = PaperParcelable.Creator(BleDevice::class.java)
//    }
//
//    companion object {
////        @JvmField val CREATOR = PaperParcelable.Creator(BleDevice::class.java)
//            @JvmField val CREATOR = PaperParcelBleDevice.CREATOR
//    }


//    PaperParcel을 이렇게하면 가져야된다
//    {className}Parcel이 자동으로 생성되야되는데...
//   Parcel을 통해 읽고 쓸수 있다.
//}

class BleDevice constructor(var address: String, var name: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BleDevice> {
        override fun createFromParcel(parcel: Parcel): BleDevice {
            return BleDevice(parcel)
        }

        override fun newArray(size: Int): Array<BleDevice?> {
            return arrayOfNulls(size)
        }
    }
}