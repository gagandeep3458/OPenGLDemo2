package com.example.opengldemo2.util

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.example.opengldemo2.Triangle
import com.example.opengldemo2.Vector3d
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*
import kotlin.collections.ArrayList


fun readTextFileFromAssets(context: Context, assetName: String): String {
    val inputStream: InputStream = context.assets.open("shaders/$assetName")
    val inputStreamReader = InputStreamReader(
            inputStream)
    val bufferedReader = BufferedReader(
            inputStreamReader)
    var nextLine: String?
    val body = StringBuilder()
    try {
        while (bufferedReader.readLine().also { nextLine = it } != null) {
            body.append(nextLine)
            body.append('\n')
        }
    } catch (e: IOException) {
        return ""
    }
    return body.toString()
}

fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}

fun loadTexObjAndGetObjID(context: Context, resourceID: Int): Int {

    val textureObjectIds = IntArray(1)
    GLES20.glGenTextures(1, textureObjectIds, 0)
    if (textureObjectIds[0] == 0) {
        Log.d("TTTTT", "Could not generate a new OpenGL texture object.")
        return 0
    }

    val options = BitmapFactory.Options()
    options.inScaled = false
    val bitmap = BitmapFactory.decodeResource(context.resources, resourceID, options)
    if (bitmap == null) {
        Log.d("TTTTT", "Resource ID $resourceID could not be decoded.")
        GLES20.glDeleteTextures(1, textureObjectIds, 0)
        return 0
    }

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0])

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    bitmap.recycle()
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    Log.d("TTTTT", "loadTexObjAndGetObjID: ${textureObjectIds[0]}")

    return textureObjectIds[0]
}

fun loadMultipleTexturesAndGetIDs(context: Context, resourceIDArray: IntArray): IntArray {

    val temp = IntArray(resourceIDArray.size)

    for (index in resourceIDArray.indices) {
        temp[index] = loadTexObjAndGetObjID(context,resourceIDArray[index])
    }
    return temp
}

fun getVerticesBuffer(context: Context, assetName: String): FloatBuffer {

    val verticesList: MutableList<String> = ArrayList()

    val scanner = Scanner(context.assets.open("models/$assetName"))
    while (scanner.hasNextLine()) {
        val line: String = scanner.nextLine()
        if (line.startsWith("v ")) {
            verticesList.add(line)
        }
    }
    scanner.close()

    return ByteBuffer.allocateDirect(verticesList.size * 3 * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            for (vertex in verticesList) {
                val coords = vertex.split(" ".toRegex()).toTypedArray()
                val x = coords[1].toFloat()
                val y = coords[2].toFloat()
                val z = coords[3].toFloat()
                this.put(x)
                this.put(y)
                this.put(z)
            }
            this.position(0)
        }
    }
}

fun getFacesBuffer(context: Context, assetName: String): ShortBuffer {

    val facesList: MutableList<String> = ArrayList()

    val scanner = Scanner(context.assets.open("models/$assetName"))
    while (scanner.hasNextLine()) {
        val line: String = scanner.nextLine()
        if (line.startsWith("f ")) {
            facesList.add(line)
        }
    }
    scanner.close()

    return ByteBuffer.allocateDirect(facesList.size * 3 * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply {
            Log.d("GET_DRAW_ORDER", "->")
            for (face in facesList) {
                val data = face.split(" ".toRegex()).toTypedArray()
                val x = data[1].split("/".toRegex()).toTypedArray()[0].toShort()
                val y = data[2].split("/".toRegex()).toTypedArray()[0].toShort()
                val z = data[3].split("/".toRegex()).toTypedArray()[0].toShort()
                // Subtracting one because vertex array indices starts from 0
                this.put((x - 1).toShort())
                this.put((y - 1).toShort())
                this.put((z - 1).toShort())
                Log.d("GET_DRAW_ORDER", "$x $y $z")
            }
            this.position(0)
        }
    }
}

fun getTextureCoordinatesBuffer(context: Context, assetName: String): FloatBuffer {

    val texelList: MutableList<String> = ArrayList()

    val scanner = Scanner(context.assets.open("models/$assetName"))
    while (scanner.hasNextLine()) {
        val line: String = scanner.nextLine()
        if (line.startsWith("vt ")) {
            texelList.add(line)
        }
    }
    scanner.close()

    return ByteBuffer.allocateDirect(texelList.size * 2 * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            for (face in texelList) {
                val data = face.split(" ".toRegex()).toTypedArray()
                val x = data[1].toFloat()
                val y = data[2].toFloat()
                this.put(x)
                this.put(y)
            }
            this.position(0)
        }
    }
}

fun getRayVector(worldRayOriginPoint: Vector3d, worldRayFarPoint: Vector3d): Vector3d {
    return Vector3d(worldRayFarPoint.x - worldRayOriginPoint.x,
            worldRayFarPoint.y - worldRayOriginPoint.y,
            worldRayFarPoint.z - worldRayOriginPoint.z)
}

fun checkIntersections(rayOrigin: Vector3d, rayVector: Vector3d, trianglesList: MutableList<Triangle>, hitArray: BooleanArray) {

//    Log.d("CustomGLRenderer", "Ray_Origin-> ${rayOrigin}: ")
//    Log.d("CustomGLRenderer", "Ray-> ${rayVector}: ")
    for (triangle in trianglesList.indices) {
        hitArray[triangle] = doesIntersect(rayOrigin, rayVector, trianglesList[triangle])
    }
}

fun doesIntersect(rayOrigin: Vector3d, rayVector: Vector3d, triangle: Triangle): Boolean {


    val EPSILON = 0.0000001f

    val vertex0: Vector3d = triangle.a
    val vertex1: Vector3d = triangle.b
    val vertex2: Vector3d = triangle.c
    val edge1 = Vector3d()
    val edge2 = Vector3d()
    val h = Vector3d()
    val s = Vector3d()
    val q = Vector3d()
    val u: Float
    val v: Float
    edge1.sub(vertex1, vertex0)
    edge2.sub(vertex2, vertex0)

    h.cross(rayVector, edge2)
    val a: Float = edge1.dot(h)
    if (a > -EPSILON && a < EPSILON) {
        return false // This ray is parallel to this triangle.
    }
    val f: Float = 1.0f / a
    s.sub(rayOrigin, vertex0)
    u = f * s.dot(h)
    if (u < 0.0 || u > 1.0) {
        return false
    }
    q.cross(s, edge1)
    v = f * rayVector.dot(q)
    if (v < 0.0 || u + v > 1.0) {
        return false
    }
    // At this stage we can compute t to find out where the intersection point is on the line.
    // At this stage we can compute t to find out where the intersection point is on the line.
    val t: Float = f * edge2.dot(q)
    if (t > EPSILON) {

        val triNormal = getNormal(triangle.a,triangle.b,triangle.c)
//        Log.d("CustomGLRenderer", "Normal-> ${triNormal}: ")
//        Log.d("CustomGLRenderer", "DOT PRODUCT: ${rayVector.dot(triNormal)}")

        if (rayVector.dot(triNormal) < 0) {
        return true}
    }
    return false
}

fun getHitFace(hitArray: BooleanArray): String {

    var temp = ""

    when(true) {
        hitArray[0] or hitArray[1] -> { temp = temp.plus("APPLE HIT ") }
        hitArray[2] or hitArray[3] -> { temp = temp.plus("GRAPES HIT ") }
        hitArray[4] or hitArray[5] -> { temp = temp.plus("ORANGE HIT ") }
        hitArray[6] or hitArray[7] -> { temp = temp.plus("POMEGRANATE HIT ") }
        hitArray[8] or hitArray[9] -> { temp = temp.plus("PINEAPPLE HIT ") }
        hitArray[10] or hitArray[11] -> { temp = temp.plus("BANANA HIT ") }
        true -> temp
        false -> temp
    }
    return temp
}

fun computeNormals(trianglesArray: FloatArray): MutableList<Vector3d> {

    val normalsList: MutableList<Vector3d> = ArrayList()

    for (index in trianglesArray.indices step 9) {
        normalsList.add(getNormal(Vector3d(trianglesArray[index], trianglesArray[index + 1], trianglesArray[index + 2]),
            Vector3d(trianglesArray[index + 3], trianglesArray[index + 4], trianglesArray[index + 5]),
            Vector3d(trianglesArray[index + 6], trianglesArray[index + 7], trianglesArray[index + 8])))
    }

    return normalsList
}

fun getNormal(vertex0: Vector3d, vertex1: Vector3d, vertex2: Vector3d): Vector3d {

    val normal = Vector3d()

    val edge1 = Vector3d()
    val edge2 = Vector3d()

    edge1.sub(vertex1,vertex0)
    edge2.sub(vertex2,vertex0)

    normal.cross(edge1,edge2)
    return normal
}
