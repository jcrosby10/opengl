package com.example.myapplication.ui.theme

import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glDisableVertexAttribArray
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of coordinates per vertex in this array
// by default the coordinate system is 0,0,0 is the center of the screen and 1,1,0 is the top right of the frame and -1,-1,0 is the bottom left

private const val COORDS_PER_VERTEX = 3

class Triangle {

    private val triangleCoords = floatArrayOf( // in counterclockwise order:
        0.0f, 0.62200844f, 0.0f,      // top
        -0.5f, -0.31100425f, 0.0f,    // bottom left
        0.5f, -0.31100425f, 0.0f      // bottom right
    )

    // shaders must be compiled before using
    // vertex shaders handle the positioning
    private val vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    // fragment shaders handles the coloring
    // fragment shaders provide color information
    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vPMatrixHandle: Int = 0

    private var program: Int

    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    init {
        val vertexShader: Int = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        program = glCreateProgram().also {

            // add the vertex shader to program
            glAttachShader(it, vertexShader)

            // add the fragment shader to program
            glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        glUseProgram(program)

        // get handle to vertex shader's vPosition member
        positionHandle = glGetAttribLocation(program, "vPosition").also {

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // get handle to fragment shader's vColor member
            colorHandle = glGetUniformLocation(program, "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                glUniform4fv(colorHandle, 1, color, 0)
            }

            // get handle to shape's transformation matrix
            vPMatrixHandle = glGetUniformLocation(program, "uMVPMatrix")

            // Pass the projection and view transformation to the shader
            glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

            // Draw the triangle
            glDrawArrays(GL_TRIANGLES, 0, vertexCount)

            // Disable vertex array
            glDisableVertexAttribArray(it)
        }
    }

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
}