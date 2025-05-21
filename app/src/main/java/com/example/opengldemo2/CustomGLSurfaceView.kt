package com.example.opengldemo2

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import kotlin.coroutines.coroutineContext

class CustomGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context,attributeSet) {

    private val renderer: CustomGLRenderer

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        setEGLContextClientVersion(2)
        renderer = CustomGLRenderer(context) { hitFaceName ->
            mainHandler.post {
                Toast.makeText(context, hitFaceName, Toast.LENGTH_SHORT).show()
            }
        }
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        //Normalizing Coordinates
        val x: Float = (e.x / this.width.toFloat()) * 2f - 1f
        val y: Float = -((e.y / this.height.toFloat()) * 2f -1f)

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {

                renderer.normHitX = x
                renderer.normHitY = y
                renderer.clicked = true
            }
        }
        return true
    }

    companion object {
        private const val TAG = "CustomGLSurfaceView"
    }
}
