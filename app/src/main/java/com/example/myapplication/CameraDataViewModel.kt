package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vnpttech.ipcamera.CameraCallback
import com.vnpttech.ipcamera.Constants
import com.vnpttech.ipcamera.VNPTCamera
import kotlin.concurrent.thread

class CameraDataViewModel() : ViewModel() {

    var dataVideo: MutableLiveData<VideoDataReceiveFromCamera> = MutableLiveData()
    var dataAudio: MutableLiveData<AudioDataReceiveFromCamera> = MutableLiveData()

    val camera = VNPTCamera("VNTTA-017700-GBSJY", "nPksh5p2")
    val TAG: String = "NQT"
    var isConnect: Boolean = false


    fun changeQuantity(mode: Int){
        camera.setVideoQuality(mode,-1)
      //  print( " chế độ chọn lại chất lượng "+camera.setVideoQuality(mode,-1))
    }

    fun turnCameraOnOrOff(bool: Boolean) {

        if(bool==false){
            camera.setVideo(false)
            camera.setAudio(false)


        }
        else{
            camera.setVideo(true)
            camera.setAudio(true)
        }

    }

    fun sendAudio(buf: ByteArray, len: Int, codec: Int) {
        Log.i(TAG, "bắt đầu sentAudio")

        camera.startSendAudio()
        camera.pushAudio(buf, len, 1)

    }


    fun resetCameraPosition() {
        camera.resetPTZ("")
    }

    fun setCameraPosition(l: Int, r: Int, u: Int, d: Int) {
        camera.setRunPTZ(l, r, u, d)
    }

    fun initAndConnectCamera() {

        camera.init(object : CameraCallback {


            override fun onReceiveAudio(bytes: ByteArray, i: Int, i1: Int, i2: Int) {
                Log.i(TAG, "onReceiveAudio: i=$i, i1=$i1, i2=$i2, bytes size=${bytes.size}")

                var data = AudioDataReceiveFromCamera(bytes, i)
                thread(start = true) {
                    dataAudio.postValue(data)
                }
            }


            override fun onReceiveVideo(
                bytes: ByteArray,
                bytes1: ByteArray,
                bytes2: ByteArray,
                i: Int,
                i1: Int,
                i2: Int,
                i3: Int
            ) {
                Log.d(TAG, "onReceiveVideo:")
                Log.d(TAG, "  - bytes size: ${bytes.size}")
                Log.d(TAG, "  - bytes1 size: ${bytes1.size}")
                Log.d(TAG, "  - bytes2 size: ${bytes2.size}")
                Log.d(TAG, "  - i=$i, i1=$i1, i2=$i2, i3=$i3")

                // Tạo data object
                val data = VideoDataReceiveFromCamera(bytes, bytes1, bytes2, i, i1)


                // Đẩy lên LiveData từ background thread
                thread(start = true) {
                    dataVideo.postValue(data)
                }


            }

            override fun onConnect(i: Int, command: Constants.Command, status: Int) {
                //Log.d(TAG, "onConnect: i=$i, command=${command.name}, i1=$status")
                //0 is success,another is error
                //ết nối thành công mới cho phép hiển thị dữ liệu ko
                if (status == 0) {
                    isConnect = true
                    camera.setVideo(isConnect);
                    camera.setAudio(isConnect)


                } else {
                    isConnect = false
                }

            }

            override fun onConnectionLoss(i: Int, command: Constants.Command) {
                Log.d(TAG, "onConnectionLoss: i=$i, command=${command.name}")
            }

            override fun onCommandSet(command: Constants.Command, i: Int) {
                Log.d(TAG, "onCommandSet: command=${command.name}, i=$i")

            }

            override fun onCommandGet(command: Constants.Command, i: Int, i1: Int, s: String?) {

                Log.d(TAG, "onCommandGet: command=${command.name}, i=$i, i1=$i1, s=$s")
            }

            override fun onStatistic(
                i: Int,
                command: Constants.Command,
                i1: Int,
                l: Long,
                l1: Long,
                l2: Long,
                l3: Long
            ) {
                Log.d(
                    TAG,
                    "onStatistic: i=$i, command=${command.name}, i1=$i1, l=$l, l1=$l1, l2=$l2, l3=$l3"
                )
            }
        })

        camera.connect(285350, Char(0x7A))

    }


}