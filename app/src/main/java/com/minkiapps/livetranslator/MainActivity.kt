package com.minkiapps.livetranslator

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.huawei.hms.mlsdk.tts.MLTtsError
import com.minkiapps.livetranslator.analyser.OcrAnalyser
import com.minkiapps.livetranslator.tooltip.ScanRectTooltip
import com.minkiapps.livetranslator.utils.hasInternetConnection
import com.minkiapps.livetranslator.utils.isHUAWEIManufacturer
import com.minkiapps.livetranslator.utils.isHmsAvailable
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var torchOn : Boolean = false
    private val analyserExecutor = Executors.newSingleThreadExecutor()

    private val toolTip by lazy { ScanRectTooltip(this) }
    private val analyser : OcrAnalyser by lazy {
        OcrAnalyser(olActMain)
    }

    private val mainViewModel : MainViewModel by viewModels()
    private val ttsWrapper = TTSWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.BaseTheme_LiveChineseEnglishTranslator)
        setContentView(R.layout.activity_main)

        tvActMainBuildInfo.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        if(!isHmsAvailable()) {
            Toast.makeText(this, R.string.hms_not_available, Toast.LENGTH_SHORT).show()
            return
        }

        olActMain.isEnabled = false

        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setUpLanguageModels()
            } else {
                finish()
            }
        }

        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun setUpLanguageModels() {
        mainViewModel.modelReadyLiveData().observe(this) {
            finalSetUp()
        }

        mainViewModel.downLoadProgressLiveData().observe(this) { p ->
            val progress = (p.alreadyDownLength * 1.0 / p.totalLength * 100).roundToInt()
            when(p.modelName) {
                MainViewModel.ModelName.zh -> pBActMainDownloadChineseModel.progress = progress
                MainViewModel.ModelName.en -> pBActMainDownloadEnglishModel.progress = progress
            }
        }
    }

    private fun finalSetUp() {
        clActMainDownloadContainer.isVisible = false
        llActMainTranslatePreview.setOnClickListener {
            swActMainTextFreeze.toggle()
        }

        swActMainTextFreeze.setOnCheckedChangeListener { _, isChecked ->
            analyser.freeze = isChecked
            ivMainTTS.isVisible = isHUAWEIManufacturer() && analyser.freeze
            tvActMainFreezeText.setHint(if (analyser.freeze) R.string.unfreeze_text_tap else R.string.freeze_text_tap)
            swActMainTextFreeze.setText(if (analyser.freeze) R.string.frozen_text else R.string.freeze_text)
        }

        ttsWrapper.setTtsListener(object : TTSWrapper.TTSListener {
            override fun onSpeechEnded() {
                pbMainTTSProgress.isVisible = false
                swActMainTextFreeze.isEnabled = true
                ivMainTTS.isEnabled = true
                ivMainTTS.setImageResource(R.drawable.ic_baseline_volume_up_24)
            }

            override fun onError(err: MLTtsError) {
                Timber.e(err.errorMsg)
            }
        })

        analyser.liveData().observe(this) { t ->
            tvActMainPreviewChinese.text = t.original
            tvActMainPreviewEnglish.text = t.translated

            ivMainTTS.setOnClickListener { view ->
                if(view.tag == null) {
                    if(!hasInternetConnection()) {
                        Toast.makeText(this, R.string.tts_no_internet, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    pbMainTTSProgress.isVisible = true
                    swActMainTextFreeze.isEnabled = false
                    ivMainTTS.setImageResource(R.drawable.ic_baseline_stop_24)

                    when(analyser.translationType) {
                        OcrAnalyser.Translation.CN_EN -> ttsWrapper.speakChinese(t.original)
                        OcrAnalyser.Translation.EN_CN -> ttsWrapper.speakEnglish(t.original)
                    }
                    view.tag = "IS_SPEECHING"
                } else {
                    ttsWrapper.stop()
                    ivMainTTS.setImageResource(R.drawable.ic_baseline_volume_up_24)
                    pbMainTTSProgress.isVisible = false
                    swActMainTextFreeze.isEnabled = true
                    view.tag = null
                }
            }
        }

        //ivActMainToggleTranslation.setRotateView180OnClick { //TODO https://github.com/HMS-Core/hms-ml-demo/issues/27
        //    analyser.translationType = when(analyser.translationType) {
        //        OcrAnalyser.Translation.CN_EN -> OcrAnalyser.Translation.EN_CN
        //        OcrAnalyser.Translation.EN_CN -> OcrAnalyser.Translation.CN_EN
        //    }
        //}

        startCamera()
        showToolTip()
    }

    private fun showToolTip() {
        if(!toolTip.shouldShowTooltip()) {
            olActMain.isEnabled = true
            return
        }

        val tooltip = Tooltip.Builder(this)
            .anchor(vActMainToolTipAnchor, 0, 0)
            .text(getString(R.string.tooltip_show_move_tip))
            .showDuration(2500L)
            .closePolicy(ClosePolicy.TOUCH_NONE)
            .floatingAnimation(Tooltip.Animation.DEFAULT)
            .create().doOnHidden {
                showResizeToolTip()
            }

        tooltip.show(vActMainToolTipAnchor, Tooltip.Gravity.TOP, true)
    }

    private fun showResizeToolTip() {
        val tooltip = Tooltip.Builder(this)
            .anchor(vActMainToolTipAnchor, vActMainToolTipAnchor.width / 2, - vActMainToolTipAnchor.height / 2, true)
            .text(getString(R.string.tooltip_show_scale_tip))
            .showDuration(2500L)
            .closePolicy(ClosePolicy.TOUCH_NONE)
            .floatingAnimation(Tooltip.Animation.DEFAULT)
            .create().doOnHidden {
                olActMain.isEnabled = true
        }

        tooltip.show(vActMainToolTipAnchor, Tooltip.Gravity.TOP, true)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            lifecycle.addObserver(analyser)
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT))
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT))
                .build()
                .also {
                    it.setAnalyzer(analyserExecutor, analyser)
                }

            // Select back camera
            val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                preview.setSurfaceProvider(pvActMainScanner.surfaceProvider)

                fabActMainTorch.setOnClickListener {
                    torchOn = !torchOn
                    camera.cameraControl.enableTorch(torchOn)
                    setTorchUI()
                }
                setTorchUI()
            } catch (exc: Exception) {
                Timber.e(exc, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onPause() {
        super.onPause()
        ttsWrapper.stop()
        ivMainTTS.setImageResource(R.drawable.ic_baseline_volume_up_24)
        ivMainTTS.tag = null
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsWrapper.shutdown()
    }

    private fun setTorchUI() {
        fabActMainTorch.setImageResource(if (torchOn) R.drawable.ic_baseline_flash_off_24dp_white else R.drawable.ic_baseline_flash_on_24dp_white)
    }

    companion object {
        private const val TARGET_PREVIEW_WIDTH = 960
        private const val TARGET_PREVIEW_HEIGHT = 1280
    }
}