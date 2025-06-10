package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.vnpttech.opengl.MGLSurfaceView
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val TAG: String = "NQT"
    private lateinit var mglSurfaceView: MGLSurfaceView
    private lateinit var buttonReset: Button
    private lateinit var buttonSendAudio: Button
    private lateinit var buttonTurnLeft: Button
    private lateinit var buttonTurnRight: Button
    private lateinit var buttonTurnUp: Button
    private lateinit var buttonTurnDown: Button
    private lateinit var cameraViewModel: CameraDataViewModel
    private lateinit var audioTrack: AudioTrack
    private lateinit var cameraOffImage: ImageView
    private lateinit var buttonCameraOnOff: Button

    private val audioPermission = Manifest.permission.RECORD_AUDIO
    private val audioRequestCode = 1001

    // Audio recording vars
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord


    private fun checkAndRequestAudioPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                audioPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(audioPermission), audioRequestCode)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == audioRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Audio permission granted")
        } else {
            Log.e(TAG, "Audio permission denied")
        }
    }

    private fun startAudioCaptureAndSend() {
        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val audioBuffer = ByteArray(bufferSize)

        audioRecord.startRecording()
        isRecording = true

        thread(start = true) {
            while (isRecording) {
                val readLen = audioRecord.read(audioBuffer, 0, bufferSize)
                if (readLen > 0) {

                    cameraViewModel.sendAudio(audioBuffer, readLen, 1)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ////
        val items = arrayOf<String?>("1920x1880", "1280x720", "640x480")

        val spinner: Spinner = findViewById(R.id.spinner_videoQuantity)

        val arrayAdapter= ArrayAdapter(this,android.R.layout.simple_spinner_item,items)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = arrayAdapter
        ////

        cameraViewModel = CameraDataViewModel()

        buttonReset = findViewById(R.id.btn_reset)
        buttonTurnUp = findViewById(R.id.btn_up)
        buttonTurnDown = findViewById(R.id.btn_down)
        buttonTurnLeft = findViewById(R.id.btn_left)
        buttonTurnRight = findViewById(R.id.btn_right)
        buttonSendAudio = findViewById(R.id.btn_sendAudioToCamera)
        buttonCameraOnOff = findViewById(R.id.btn_turnOnOrOff)
        mglSurfaceView = findViewById(R.id.surface_view)

        cameraOffImage = findViewById(R.id.camera_off_image)

        val sampleRateInHz = 8000
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

        cameraViewModel.initAndConnectCamera()

        // Observe video data and render
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

        // Observe audio data from camera and play
        cameraViewModel.dataAudio.observe(this, Observer { audioData ->
            audioTrack.write(audioData.buf, 0, audioData.length)
        })

        buttonReset.setOnClickListener {
            cameraViewModel.resetCameraPosition()
        }

        buttonTurnLeft.setOnClickListener {
            cameraViewModel.setCameraPosition(1, -1, -1, -1)
        }
        buttonTurnRight.setOnClickListener {
            cameraViewModel.setCameraPosition(-1, 1, -1, -1)
        }
        buttonTurnUp.setOnClickListener {
            cameraViewModel.setCameraPosition(-1, -1, 1, -1)

        }
        buttonTurnDown.setOnClickListener {
            cameraViewModel.setCameraPosition(-1, -1, -1, 1)
        }

        buttonSendAudio.setOnClickListener {
            if (!isRecording) {
                if (checkAndRequestAudioPermission()) {
                    startAudioCaptureAndSend()
                    buttonSendAudio.text = "Stop Audio"
                }
            } else {
                //  stopAudioCapture()

                isRecording = false
                audioRecord.stop()
                audioRecord.release()

                buttonSendAudio.text = "Send Audio To Camera"
            }
        }

        var isCameraOn: Boolean = true
        buttonCameraOnOff.text = "tạm dừng camera"




        buttonCameraOnOff.setOnClickListener {

            isCameraOn = !isCameraOn
            if (isCameraOn == true) {
                buttonCameraOnOff.setText("tạm dừng camera")
                mglSurfaceView.visibility = View.VISIBLE
                cameraOffImage.visibility = View.GONE
            } else {
                buttonCameraOnOff.setText("tiếp tục camera")
                mglSurfaceView.visibility = View.GONE
                cameraOffImage.visibility = View.VISIBLE
            }
            cameraViewModel.turnCameraOnOrOff(isCameraOn)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selection=parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity,selection, Toast.LENGTH_LONG).show()
                var mode:Int=0;
                if(selection.equals("1920x1880")){
                    mode=0
                }
                if(selection.equals("1280x720")){
                    mode=1
                }
                if(selection.equals("640x480")){
                    mode=2
                }

                cameraViewModel.changeQuantity(mode)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }



    }


}