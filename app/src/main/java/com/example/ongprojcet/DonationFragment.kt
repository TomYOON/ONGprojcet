package com.example.ongprojcet

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_donation.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

class DonationFragment : Fragment(){
    var adapter:DonationAdapter? = null
    var donationList = arrayListOf<Donation>()  //API 검색 결과로 나오는 기부 목록 넣을 리스트
    var donationCenterList = mutableListOf<String>()  //같은 센터 기부 목록 중복 제거를 위해 센터 이름만 모아놓는 리스트 따로 저장

    var keywordList = arrayOf("노인", "아동", "동물", "저소득층")  //스피너에 나올 키워드 목록

    var keyword:String = "노인"  //처음값(노인)이 기본 값

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getData(keyword)  //기본

        //spinner
        donation_spinner.setSelection(0)  //처음 요소가 기본값
        donation_spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, keywordList)
        donation_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                keyword = keywordList[position]
                getData(keyword)  //다시 API 검색
            }
        }

        //recyclerView
        adapter = DonationAdapter(donationList, requireContext())
        donation_rv.layoutManager = LinearLayoutManager(requireContext())
        donation_rv.setHasFixedSize(true)
        donation_rv.adapter = adapter
        //포인트 접근 법 : requireActivity().getSharedPreferences("checkFirst", 0).getInt("point", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_donation, container, false)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getData(mKeyword: String){  //API
        Thread({
            donationList.clear()
            donationCenterList.clear()

            var pageNo:Int = 1  //끝까지 검색하기 위한 페이지 번호. 1부터 시작해서 검색 안나올때까지 진행
            while(true){
                var url = "http://openapi.1365.go.kr/openapi/service/rest/ContributionGroupService/getCntrGrpProgramList?keyword="+ mKeyword + "&pageNo=" + pageNo
                val xml : Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url)

                xml.documentElement.normalize()
                println("Root element : "+xml.documentElement.nodeName)

                //찾고자 하는 데이터가 어느 노드 아래에 있는지 확인
                val list: NodeList =xml.getElementsByTagName("item")

                if(list.length==0)
                    break;  //검색된거 없으면 while문 탈출. 페이지 끝까지 다 본거임

                for(i in 0..list.length-1){
                    var n: Node =list.item(i)
                    if(n.getNodeType()== Node.ELEMENT_NODE){
                        val elem=n as Element
                        val map=mutableMapOf<String,String>()

                        for(j in 0..elem.attributes.length - 1) {
                            map.putIfAbsent(elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
                        }

                        var title = elem.getElementsByTagName("reprsntSj").item(0).textContent
                        var contents = elem.getElementsByTagName("rcritSj").item(0).textContent
                        var purpose = elem.getElementsByTagName("rcritPurps").item(0).textContent
                        var startDate = elem.getElementsByTagName("rcritBgnde").item(0).textContent
                        var endDate = elem.getElementsByTagName("rcritEndde").item(0).textContent
                        var center = elem.getElementsByTagName("rcritrNm").item(0).textContent

                        //중복검사
                        if(donationCenterList.contains(center))
                            continue  //중복이라면 다음 item으로
                        else
                            donationCenterList.add(center)

                        donationList.add(Donation(title, center, purpose, contents, startDate, endDate))

                        /*
                        println("=========${i+1}=========")
                        println("1. 제목 : ${elem.getElementsByTagName("reprsntSj").item(0).textContent}")
                        println("2. 요약내용 : ${elem.getElementsByTagName("rcritSj").item(0).textContent}")
                        println("3. 모집목적 : ${elem.getElementsByTagName("rcritPurps").item(0).textContent}")
                        println("4. 모집시작일 : ${elem.getElementsByTagName("rcritBgnde").item(0).textContent}")
                        println("5. 모집완료일 : ${elem.getElementsByTagName("rcritEndde").item(0).textContent}")
                        println("6. 모집기관명 : ${elem.getElementsByTagName("rcritrNm").item(0).textContent}")
                        */
                    }
                }
                pageNo++  //다음 페이지 검색을 위해 페이지 값 1 증가 시킴
            }

            activity?.runOnUiThread({
                adapter?.notifyDataSetChanged()  //어댑터한테 리스트 값 바꼈다고 알려주기
            })
        }).start()
    }
}