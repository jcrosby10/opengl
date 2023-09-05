package com.example.myapplication.ui.theme

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock

/**
 * A Renderer is responsible for interacting with OpenGL to draw the frame information in the framebuffer
 */
class MyGLRenderer : GLSurfaceView.Renderer {

    // shapes
    private lateinit var triangle: Triangle

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    var angle: Float = 0f

    // animations
    private val rotationMatrix = FloatArray(16)

    // called when the surface is created or recreated
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    // called continuously to draw the frame
    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT)

        val scratch = FloatArray(16)
        triangle = Triangle()

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Create a rotation transformation for the triangle
//        val time = SystemClock.uptimeMillis() % 4000L
//        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw shape
        triangle.draw(scratch)
    }

    // called when the surface size changes
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
//        glViewport(0, 0, width, height)
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}

// load and compile shaders
fun loadShader(type: Int, shaderCode: String): Int {
    return glCreateShader(type).also { shader ->
        // add the source code to the shader and compile it
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
    }
}