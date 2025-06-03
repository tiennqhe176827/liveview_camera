package com.example.myapplication

import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
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
    private lateinit var cameraViewModel: CameraDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mglSurfaceView = findViewById(R.id.surface_view)



        cameraViewModel = CameraDataViewModel()
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
    }


    }

