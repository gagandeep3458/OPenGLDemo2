package com.example.opengldemo2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.widget.Toast
import com.example.opengldemo2.renderables.Cube
import com.example.opengldemo2.util.*
import java.nio.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.absoluteValue

class CustomGLRenderer(private val context: Context, private val onHit: (String) -> Unit) : GLSurfaceView.Renderer {

    private val cubeData = floatArrayOf(
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

    private lateinit var trianglesList: MutableList<Triangle>

    private var viewportWidth: Int = 0
    private var viewportheight: Int = 0
    private var buff = ByteBuffer.allocate(4)
    private var angle: Float = 0.0F
    private lateinit var mCube: Cube

    @Volatile
    var normHitX: Float = 0f
    @Volatile
    var normHitY: Float = 0f
    @Volatile
    var clicked: Boolean = false

    private val projectionMatrix = FloatArray(16)
    private var finalMatrix = FloatArray(16)
    private var inverseFinalMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val resourceIDArray = intArrayOf(R.drawable.apple,
        R.drawable.grapes,
        R.drawable.orange,
        R.drawable.pomegranate,
        R.drawable.pineapple,
        R.drawable.banana)

    private var texID = IntArray(resourceIDArray.size)

    var ratio: Float = 1.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        buff.order(ByteOrder.nativeOrder())
        texID = loadMultipleTexturesAndGetIDs(context, resourceIDArray)
        mCube = Cube(context)

        trianglesList = ArrayList()

        for (i in cubeData.indices step 9) {
            trianglesList.add(Triangle(Vector3d(cubeData[i],cubeData[i+1],cubeData[i+2]),
                    Vector3d(cubeData[i+3],cubeData[i+4],cubeData[i+5]),
                    Vector3d(cubeData[i+6],cubeData[i+7],cubeData[i+8])))
        }

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GL10.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        viewportWidth = width
        viewportheight = height

        GLES20.glViewport(0, 0, viewportWidth, viewportheight)
        ratio = width.toFloat() / height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 1.0f, 10.0f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, posX, posY, posZ)
        Matrix.rotateM(modelMatrix, 0, 36f, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.multiplyMM(finalMatrix, 0, projectionMatrix, 0, modelMatrix, 0)


        mCube.drawCube(finalMatrix,texID)

        if (clicked) {
            Matrix.setIdentityM(inverseFinalMatrix,0)
            Matrix.invertM(inverseFinalMatrix,0,finalMatrix,0)
            handleTouchPress()
            clicked = false
        }

        angle += 0.6f
    }

    private fun handleTouchPress() {

        val nearPoint = floatArrayOf(normHitX,normHitY,-1f,1f)
        val farPoint = floatArrayOf(normHitX,normHitY,1f,1f)

        val worldRayOriginPoint = getWorldPoint(nearPoint)
        val worldRayFarPoint = getWorldPoint(farPoint)

        val rayVector = getRayVector(worldRayOriginPoint,worldRayFarPoint)

        val hitArray = BooleanArray(trianglesList.size)

        checkIntersections(worldRayOriginPoint,rayVector,trianglesList,hitArray)
        Log.d(TAG, "handleTouchPress: ${getHitFace(hitArray)}")
        onHit.invoke(getHitFace(hitArray))
        
    }


    private fun getWorldPoint(normalizedPoint: FloatArray): Vector3d {
        val temp = FloatArray(4)
        Matrix.multiplyMV(temp,0,inverseFinalMatrix,0,normalizedPoint,0)

        temp[0] /= temp[3]
        temp[1] /= temp[3]
        temp[2] /= temp[3]

        return Vector3d(temp[0],temp[1],temp[2])
    }

    companion object {
        @Volatile
        var posX: Float = 0f

        @Volatile
        var posY: Float = 0f

        @Volatile
        var posZ: Float = -4f

        @Volatile
        var fov: Float = 60f

        private const val TAG = "CustomGLRenderer"
    }
}

data class Vector3d(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {

    fun dot(other: Vector3d): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    fun sub(vector1: Vector3d, vector2: Vector3d) {
        this.x = vector1.x - vector2.x
        this.y = vector1.y - vector2.y
        this.z = vector1.z - vector2.z
    }

    fun cross(vector1: Vector3d, vector2: Vector3d) {
        this.x = vector1.y * vector2.z- vector1.z * vector2.y
        this.y = vector1.z * vector2.x - vector1.x * vector2.z
        this.z = vector1.x * vector2.y - vector1.y * vector2.x
    }

    operator fun Vector3d.unaryMinus() = Vector3d(-this.x,-this.y,-this.z)

    fun normalize() {
        var max = 0f

        if (this.x.absoluteValue > max) {
            max = this.x.absoluteValue
        }
        if (this.y.absoluteValue > max) {
            max = this.y.absoluteValue
        }
        if (this.z.absoluteValue > max) {
            max = this.z.absoluteValue
        }

        this.x /= max
        this.y /= max
        this.z /= max
    }
}
data class Triangle(val a: Vector3d,val b: Vector3d,val c: Vector3d)
