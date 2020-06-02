package com.example.ongprojcet

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_donation.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    lateinit var asyncTask: currentTask
    lateinit var videoId:String;
    private val youtubeAPIKey = "AIzaSyBoFWKSgwIAjZVjJASoO8_cVwOzjCaq6NY";  //안써도 왜 되는걸까 신기함

    var videoTitleList = arrayOf(
        "바르게 앉는 법",
        "오래 앉아 있는 사람들을 위한 영상",
        "디스크 있는 사람들을 위한 영상",
        "디스크 자가 진단과 스트레칭",
        "앉아서 하는 스트레칭1",
        "앉아서 하는 스트레칭2")  //스피너에 나올 키워드 목록

    var videoIdList = arrayOf(
        "Rjv7hnHkgXE",
        "KNvldxi8TwU",
        "_RXjbRdiFBs",
        "N1SzXAHUhvo",
        "-JzaMksAeew",
        "5V8a5_LXXQs"
    )

    override fun onPause() {
        //프레그먼트 바뀔 때 asynctask 종료
        Log.v("fragment", "onPause")
        asyncTask.cancel(true)
        super.onPause()
    }

    fun getYoutube(){
        //video
        youtubeThumbnailView.initialize("develop", object : YouTubeThumbnailView.OnInitializedListener {
            override fun onInitializationSuccess(youTubeThumbnailView: YouTubeThumbnailView, youTubeThumbnailLoader: YouTubeThumbnailLoader) {
                youTubeThumbnailLoader.setVideo(videoId)
                youTubeThumbnailLoader.setOnThumbnailLoadedListener(object : YouTubeThumbnailLoader.OnThumbnailLoadedListener {
                    override fun onThumbnailLoaded(youTubeThumbnailView: YouTubeThumbnailView, s: String) {
                        youTubeThumbnailLoader.release()
                    }

                    override fun onThumbnailError(youTubeThumbnailView: YouTubeThumbnailView, errorReason: YouTubeThumbnailLoader.ErrorReason) {}
                })
            }

            override fun onInitializationFailure(youTubeThumbnailView: YouTubeThumbnailView, youTubeInitializationResult: YouTubeInitializationResult) {}
        })

        youtubeThumbnailView.setOnClickListener {
            //마지막 인자 true로 하면 전체화면 XX
            val intent = YouTubeStandalonePlayer.createVideoIntent(activity, "develop", videoId, 0, true, false)
            startActivity(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //기본 UI
        animation.setAnimation("loading.json")
        animation.loop(true)
        animation.playAnimation()

        //화살표
        up.setImageResource(R.drawable.upw)
        left.setImageResource(R.drawable.leftw)
        right.setImageResource(R.drawable.rightw)
        down.setImageResource(R.drawable.downw)
        center.setImageResource(R.drawable.heartw)

        //spinner
        youtube_spinner.setSelection(0)  //처음 요소가 기본값
        youtube_spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, videoTitleList)
        youtube_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                Log.v("spinner check", "listener 호출됨")
                videoId = videoIdList[position]
                getYoutube()
            }
        }


        Log.v("fragment", "onViewCreated")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        Log.v("fragment", "onResume")

        //홈버튼 누리고 돌아오면 onViewCreated로 안가고 resume으로 옴.
        //그래서 onResume에서 asyncTask 호출
        asyncTask = currentTask(requireContext(), LR_tv, FB_tv, animation, left, right, up, down, center)
        asyncTask.execute()  //start asyncTask

        super.onResume()
    }

    //AsyncTask
    class currentTask(
        mContext:Context,
        LR_tv:TextView,
        FB_tv:TextView,
        animation:com.airbnb.lottie.LottieAnimationView,
        left:ImageView,
        right:ImageView,
        up:ImageView,
        down:ImageView,
        center:ImageView):AsyncTask<Void, Void, Void>() {

        lateinit var mContext:Context
        lateinit var LR_tv:TextView
        lateinit var FB_tv:TextView
        lateinit var animation:com.airbnb.lottie.LottieAnimationView
        lateinit var left:ImageView
        lateinit var right:ImageView
        lateinit var up:ImageView
        lateinit var down:ImageView
        lateinit var center:ImageView

        lateinit var pref: SharedPreferences
        var currentLR: Float = 0.0f
        var currentFB: Float = 0.0f
        var goodLR: Float = 0.0f
        var goodFB: Float = 0.0f
        var status: Int = 0
        var isBluetoothRunning:Boolean = false;

        init {
            this.mContext = mContext
            this.LR_tv = LR_tv
            this.FB_tv = FB_tv
            this.animation = animation
            this.left = left
            this.right = right
            this.up = up
            this.down = down
            this.center = center

            pref = this.mContext.getSharedPreferences("checkFirst", 0)

            //init good
            //goodLR = ...
            //goodFB = ...
        }


        override fun doInBackground(vararg p0: Void?): Void? {
            Log.v("SHP_isBluetoothRunning홈", pref.getBoolean("isBluetoothRunning", false).toString())
            while(!isCancelled){
                //현재 값 가져오기
                isBluetoothRunning = pref.getBoolean("isBluetoothRunning", false);

                if(isBluetoothRunning) {
                    currentLR = pref.getFloat("currentLR", (-200.0).toFloat())
                    currentFB = pref.getFloat("currentFB", (-200.0).toFloat())

                    //status는 안좋을때마다 ++
                    //status == 0 => good
                    //status == [1,2] => soso
                    //status > 3 => bad
                    //플마5도 이내면 그대로
                    //플마5초과 10이하면 +1
                    //플마 10초과면 +2

                    status = 0

                    //LR
                    if (goodLR - 5 <= currentLR && currentLR <= goodLR + 5) {  //~ 5도
                        //좋은 자시
                    } else if (goodLR - 10 <= currentLR && currentLR <= goodLR + 10)  //5도 ~ 10도
                        status = status + 1
                    else  //10도 ~
                        status = status + 2

                    //FB
                    if (goodFB - 5 <= currentFB && currentFB <= goodFB + 5) {
                        //좋은 자시
                    } else if (goodFB - 10 <= currentFB && currentFB <= goodFB + 10)
                        status = status + 1
                    else
                        status = status + 2

                    Log.v("current_LR", currentLR.toString())
                    Log.v("current_FB", currentFB.toString())
                    Log.v("current_status", status.toString())

                    /*
                if(isFirstCurrent){
                    //처음에는 서비스 도는데 좀 걸리니까 시간 좀 길게
                    Thread.sleep(5000)
                    isFirstCurrent = false
                }
                else
                    Thread.sleep(2000)  //안하면 에러남... 메모리 너무 많이 쓴다고... 1000으로 해도 됨
                */

                    publishProgress()  //update UI

                    Thread.sleep(1000)
                }
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {
            if (status == 0) {
                animation.setAnimation("good.json")
                animation.loop(true)
                animation.playAnimation()
            } else if (status <= 2) {
                animation.setAnimation("soso.json")
                animation.loop(true)
                animation.playAnimation()
            } else {
                animation.setAnimation("bad.json")
                animation.loop(true)
                animation.playAnimation()
            }

            lateinit var LR_text:String
            lateinit var FB_text:String

            var isGood:Boolean = true

            //arrow
            if(currentLR > goodLR+5){
                isGood = false
                left.setImageResource(R.drawable.leftw)
                right.setImageResource(R.drawable.rightb)
            }
            else if(currentLR < goodLR-5){
                isGood = false
                left.setImageResource(R.drawable.leftb)
                right.setImageResource(R.drawable.rightw)
            }
            else{
                left.setImageResource(R.drawable.leftw)
                right.setImageResource(R.drawable.rightw)
            }

            if(currentFB > goodFB+5){
                isGood = false
                down.setImageResource(R.drawable.downw)
                up.setImageResource(R.drawable.upb)
            }
            else if(currentFB < goodFB-5){
                isGood = false
                down.setImageResource(R.drawable.downb)
                up.setImageResource(R.drawable.upw)
            }
            else{
                down.setImageResource(R.drawable.downw)
                up.setImageResource(R.drawable.upw)
            }

            if(isGood)
                center.setImageResource(R.drawable.heartr)
            else
                center.setImageResource(R.drawable.heartw)

            //Textview
            if (currentLR > goodLR)
                LR_text = "오른쪽으로 " + (currentLR-goodLR).toString() + "도"
            else if (currentLR < goodLR)
                LR_text = "왼쪽 " + (-1 * (currentLR-goodLR)).toString() + "도"
            else
                LR_text = "좌우기준 정가운데"

            if (currentFB > goodFB)
                FB_text = "앞으로 " + (currentFB-goodFB).toString() + "도"
            else if (currentFB < goodFB)
                FB_text = "뒤로 " + (-1 * (currentFB-goodFB)).toString() + "도"
            else
                FB_text = "앞뒤기준 정가운데"

            LR_tv.setText(LR_text)
            FB_tv.setText(FB_text)

            super.onProgressUpdate(*values)
        }

    }
}