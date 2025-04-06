package fr.ceri.amiibo.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import fr.ceri.amiibo.ui.GameActivity
import kotlin.math.abs

class OnSwipeTouchListener(
    private val context: Context,
    private val gameActivity: GameActivity
) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(e: MotionEvent) = true

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - (e1?.x ?: 0f)
            val diffY = e2.y - (e1?.y ?: 0f)
            return when {
                abs(diffX) > abs(diffY) -> {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) onSwipeRight() else onSwipeLeft()
                        true
                    } else false
                }
                else -> false
            }
        }

        private fun onSwipeLeft() {
            gameActivity.switchToGameSeriesQuestion()
        }

        private fun onSwipeRight() {
            gameActivity.switchToNameQuestion()
        }
    }
}