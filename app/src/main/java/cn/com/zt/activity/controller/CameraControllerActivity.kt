package cn.com.zt.activity.controller

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import cn.com.zt.R
import cn.com.zt.activity.BaseActivity
import cn.com.zt.databinding.ActivityPictureBinding
import cn.com.zt.util.CameraUtil
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * date:2021/6/16
 * author:zhangteng
 * description:使用cameraController更方便
 */
class CameraControllerActivity : BaseActivity() {
    private lateinit var binding: ActivityPictureBinding
    private lateinit var lifecycleCameraController: LifecycleCameraController
    private lateinit var cameraExecutor: ExecutorService
    private var focusAnim: ScaleAnimation? = null
    private lateinit var outputDirectory: String
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var recordTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFile()
        initController()
        initClickListener()
    }

    @SuppressLint("ClickableViewAccessibility", "UnsafeOptInUsageError")
    private fun initController() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        lifecycleCameraController = LifecycleCameraController(this)
        lifecycleCameraController.bindToLifecycle(this)
        lifecycleCameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
        binding.previewView.controller = lifecycleCameraController
        binding.previewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startFocusAnim(event.x.toInt(), event.y.toInt())
            }
            false
        }
    }

    private fun initFile() {
        outputDirectory =
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/CameraControllerTest/"
        val file = File(outputDirectory)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    private fun initClickListener() {
        binding.takePicture.setOnClickListener {
            takePicture()
        }
        binding.switchCamera.setOnClickListener {
            switchCamera()
        }

        binding.autoFlash.setOnClickListener {
            setFlashMode(ImageCapture.FLASH_MODE_AUTO)
        }

        binding.openFlash.setOnClickListener {
            setFlashMode(ImageCapture.FLASH_MODE_ON)
        }

        binding.closeFlash.setOnClickListener {
            setFlashMode(ImageCapture.FLASH_MODE_OFF)
        }

        binding.startRecord.setOnClickListener {
            startRecord()
        }

        binding.stopRecord.setOnClickListener {
            stopRecord()
        }
    }

    private fun setFlashMode(mode: Int) {
        if (lifecycleCameraController.imageCaptureFlashMode == mode) {
            return
        }
        lifecycleCameraController.imageCaptureFlashMode = mode
        when (mode) {
            ImageCapture.FLASH_MODE_AUTO -> Toast.makeText(this, "闪光灯自动", Toast.LENGTH_SHORT).show()
            ImageCapture.FLASH_MODE_ON -> Toast.makeText(this, "闪光灯打开", Toast.LENGTH_SHORT).show()
            ImageCapture.FLASH_MODE_OFF -> Toast.makeText(this, "闪光灯关闭", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        lifecycleCameraController.cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()
    }

    private fun startFocusAnim(x: Int, y: Int) {
        if (binding.focusImage.isShown) {
            return
        }

        if (focusAnim === null) {
            focusAnim = AnimationUtils.loadAnimation(this, R.anim.focus_anim) as ScaleAnimation?
            focusAnim?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    binding.focusImage.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }

        binding.focusImage.visibility = View.VISIBLE
        binding.focusImage.post {
            binding.focusImage.layout(
                x - binding.focusImage.width / 2,
                y - binding.focusImage.height / 2,
                x + binding.focusImage.width / 2,
                y + binding.focusImage.height / 2
            )
            binding.focusImage.startAnimation(focusAnim)
        }
    }

    /**
     * 拍照
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun takePicture() {
        val photoFile = createPhotoFile()
        val metadata = ImageCapture.Metadata().apply {
            //水平翻转
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()
        lifecycleCameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
        lifecycleCameraController.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d("OnImageSavedCallback", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Log.d("OnImageSavedCallback", "Photo capture succeeded: $savedUri")
                }
            })
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startRecord() {
        if (isRecording()) {
            Toast.makeText(this, "正在录像", Toast.LENGTH_SHORT).show()
            return
        }

        val videoFile = createVideoFile()
        val outputOptions = OutputFileOptions.builder(videoFile).build()
        lifecycleCameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        lifecycleCameraController.startRecording(
            outputOptions,
            cameraExecutor,
            object : OnVideoSavedCallback {
                override fun onVideoSaved(output: OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(videoFile)
                    Log.e("ztzt", "onVideoSaved：${savedUri.path}")
                    //这里注意 是在视频录制的线程的回调
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraControllerActivity,
                            "录像完成：${savedUri.path}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    Log.e("ztzt", "onError：${message}")
                }
            })
        if (isRecording()) {
            Toast.makeText(this, "开始录像", Toast.LENGTH_SHORT).show()
            startChronometer()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun stopRecord() {
        if (!isRecording()) {
            return
        }
        binding.chronometer.stop()
        binding.chronometer.visibility = View.GONE
        lifecycleCameraController.stopRecording()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun isRecording(): Boolean {
        return lifecycleCameraController.isRecording
    }

    /**
     * 录像计时
     */
    private fun startChronometer() {
        val startTime = System.currentTimeMillis()
        binding.chronometer.setOnChronometerTickListener {
            recordTime = (System.currentTimeMillis() - startTime) / 1000
            Log.e("ztzt", "startTime：$startTime")
            Log.e("ztzt", "recordTime：$recordTime")
            val asText = String.format("%02d", recordTime / 60) + ":" + String.format(
                "%02d",
                recordTime % 60
            )
            it.text = asText
            if (CameraUtil.isMaxRecordTime(recordTime)) {
                stopRecord()
            }
        }
        binding.chronometer.start()
        binding.chronometer.visibility = View.VISIBLE
    }

    private fun createPhotoFile(): File {
        return File(
            outputDirectory, System.currentTimeMillis().toString() + CameraUtil.PHOTO_EXTENSION
        )
    }

    private fun createVideoFile(): File {
        return File(
            outputDirectory,
            System.currentTimeMillis().toString() + CameraUtil.VIDEO_EXTENSION
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}