package com.patrik.csikos.canyoufindit

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class YourImageAnalyzer(
    val onSuccess: (Text) -> Unit,
    val onFailure: (Exception) -> Unit
) : ImageAnalysis.Analyzer {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    Log.d("myapp", "analyze: $visionText")
                    onSuccess(visionText)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }
        imageProxy.close()
    }
}