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
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Canvas
import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "TFLite - ODT"
        const val REQUEST_IMAGE_CAPTURE: Int = 1
        private const val MAX_FONT_SIZE = 96F
    }
    val paint= Paint()
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var bitmap: Bitmap
    private lateinit var assetManager: AssetManager
    private lateinit var interpreter: Interpreter
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var outputBuffer: ByteBuffer
    private val inputShape: IntArray = intArrayOf(1, 300, 300, 3)
    //    private val outputShape: IntArray = intArrayOf(1, 5, 8736, 4)
    private val outputShape: IntArray = intArrayOf(1, 5, 8736, 4)

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd("tileDetection.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent= Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        imageView=findViewById(R.id.imageV)
        button=findViewById(R.id.btn)
        button.setOnClickListener{
            startActivityForResult(intent,101)
        }
        assetManager = assets
        // Initialize the TensorFlow Lite interpreter
        interpreter = Interpreter(loadModelFile())
        inputBuffer = ByteBuffer.allocateDirect(inputShape[1] * inputShape[2] * inputShape[3] * 4)
        outputBuffer = ByteBuffer.allocateDirect(outputShape[1] * outputShape[2] * outputShape[3] * 4)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==101){
            var uri=data?.data
            bitmap= MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
            get_predictions(bitmap)
        }
    }


    private fun resizeBitmapWithPadding(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        paddingColor: Int
    ): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val scale = targetWidth.toFloat() / originalWidth.coerceAtLeast(originalHeight)
        val scaledWidth = (scale * originalWidth).toInt()
        val scaledHeight = (scale * originalHeight).toInt()

        val leftOffset = (targetWidth - scaledWidth) / 2
        val topOffset = (targetHeight - scaledHeight) / 2

        val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, bitmap.config)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(paddingColor)
        canvas.drawBitmap(bitmap, leftOffset.toFloat(), topOffset.toFloat(), null)

        return resultBitmap
    }


    fun get_predictions(bitmap:Bitmap){
        // Step 1: create TFLite's TensorImage object
        val resizedBitmap = resizeBitmapWithPadding(bitmap, 300, 300, Color.BLACK)
        val image = TensorImage.fromBitmap(resizedBitmap)
        interpreter.run(image.buffer, outputBuffer)
        val numDetections = outputBuffer.getInt(0)
        val detectionResults = mutableListOf<Detection>()
        Log.d(TAG, "detectionResults $detectionResults")



//        val results = detector.detect(image)
//        val locations=results.map {obj->
//            obj.boundingBox
//        }
//        val labels=results.map{obj->
//            obj.categories
//        }
//        Log.d(TAG, "locations $locations")
//        Log.d(TAG, "labels $labels")

        val mutable=bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas=Canvas(mutable)
        val h=mutable.height

        paint.textSize=h/15f
        paint.strokeWidth=h/85f

//        results.forEachIndexed{index,obj->
//            val location=obj.boundingBox
//            paint.style=Paint.Style.STROKE
//            canvas.drawRect(location,paint)
//            for (category in obj.categories){
//                val score=category.score
//                //I want the category name here
//                val name=category.label
//                Log.d(TAG, "Name $name")
//                Log.d(TAG, "score $score")
//                paint.style=Paint.Style.FILL
//                canvas.drawText("$name $score",location.left,location.top,paint)
//            }
//        }

        imageView.setImageBitmap(mutable)
        // Step 4: Parse the detection result and show it
//        debugPrint(results)
    }








}



















/*
override fun onDestroy() {
super.onDestroy()
model.close()
}
*/


//    private fun debugPrint(results : List<Detection>) {
//        for ((i, obj) in results.withIndex()) {
//            val box = obj.boundingBox
//
//            Log.d(TAG, "Detected object: ${i} ")
//            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")
//
//            for ((j, category) in obj.categories.withIndex()) {
//                Log.d(TAG, "    Label $j: ${category.label}")
//                val confidence: Int = category.score.times(100).toInt()
//                Log.d(TAG, "    Confidence: ${confidence}%")
//            }
//        }
//    }