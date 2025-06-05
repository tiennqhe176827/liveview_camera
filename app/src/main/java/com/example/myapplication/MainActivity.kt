package com.example.myapplication

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.vnpttech.ipcamera.CameraCallback
import com.vnpttech.ipcamera.Constants
import com.vnpttech.ipcamera.SDKCallback
import com.vnpttech.ipcamera.SdkInterface
import com.vnpttech.ipcamera.VNPTCamera
import com.vnpttech.opengl.MGLSurfaceView

class MainActivity : AppCompatActivity() {

    private val TAG: String = "gia tri debug"
    private lateinit var mglSurfaceView: MGLSurfaceView

    private lateinit var buttonReset: Button
    private lateinit var buttonTurnLeft: Button
    private lateinit var buttonTurnRight: Button
    private lateinit var buttonTurnUp: Button
    private lateinit var buttonTurnDown: Button


    private lateinit var cameraViewModel: CameraDataViewModel
    private lateinit var audioTrack: AudioTrack


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraViewModel = CameraDataViewModel()


        // Initialize AudioTrack
        val sampleRateInHz = 8000 // Change this if your camera provides different audio sample rate
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

        audioTrack = AudioTrack(

            AudioManager.STREAM_MUSIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack.play()


        mglSurfaceView = findViewById(R.id.surface_view)




        cameraViewModel.initAndConnectCamera()



        cameraViewModel.dataVideo.observe(this, Observer { videoData ->
            mglSurfaceView.setYUVData(
                videoData.widthData,
                videoData.heightData,
                videoData.byteArray_Data_fy,
                videoData.byteArray_Data_fu,
                videoData.byteArray_Data_fv
            )




            mglSurfaceView.requestRender()
        })

        cameraViewModel.dataAudio.observe(this, Observer { audioData ->
            audioTrack.write(
                audioData.buf, 0, audioData.length
            )


        })

        buttonReset = findViewById(R.id.btn_reset)
        buttonTurnUp = findViewById(R.id.btn_up)
        buttonTurnDown = findViewById(R.id.btn_down)
        buttonTurnLeft = findViewById(R.id.btn_left)
        buttonTurnRight = findViewById(R.id.btn_right)

        buttonReset.setOnClickListener {
            cameraViewModel.resetCameraPosition()

        }

        buttonTurnLeft.setOnClickListener {
            cameraViewModel.setCameraPosition(1,-1,-1,-1)

        }
        buttonTurnRight.setOnClickListener {
            cameraViewModel.setCameraPosition(-1,1,-1,-1)

        }
        buttonTurnUp.setOnClickListener {

            cameraViewModel.setCameraPosition(-1,-1,1,-1)

        }
        buttonTurnDown.setOnClickListener {
            cameraViewModel.setCameraPosition(-1,-1,-1,1)

        }

    }


}

