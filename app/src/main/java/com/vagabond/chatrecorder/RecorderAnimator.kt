package com.vagabond.chatrecorder

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.os.CountDownTimer
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class RecorderAnimator(val context: Context, var hideView: View? = null, val recordView: View) :
        GestureDetector.OnGestureListener {

    private val MAX_RECORD_DUR = 30*60*1000L
    private val TOTAL_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;

    private var isAnimationAllowed = false
    private lateinit var timerTxt: TextView
    private lateinit var slideTxt: TextView
    private lateinit var animateMic: ImageView
    private lateinit var recordMic: ImageView
    private lateinit var gradientView: View
    private lateinit var seperatorView: View
    private  var touchSlop:Int = 0
    private  var recordMicRightMargin:Int = 0
    private lateinit var gestureDetector: GestureDetectorCompat




    override fun onShowPress(e: MotionEvent?) {
        Log.e("showPress", e.toString() + " uuii")
        startRecordView()
    }



    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        // probably show a toast that recording not possible

        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        makeHoldingMicBig(true)
        isAnimationAllowed = true
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.e("on fling", "veloX: " + velocityX + " veloY: " + velocityY)
        if(!isAnimationAllowed)
            return false
        if(velocityX > 20 && velocityY< 10){
            cancelEverything()
            endRecording(true)
        }
        return true
    }



    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.e("on scroll", "distX: " + distanceX + " distY: " + distanceY)
        if (!isAnimationAllowed) return false

        if(distanceX.absoluteValue > 0){
            slideMicOnDrag(e2?.rawX?:0.0f)
        }
        return true

    }

    override fun onLongPress(e: MotionEvent?) {
    }


    init {
        timerTxt = recordView.findViewById(R.id.timer_txt)
        slideTxt = recordView.findViewById(R.id.slide_txt)
        animateMic = recordView.findViewById(R.id.animated_mic)
        recordMic = recordView.findViewById(R.id.holding_mic)
        gradientView = recordView.findViewById(R.id.gradient_view)
        seperatorView = recordView.findViewById(R.id.mic_timer_seperator)

        gestureDetector = GestureDetectorCompat(context, this)
        gestureDetector.setIsLongpressEnabled(false)
        recordMicRightMargin = (recordMic.layoutParams as ConstraintLayout.LayoutParams).marginEnd
//    recordMic.post({
//        recordMicHeight = recordMic.height
//        recordMicWidth = recordMic.width
//    })

        recordMic.setOnTouchListener({ v, e ->
            onRecordButtonTouched(e)
        })

        touchSlop = ViewConfiguration.get(recordMic.getContext()).scaledTouchSlop;
    }


    private fun cancelEverything() {
        isAnimationAllowed = false
        makeHoldingMicBig(false)
        slideMicOnDrag(TOTAL_WIDTH.toFloat() - recordMicRightMargin)
        closeRecordView(true)
    }


    private fun onRecordButtonPressed() {

    }

    private fun onRecordButtonTouched(event: MotionEvent): Boolean {
        when (event.action) {
//            MotionEvent.ACTION_DOWN-> makeHoldingMicBig(true)
            MotionEvent.ACTION_UP -> {
                if (isAnimationAllowed){cancelEverything() }
                endRecording(false)
            }
            else -> return gestureDetector.onTouchEvent(event)
        }

        return true
    }

    private fun endRecording(isCancelled:Boolean){
        recordCountdownTimer?.cancel()
        recordCountdownTimer = null
    }

    private fun makeHoldingMicBig(isBig: Boolean) {
        Log.e("isBig", isBig.toString())
        val to = if (isBig) 2.0f else 1f
        recordMic.animate().scaleX(to).scaleY(to).start()
    }

    private val CANCEL_TEXT_FADING_DIST = 40
    private fun slideMicOnDrag(distanceX: Float) {
        val params = recordMic.getLayoutParams() as ConstraintLayout.LayoutParams

        params.marginEnd = TOTAL_WIDTH - distanceX.roundToInt()
        Log.e("right margin", "int: "+ params.marginEnd)
        recordMic.setLayoutParams(params)

        if(slideTxt.x <= seperatorView.x &&  isAnimationAllowed){
            cancelEverything()
            endRecording(true)
        }

    }
    private fun startRecordView() {
        animateMic.visibility = View.VISIBLE
        slideTxt.visibility = View.VISIBLE
        timerTxt.visibility = View.VISIBLE
        gradientView.visibility = View.VISIBLE
        seperatorView.visibility = View.VISIBLE

        val params = recordView.layoutParams
        if(params is ConstraintLayout.LayoutParams){

            val constraintSet = ConstraintSet()
            constraintSet.clone(recordView.parent as ConstraintLayout)
            constraintSet.constrainWidth(recordView.id, 0)

            constraintSet.connect(recordView.id,ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            constraintSet.connect(recordView.id,ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            constraintSet.applyTo(recordView.parent as ConstraintLayout)
        }else if(params is RelativeLayout.LayoutParams){
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            recordView.layoutParams = params
        }

        animateMic.alpha = 0.0f
        slideTxt.alpha = 0.0f
        timerTxt.alpha = 0.0f
        val animMicAnimator = ObjectAnimator.ofFloat(animateMic, "alpha", 0.0f,1.0f)
        val slideTxtAnimator = ObjectAnimator.ofFloat(slideTxt, "alpha", 0.0f,1.0f)
        val timerTxtAnimator = ObjectAnimator.ofFloat(timerTxt, "alpha", 0.0f,1.0f)

        val animator = AnimatorSet()
        animator.playTogether(animMicAnimator,slideTxtAnimator,timerTxtAnimator)
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                startAnimateMicRepeat()
                startRecording()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        animator.start()
    }

    private var recordCountdownTimer:CountDownTimer?= null

    private fun startRecording() {
        recordCountdownTimer = object : CountDownTimer(MAX_RECORD_DUR,1000L ){
            override fun onFinish() {
                if(isAnimationAllowed) {
                    cancelEverything()
                    endRecording(true)
                }
                recordCountdownTimer = null
            }

            override fun onTick(millisUntilFinished: Long) {
                onRecordingTimepass(MAX_RECORD_DUR - millisUntilFinished)
            }

        }
        recordCountdownTimer?.start()
    }

    private fun onRecordingTimepass(timePassInMillis: Long) {
        val totSecs = timePassInMillis/1000
        val secs = totSecs%60
        val mins = totSecs/60
        val elapsedTime = String.format("%02d:%02d", mins,secs)
        timerTxt.setText(elapsedTime)
    }

    private var animateMicAnimator:ObjectAnimator?= null
    private fun startAnimateMicRepeat() {
        animateMicAnimator = ObjectAnimator.ofFloat(animateMic, "alpha", 1.0f, 0.0f)

        animateMicAnimator?.duration = 500
        animateMicAnimator?.repeatCount = ValueAnimator.INFINITE
        animateMicAnimator?.repeatMode = ValueAnimator.REVERSE
        animateMicAnimator?.start()
    }

    private fun closeRecordView(isRecordingSuccess:Boolean) {




        val animMicAnimator = ObjectAnimator.ofFloat(animateMic, "alpha", 1.0f,0.0f)
        val slideTxtAnimator = ObjectAnimator.ofFloat(slideTxt, "alpha", 1.0f,0.0f)
        val timerTxtAnimator = ObjectAnimator.ofFloat(timerTxt, "alpha", 1.0f,0.0f)

        val animator = AnimatorSet()
        animator.playTogether(animMicAnimator,slideTxtAnimator,timerTxtAnimator)
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                animateMic.visibility = View.GONE
                slideTxt.visibility = View.GONE
                timerTxt.visibility = View.GONE
                gradientView.visibility = View.GONE
                seperatorView.visibility = View.GONE
                val params = recordView.layoutParams
                if(params is ConstraintLayout.LayoutParams){

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(recordView.parent as ConstraintLayout)
                    constraintSet.constrainWidth(recordView.id, ConstraintSet.WRAP_CONTENT)

                    constraintSet.clear(recordView.id,ConstraintSet.START)
                    constraintSet.connect(recordView.id,ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                    constraintSet.applyTo(recordView.parent as ConstraintLayout)
                }else if(params is RelativeLayout.LayoutParams){
                    params.width = RelativeLayout.LayoutParams.WRAP_CONTENT
                    params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    recordView.layoutParams = params
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        animateMicAnimator?.cancel()
        animateMicAnimator = null
        animator.start()
    }
}