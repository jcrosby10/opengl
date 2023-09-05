package com.example.myapplication.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glBufferData
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDisableVertexAttribArray
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenBuffers
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glGenVertexArrays
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.example.myapplication.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RollingCloudsRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val vertexShaderCode =
        """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform float uTime;
        void main() {
            vec2 cloudOffset = vTexCoord + vec2(0.0, uTime * 0.02);
            vec4 cloudColor = texture2D(uTexture, cloudOffset);
            gl_FragColor = cloudColor;
        }
        """.trimIndent()

    private val cloudTexture: Int = R.drawable.cloud // Replace with your cloud texture image

    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f
    )

    private lateinit var vertexBuffer: FloatBuffer

    private val projectionMatrix = FloatArray(16)
    private var modelMatrix = 0
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var shaderProgram: Int = 0
    private var timeLocation: Int = 0
    private var textureLocation: Int = 0
    private var positionLocation: Int = 0
    private var texCoordLocation: Int = 0
    private var textureHandle: Int = 0

    private var startTime: Long = 0

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        compileShaders()
        loadTexture()
        startTime = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        glUseProgram(shaderProgram)

        val currentTime = (System.currentTimeMillis() - startTime) / 1000.0f
        glUniform1f(timeLocation, currentTime)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureHandle)
        glUniform1i(textureLocation, 0)

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//        glUniformMatrix4fv(modelMatrix, 1, false, mvpMatrix, 0)

        vertexBuffer.position(0)
        glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
        glEnableVertexAttribArray(positionLocation)

        vertexBuffer.position(2)
        glVertexAttribPointer(texCoordLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
        glEnableVertexAttribArray(texCoordLocation)

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun compileShaders() {
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)

        positionLocation = glGetAttribLocation(shaderProgram, "aPosition")
        texCoordLocation = glGetAttribLocation(shaderProgram, "aTexCoord")
        textureLocation = glGetUniformLocation(shaderProgram, "uTexture")
        timeLocation = glGetUniformLocation(shaderProgram, "uTime")
        modelMatrix = glGetUniformLocation(shaderProgram, "uMVPMatrix")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
        return shader
    }

    private fun loadTexture() {
        val textureIDs = IntArray(1)
        glGenTextures(1, textureIDs, 0)
        textureHandle = textureIDs[0]

        glBindTexture(GL_TEXTURE_2D, textureHandle)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val bitmap = BitmapFactory.decodeResource(context.resources, cloudTexture)
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

//    private fun compileShaders() {
//        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
//        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
//
//        shaderProgram = glCreateProgram()
//        glAttachShader(shaderProgram, vertexShader)
//        glAttachShader(shaderProgram, fragmentShader)
//        glLinkProgram(shaderProgram)
//    }

//    private fun loadShader(type: Int, shaderCode: String): Int {
//        val shader = glCreateShader(type)
//        glShaderSource(shader, shaderCode)
//        glCompileShader(shader)
//        return shader
//    }

//    private fun setupBuffers() {
//        val buffers = IntArray(1)
//        glGenBuffers(1, buffers, 0)
//        vbo = buffers[0]
//
//        glBindBuffer(GL_ARRAY_BUFFER, vbo)
//        glBufferData(GL_ARRAY_BUFFER, vertices.size * 4, verticesBuffer, GLES20.GL_STATIC_DRAW)
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//
//        glGenVertexArrays(1, buffers, 0)
//        vao = buffers[0]
//
//        glBindVertexArray(vao)
//        glBindBuffer(GL_ARRAY_BUFFER, vbo)
//        glEnableVertexAttribArray(0)
//        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//        glBindVertexArray(0)
//    }

//    companion object {
//        private val verticesBuffer = FloatBuffer.wrap(vertices) //BufferUtils.floatBuffer(vertices)
//    }
}
