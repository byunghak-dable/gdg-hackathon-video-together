package org.personal.videotogether.util.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import org.personal.videotogether.R

class SingleViewTouchableMotionLayout(context: Context, attributeSet: AttributeSet? = null) : MotionLayout(context, attributeSet) {

    private val viewRect = Rect()
    private var touchStarted = false

    private val viewToDetectTouch by lazy {
        findViewById<View>(R.id.playerContainerCL)
    }

    private val gestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                viewToDetectTouch.getHitRect(viewRect)
                return viewRect.contains(e1.x.toInt(), e1.y.toInt())
            }
        }
    }

    private val gestureDetector by lazy {
        GestureDetector(context, gestureListener)
    }

    init {
        setTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                touchStarted = false
            }
        })
    }


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.i("TAG", "onTouchEvent: actionMasked ${event.actionMasked}")
        Log.i("TAG", "onTouchEvent: ACTION_UP ${MotionEvent.ACTION_UP}")
        Log.i("TAG", "onTouchEvent: ACTION_CANCEL ${MotionEvent.ACTION_CANCEL}")
        Log.i("TAG", "onTouchEvent: ACTION_DOWN ${MotionEvent.ACTION_DOWN}")

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchStarted = false
                return super.onTouchEvent(event)
            }
        }
        if (!touchStarted) {
            viewToDetectTouch.getHitRect(viewRect)
            touchStarted = viewRect.contains(event.x.toInt(), event.y.toInt())
        }
        return touchStarted && super.onTouchEvent(event)
    }
}