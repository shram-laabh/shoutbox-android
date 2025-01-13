package com.shoutboxapp.shoutbox.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

private const val TAG = "ChatServerRepository"
class ChatServerRepository {
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var bufferedOutputStream: BufferedOutputStream? = null

    suspend fun connectToServer(host: String, port: Int): Boolean{
        return withContext(Dispatchers.IO){
            try{
                socket = Socket(host, port)
                socket?.tcpNoDelay = true
                Log.d(TAG, socket.toString())
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                bufferedOutputStream = BufferedOutputStream(outputStream)
                return@withContext true
            } catch(e: Exception){
                Log.d(TAG, "Failed to connect to server")
                return@withContext false
            }
        }
    }

    suspend fun sendMessage(message: String){
        withContext(Dispatchers.IO){
            if (socket == null)
                Log.d(TAG, "Socket is null")
            if (outputStream == null){
                Log.d(TAG, "outputStream is null")
            }
            /*outputStream?.write(message.toByteArray())
            outputStream?.flush()*/
            bufferedOutputStream?.write(message.toByteArray())
            bufferedOutputStream?.flush()
        }
    }

    suspend fun receiveMessage(): String? {
        return withContext(Dispatchers.IO){
            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.read(buffer)
            Log.d(TAG, "Recievedd message ${buffer.toString(Charsets.UTF_8)}")
            bytesRead?.let {
                String(buffer, 0, it)
            }
        }
    }

    fun closeConnection() {
        socket?.close()
        inputStream?.close()
        outputStream?.close()
    }

}