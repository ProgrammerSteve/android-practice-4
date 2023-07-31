package com.programmersteve.tensorliteinterpreterexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    // Declare assetManager as a member variable
    private lateinit var assetManager: AssetManager

    private lateinit var interpreter: Interpreter
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var outputBuffer: ByteBuffer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the AssetManager instance
        assetManager = assets

        // Initialize the TensorFlow Lite interpreter
        interpreter = Interpreter(loadModelFile())
    }

    private val inputShape: IntArray = intArrayOf(1, 300, 300, 3)
    private val outputShape: IntArray = intArrayOf(1, 5, 8736, 4)

    init {
        val tfliteModel = loadModelFile()
        interpreter = Interpreter(tfliteModel)
        inputBuffer = ByteBuffer.allocateDirect(inputShape[1] * inputShape[2] * inputShape[3] * 4)
        outputBuffer = ByteBuffer.allocateDirect(outputShape[1] * outputShape[2] * outputShape[3] * 4)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd("tileDetection.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }



}