package cn.com.zt.activity

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import cn.com.zt.databinding.ActivityPictureBinding
import cn.com.zt.util.CameraUtil
import java.io.File

/**
 * date:2021/5/26
 * author:zhangteng
 * description:
 * 自己重写很多东西很麻烦 建议使用cameraController
 */
class TakePictureActivity : BasePreviewActivity() {
    private lateinit var binding: ActivityPictureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startPreview()
        initClickListener()

        binding.startRecord.visibility = View.GONE
        binding.stopRecord.visibility = View.GONE
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
    }

    /**
     * 闪光灯
     */
    private fun setFlashMode(mode: Int) {
        if (imageCapture?.flashMode == mode) {
            return
        }

        imageCapture?.flashMode = mode
        when (mode) {
            ImageCapture.FLASH_MODE_AUTO -> Toast.makeText(this, "闪光灯自动", Toast.LENGTH_SHORT).show()
            ImageCapture.FLASH_MODE_ON -> Toast.makeText(this, "闪光灯打开", Toast.LENGTH_SHORT).show()
            ImageCapture.FLASH_MODE_OFF -> Toast.makeText(this, "闪光灯关闭", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 切换摄像头
     */
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        //需要从新绑定实例
        bindCameraUseCases()
    }

    /**
     * 拍照
     */
    private fun takePicture() {
        if (outputDirectory.isNullOrEmpty()) {
            outputDirectory =
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/Test/"
            val file = File(outputDirectory)
            if (!file.exists()) {
                file.mkdirs()
            }
        }

        val photoFile = createFile()
        val metadata = ImageCapture.Metadata().apply {
            //水平翻转
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        imageCapture?.takePicture(
            outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("ztzt", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Log.e("ztzt", "Photo capture succeeded: $savedUri")
                }
            })
    }

    override fun onPreviewPrepared(preview: Preview?) {
        super.onPreviewPrepared(preview)
        mPreview?.setSurfaceProvider(binding.previewView.surfaceProvider)
    }

    private fun createFile(): File {
        return File(
            outputDirectory, System.currentTimeMillis().toString() + CameraUtil.PHOTO_EXTENSION
        )
    }
}