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
import com.vnpttech.ipcamera.CameraCallback
import com.vnpttech.ipcamera.Constants
import com.vnpttech.ipcamera.SDKCallback
import com.vnpttech.ipcamera.SdkInterface
import com.vnpttech.ipcamera.VNPTCamera

class MainActivity : AppCompatActivity() {

    private val TAG: String = "gia tri debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var cameraViewModel: CameraDataViewModel = CameraDataViewModel(application)

        cameraViewModel.initAndConnectCamera()


    }
}
