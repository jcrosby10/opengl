package com.example.myapplication.ui.theme

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f

/**
 * A SurfaceView is responsible for initializing and OpenGL.
 */
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

//    var useBasicRenderer = true

    private val renderer: Renderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    init {

        // useful when there are a lot of object changes without user interaction
//        renderMode = RENDERMODE_WHEN_DIRTY // only render the view when there is a change in the data

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer =
//            MyGLRenderer()
            RollingCloudsRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }

//    override fun onTouchEvent(e: MotionEvent): Boolean {
//        val x: Float = e.x
//        val y: Float = e.y
//
//        when (e.action) {
//            MotionEvent.ACTION_MOVE -> {
//
//                var dx: Float = x - previousX
//                var dy: Float = y - previousY
//
//                // reverse direction of rotation above the mid-line
//                if (y > height / 2) {
//                    dx *= -1
//                }
//
//                // reverse direction of rotation to left of the mid-line
//                if (x < width / 2) {
//                    dy *= -1
//                }
//
//                (renderer as MyGLRenderer).angle += (dx + dy) * TOUCH_SCALE_FACTOR
//                requestRender()
//            }
//        }
//
//        previousX = x
//        previousY = y
//        return true
//    }
}