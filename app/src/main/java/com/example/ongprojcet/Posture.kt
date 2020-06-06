package com.example.ongprojcet

import java.util.ArrayList

class Posture(){
    var lrAngleList = ArrayList<Int>()
    var fbAngleList = ArrayList<Int>()
    var goodPosture = 0
    var badPosture = 0

    var badPostureContinueCount = 0

    var postureString = arrayOf("앞+오른쪽","앞","앞+왼쪽","왼 쪽","good","오른쪽","뒤 + 왼쪽","뒷 쪽","뒤+오른쪽")
    var postures = IntArray(9){0} // 자세 count 저장하는 array

    var timeCount = 0

    val abnormalThreshold = 10 //안좋은 자세의 정도 : 10도를 넘기면 안좋다고 일단 작성
    val abnormalContThreshold = 10 //안좋은 자세를 10번 연속으로 취하면 안좋다고 생각하고 일단 작성
    val secPerData = 5



    //lrAngle - : 좌, + : 우  fbAngle - : 뒤, +:앞
    //    front
    //    0 1 2
    //    3 4 5  right      <-- 자세 경우의 수
    //    6 7 8
    fun checkPosture(lrAngle: Int, fbAngle: Int): Int {
        if(lrAngle > abnormalThreshold){  //오른쪽으로 기울었을 경우
            if(fbAngle > abnormalThreshold) { //오른쪽 + 앞
                return 2
            }
            else if(fbAngle < -abnormalThreshold) { //오른쪽 + 뒤
                    return 8
            }
            return 5  //오른쪽만
        } else if(lrAngle < -abnormalThreshold) { //왼쪽인 경우
            if(fbAngle > abnormalThreshold) { //왼쪽 + 앞
                return 0
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
        postures[index] += 1
        if(index == 4) {
            goodPosture +=1
            badPostureContinueCount = 0
        }
        else {
            badPosture +=1
            badPostureContinueCount += 1
        }
        this.lrAngleList.add(lrAngle)
        this.fbAngleList.add(fbAngle)
    }

    @Override
    fun putData(lrAngle: Int, fbAngle:Int, interval:Int)  {
        /*interval에 초를 입력하면 초당 5개 들어온다고 가정하여 5개 데이터중 1개만 받음*/

        var perData = interval*secPerData
        timeCount %= perData
        timeCount += 1
        if(perData % timeCount == 0){
            var index = checkPosture(lrAngle,fbAngle)
            postures[index] += 1
            if(index == 4) {
                goodPosture +=1
                badPostureContinueCount = 0
            }
            else {
                badPosture +=1
                badPostureContinueCount += 1
            }
            this.lrAngleList.add(lrAngle)
            this.fbAngleList.add(fbAngle)
        }


    }



    fun getContinueAlarm() : String {
        val posture2 = postures
        posture2[4] -= postures.max()!! //good이 가장 많을 경우 제외
        val maxIdx = posture2.indices.maxBy { posture2[it] } ?: -1


        return "연속적으로 나쁜자세를 취했어요! 주로 " + postureString[maxIdx] + " 방향으로  몸이 기울어요!"
    }

    fun getIntervalAlarm() : String {
        if (badPosture == 0) {
            return "좋은 자세!"
        }
        var returnMsg = "나쁜 자세 비율: ${100*badPosture/(goodPosture+badPosture)}%, 그 중 "

        for (i in 0 until 9){
            if(i==4) continue
            returnMsg += postureString[i] + "-${100*postures[i]/badPosture}%, "
        }
        return returnMsg
    }

    fun testAlarm() : String {
        var returnMsg = "goodPosture: ${goodPosture}, badPosture: ${badPosture}"

        for (i in 0 until 9) {
            returnMsg += postureString[i] + "- ${postures[i]} "
        }
        return returnMsg
    }

    fun isBadContinue(continueNum : Int = 10): Boolean {
        if(badPostureContinueCount > continueNum) return true
        return false
    }

    fun resetData(){
        lrAngleList = ArrayList<Int>()
        fbAngleList = ArrayList<Int>()
        goodPosture = 0
        badPosture = 0

        badPostureContinueCount = 0


        postures = IntArray(9){0} // 자세 count 저장하는 array


    }

}
