package com.example.escaneadorpip.workers

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import android.util.Log

class BackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsFolder?.listFiles()

        files?.forEach { file ->
            if (file.isFile){
                uploadFileToServer(file)
            }
        }
        return Result.success()
    }

    private fun uploadFileToServer(file: File){
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name,file.asRequestBody())
            .build()

        val request = Request.Builder()
            .url("http://192.168.2.103:2000/")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Upload", "Error al subir archivo: $ {e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    Log.d("Upload", "Archivo subido con exito: $ {file.name}")
                }else{
                    Log.e("Upload", "Error en la respuesta: $ {response.message}")
                }
            }
        })
    }
}