package com.example.opengldemo2

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar

class MainActivity : AppCompatActivity() {

    private lateinit var customGLSurfaceView: CustomGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customGLSurfaceView = findViewById(R.id.custom_gl_surface_view)

        findViewById<SeekBar>(R.id.seekX).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pos = (progress.toFloat()*2/100f-1f).coerceIn(-1f,1f)
                CustomGLRenderer.posX = pos

                Log.d("KKKKK", "X: $pos")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        findViewById<SeekBar>(R.id.seekY).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pos = (progress.toFloat()*2/100f-1f).coerceIn(-1f,1f)
                CustomGLRenderer.posY = pos
                Log.d("KKKKK", "Y: $pos")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        findViewById<SeekBar>(R.id.seekZ).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pos = (-progress.toFloat()/10f - 3f).coerceIn(-9f,-3f)
                CustomGLRenderer.posZ = pos
                Log.d("KKKKK", "Z: $pos")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        findViewById<SeekBar>(R.id.fov).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pos = (progress + 10).coerceIn(10,100)
                CustomGLRenderer.fov = pos.toFloat()
                Log.d("KKKKK", "Focal Length: $pos")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }
}