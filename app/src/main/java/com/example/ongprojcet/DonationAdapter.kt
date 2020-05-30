package com.example.ongprojcet

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble.RxBleDevice
import kotlinx.android.synthetic.main.listitem_device.view.*
import kotlinx.android.synthetic.main.listitem_donation.view.*

class DonationAdapter(val list: List<Donation>, val context:Context):
    RecyclerView.Adapter<DonationAdapter.ViewHolder>() {

    lateinit var mParent: ViewGroup;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_donation,parent,false)
        mParent = parent
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        //click event
        val listener = View.OnClickListener { it->
            //Toast.makeText(it.context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()

            //기부하기 창 버튼 누르면 뜨는 custom dialong
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(mParent.context).inflate(R.layout.custom_dialog, null)
            val dalongET = dialogView.findViewById<EditText>(R.id.donation_dialong_et)

            var pref = it.context.getSharedPreferences("checkFirst", 0)
            val point: Int = pref.getInt("point", -1)  //current point

            var currentPointTv = dialogView.findViewById<TextView>(R.id.current_point)
            currentPointTv.text = "현재 포인트: " + point.toString()  //현재 포인트 포이게

            builder.setView(dialogView)
                .setPositiveButton("기부하기"){ dialogInterface, i ->
                    var money = -1
                    if(dalongET.text.toString() != "")
                        money= Integer.parseInt(dalongET.text.toString())  //기부하겠다고 입력한 금액

                    if(point == -1)
                        Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show()  //point 변수 목 읽어오면
                    else if(money == -1)
                        Toast.makeText(context, "기부 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                    else if(point < money)
                        Toast.makeText(context, "포인트가 부족합니다", Toast.LENGTH_SHORT).show()  //가지고 있는 포인트보다 많이 기부하려고 하면
                    else{
                        val editor: SharedPreferences.Editor = pref.edit()
                        editor.putInt("point", point-money).apply()  //포인트 갱신
                        Toast.makeText(context, money.toString() +" 포인트를 기부하였습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
        holder.apply {
            bindItems(listener, item, context)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(listener:View.OnClickListener, d:Donation, context: Context){
            itemView.donation_title.text = d.title
            itemView.donation_purpose.text = d.purpose
            itemView.donation_date.text = d.startDate +" - "+d.endDate
            itemView.donation_center.text = d.center

            itemView.donation_btn.setOnClickListener(listener)  //attach listener
        }
    }
}