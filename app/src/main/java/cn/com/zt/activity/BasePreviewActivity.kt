package cn.com.zt.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import cn.com.zt.util.CameraUtil
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * date:2021/6/15
 * author:zhangteng
 * description:
 */
open class BasePreviewActivity : BaseActivity() {
    protected var mPreview: Preview? = null
    protected lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    protected lateinit var cameraExecutor: ExecutorService
    protected lateinit var processCameraProvider: ProcessCameraProvider
    protected lateinit var cameraSelector: CameraSelector
    protected var imageCapture: ImageCapture? = null
    protected var outputDirectory: String? = null
    protected var imageAnalyzer: ImageAnalysis? = null
    protected var lensFacing: Int = -1//记录是前置or后置摄像头
    protected var mCamera: Camera? = null

    /**
     * 开始预览
     */
    protected fun startPreview() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            //初始化一次即可
            processCameraProvider = cameraProviderFuture.get()
            //实例化 旋转屏幕调用多次
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))//异步 ContextCompat.getMainExecutor相当于通过handler回调在主线程
    }

    /**
     * 实例化相机&预览
     */
    open fun bindCameraUseCases() {
        //获得可预览对象
        mPreview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio())//比例
            .setTargetRotation(getRotation())//设置了旋转 就不需要在判断file里的旋转信息了
            .build()
        //默认选择后置摄像头
        if (lensFacing == -1) {
            lensFacing = CameraSelector.LENS_FACING_BACK
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        //保险起见 先解绑所有
        processCameraProvider.unbindAll()
        //初始化拍照实例
        initTakePictureCases()
        // 所有实例绑定到生命周期
        mCamera = processCameraProvider.bindToLifecycle(
            this,
            cameraSelector,
            mPreview,
            imageCapture,
            imageAnalyzer
        )
        //预览加载到view上 子类具体实现
        onPreviewPrepared(mPreview)
    }

    open fun onPreviewPrepared(preview: Preview?) {

    }

    /**
     * 初始化拍照实例
     */
    private fun initTakePictureCases() {
        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .setTargetAspectRatio(aspectRatio())
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//低延迟低质量 or 高延迟高质量
            .setTargetRotation(getRotation())
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)//闪光灯自动
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            //注意源码注释： It is not allowed to set both target aspect ratio and target resolution on the same use case
//            .setTargetResolution(Size(1280, 720))
            .setTargetRotation(aspectRatio())
            .build()
    }


    /**
     * 计算比例 文档里官方写法
     */
    private fun aspectRatio(): Int {
        val width = getActivityWidth()
        val height = getActivityHeight()
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun getRotation(): Int {
        return window.decorView.display.rotation
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private fun getActivityWidth(): Int {
        return CameraUtil.getScreenWidth(this)
    }

    private fun getActivityHeight(): Int {
        return CameraUtil.getScreenHeight(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bindCameraUseCases()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}