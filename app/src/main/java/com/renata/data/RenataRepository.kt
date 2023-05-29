package com.renata.data

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.renata.data.retrofit.ApiConfig
import com.renata.data.retrofit.ApiService
import com.renata.data.user.forgotpass.ForgotPassResponse
import com.renata.data.user.login.LoginResponse
import com.renata.data.user.register.RegisterResponse
import com.renata.data.user.resetpass.ResetPassResponse
import com.renata.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class RenataRepository(private val application: Application) {

    private var imageSize: Int = 224
    private val apiService: ApiService = ApiConfig.getApiService()

    fun classifyImage(image: Bitmap): String {
        try {
            val model: Model = Model.newInstance(application.applicationContext)
            val inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, imageSize, imageSize, 3),
                DataType.FLOAT32
            )
            val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)
            val outputs: Model.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer
            val confidences: FloatArray = outputFeature0.floatArray
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf(
                "Unknown",
                "Aluvial",
                "Andosol",
                "Entisol",
                "Humus",
                "Inceptisol",
                "Kapur",
                "Laterit",
                "Pasir"
            )
            val detectedClass = classes[maxPos]
            model.close()
            return detectedClass
        } catch (e: IOException) {
            throw Exception("Failed to classify image")
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPass: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(
                email,
                password,
                confirmPass
            )
            if (response.success) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            if (response.success) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun userForgotPass(
        email: String,
    ): LiveData<Result<ForgotPassResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.forgotPass(
                email
            )
            if (response.success) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun userResetPass(
        email: String,
        password: String,
        confirmPass: String
    ): LiveData<Result<ResetPassResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.resetPass(
                email,
                password,
                confirmPass
            )
            if (response.success) {
                emit(Result.Error(response.message))
            } else {
                emit(Result.Success(response))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

}