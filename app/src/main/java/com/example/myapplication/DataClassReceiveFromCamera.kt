package com.example.myapplication

import androidx.lifecycle.MutableLiveData

data class VideoDataReceiveFromCamera(
    var byteArray_Data_fy: ByteArray,
    var byteArray_Data_fu: ByteArray,
    var byteArray_Data_fv: ByteArray,
    var widthData:Int,
    var heightData:Int

)