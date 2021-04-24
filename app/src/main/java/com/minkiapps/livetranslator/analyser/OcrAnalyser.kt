package com.minkiapps.livetranslator.analyser

import android.annotation.SuppressLint
import android.graphics.Rect
import android.media.Image
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.*
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.mlsdk.common.MLFrame
import com.minkiapps.livetranslator.overlay.ScannerOverlay
import com.minkiapps.livetranslator.translation.Translation
import com.minkiapps.livetranslator.translation.TranslationFactory
import com.minkiapps.livetranslator.translation.translationList
import com.minkiapps.livetranslator.utils.BitmapUtil
import com.minkiapps.livetranslator.utils.FrameMetadata
import com.minkiapps.livetranslator.utils.YuvNV21Util
import timber.log.Timber

class OcrAnalyser(private val scannerOverlay: ScannerOverlay) : ImageAnalysis.Analyzer,
    LifecycleObserver {

    private val mutableLiveData = MutableLiveData<TranslationText>()
    private val errorData = MutableLiveData<Exception>()

    fun liveData() : LiveData<TranslationText> = mutableLiveData
    fun errorLiveData() : LiveData<Exception> = errorData

    @Volatile
    var freeze : Boolean = false

    @Volatile
    var translation : Translation = translationList.getOrElse(0) { Translation("en","zh") }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if(freeze) {
            imageProxy.close()
            return
        }

        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val scannerRect = getScannerRectToPreviewViewRelation(
                Size(
                    imageProxy.width,
                    imageProxy.height
                ), rotation
            )

            val image = imageProxy.image!!
            val cropRect = image.getCropRectAccordingToRotation(scannerRect, rotation)
            image.cropRect = cropRect

            val byteArray = YuvNV21Util.yuv420toNV21(image)
            val bitmap = BitmapUtil.getBitmap(
                byteArray, FrameMetadata(
                    cropRect.width(),
                    cropRect.height(),
                    rotation
                )
            )

            val recogniser = TranslationFactory.getRecogniser(translation)

            val mlText = Tasks.await(recogniser.asyncAnalyseFrame(MLFrame.fromBitmap(bitmap)))
            Timber.d("Recognised Text: ${mlText.stringValue}")
            val toTranslate = mlText.stringValue.replace("\n"," ")

            if(toTranslate.isNotBlank() && !freeze) {
                val translator = TranslationFactory.getTranslator(translation)
                val translated = Tasks.await(translator.asyncTranslate(toTranslate))
                Timber.d("Translated Text: $translated")
                mutableLiveData.postValue(TranslationText(toTranslate, translated))
            }
        } catch (e: Exception) {
            errorData.postValue(e)
        } finally {
            imageProxy.close()
        }
    }

    private fun getScannerRectToPreviewViewRelation(proxySize: Size, rotation: Int): ScannerRectToPreviewViewRelation {
        return when(rotation) {
            0, 180 -> {
                val size = scannerOverlay.size
                val width = size.width
                val height = size.height
                val previewHeight = width / (proxySize.width.toFloat() / proxySize.height)
                val heightDeltaTop = (previewHeight - height) / 2

                val scannerRect = scannerOverlay.scanRect
                val rectStartX = scannerRect.left
                val rectStartY = heightDeltaTop + scannerRect.top

                ScannerRectToPreviewViewRelation(
                    rectStartX / width,
                    rectStartY / previewHeight,
                    scannerRect.width() / width,
                    scannerRect.height() / previewHeight
                )
            }
            90, 270 -> {
                val size = scannerOverlay.size
                val width = size.width
                val height = size.height
                val previewWidth = height / (proxySize.width.toFloat() / proxySize.height)
                val widthDeltaLeft = (previewWidth - width) / 2

                val scannerRect = scannerOverlay.scanRect
                val rectStartX = widthDeltaLeft + scannerRect.left
                val rectStartY = scannerRect.top

                ScannerRectToPreviewViewRelation(
                    rectStartX / previewWidth,
                    rectStartY / height,
                    scannerRect.width() / previewWidth,
                    scannerRect.height() / height
                )
            }
            else -> throw IllegalArgumentException("Rotation degree ($rotation) not supported!")
        }
    }

    private data class ScannerRectToPreviewViewRelation(
        val relativePosX: Float,
        val relativePosY: Float,
        val relativeWidth: Float,
        val relativeHeight: Float
    )

    private fun Image.getCropRectAccordingToRotation(
        scannerRect: ScannerRectToPreviewViewRelation,
        rotation: Int
    ) : Rect {
        return when(rotation) {
            0 -> {
                val startX = (scannerRect.relativePosX * this.width).toInt()
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val startY = (scannerRect.relativePosY * this.height).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            90 -> {
                val startX = (scannerRect.relativePosY * this.width).toInt()
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                val startY = height - (scannerRect.relativePosX * this.height).toInt() - numberPixelH
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            180 -> {
                val numberPixelW = (scannerRect.relativeWidth * this.width).toInt()
                val startX =
                    (this.width - scannerRect.relativePosX * this.width - numberPixelW).toInt()
                val numberPixelH = (scannerRect.relativeHeight * this.height).toInt()
                val startY =
                    (height - scannerRect.relativePosY * this.height - numberPixelH).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            270 -> {
                val numberPixelW = (scannerRect.relativeHeight * this.width).toInt()
                val numberPixelH = (scannerRect.relativeWidth * this.height).toInt()
                val startX =
                    (this.width - scannerRect.relativePosY * this.width - numberPixelW).toInt()
                val startY = (scannerRect.relativePosX * this.height).toInt()
                Rect(startX, startY, startX + numberPixelW, startY + numberPixelH)
            }
            else -> throw IllegalArgumentException("Rotation degree ($rotation) not supported!")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun close() {
        TranslationFactory.close()
    }
}