package com.example.opengldemo2.renderables

import android.content.Context
import android.opengl.GLES10.glColor4f
import android.opengl.GLES20
import android.util.Log
import com.example.opengldemo2.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10

class Cube(context: Context) {

    private val assetName = "Cube.obj"

    private val colors = arrayOf(
        floatArrayOf(1.0f, 0.5f, 0.0f, 1.0f),
        floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f),
        floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f),
        floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f),
        floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f),
        floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
    )

    private val cubeArray = floatArrayOf(
        //Front
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,

        // Right face
        1.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,

        // Back face
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,

        // Left face
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,

        // Top face
        -1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,

        // Bottom face
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
    )

    private var verticesBuffer = ByteBuffer.allocateDirect(cubeArray.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(cubeArray)
            position(0)
        }
    }

    private val texCoordArray = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,

        // Right face
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,

        // Back face
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,

        // Left face
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,

        // Top face
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,

        // Bottom face
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    private var texCoordinatesBuffer = ByteBuffer.allocateDirect(texCoordArray.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(texCoordArray)
            position(0)
        }
    }

    private var mProgram: Int = 0
    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var colorHandle: Int = 0
    private var textureCoordinateHandle: Int = 0
    private var textureUniformHandel: Int = 0

    init {

        val vertexShader: Int = loadShader(
            GLES20.GL_VERTEX_SHADER, readTextFileFromAssets(
                context,
                "cube_texture_vertex_shader.glsl"
            )
        )
        val fragmentShader: Int = loadShader(
            GLES20.GL_FRAGMENT_SHADER, readTextFileFromAssets(
                context,
                "cube_texture_fragment_shader.glsl"
            )
        )

        Log.d("TTTTT", ": $vertexShader $fragmentShader")

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

    }

    fun drawCube(mvpMatrix: FloatArray, texHandleArray: IntArray) {

        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "v_Position").also { posHandle ->
            GLES20.glEnableVertexAttribArray(posHandle)

            GLES20.glVertexAttribPointer(
                positionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                12, verticesBuffer
            )
        }

        mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also { mvpHandle ->
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        }

        textureUniformHandel =
            GLES20.glGetUniformLocation(mProgram, "u_TextureUnit").also { tuHandle ->

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandleArray[0])

                GLES20.glUniform1i(tuHandle, 0)
            }

        textureCoordinateHandle =
            GLES20.glGetAttribLocation(mProgram, "a_TextureCoordinates").also { tcHandle ->
                GLES20.glEnableVertexAttribArray(tcHandle)

                GLES20.glVertexAttribPointer(
                    tcHandle,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    8, texCoordinatesBuffer
                )

            }

        var k = 0

        for (i in 0 until 36 step 6) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandleArray[k++])
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, i, 6)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}