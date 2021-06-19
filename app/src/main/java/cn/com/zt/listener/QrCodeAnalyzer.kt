package cn.com.zt.listener

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyser(private val listener: (List<Barcode>, Int, Int) -> Unit) :
    ImageAnalysis.Analyzer {
    //配置当前扫码格式
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        ).build()

    //获取解析器
    private val detector = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: kotlin.run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { barCodes ->
                Log.e("ztzt", "barCodes: ${barCodes.size}")
                if (barCodes.size > 0) {
                    listener.invoke(barCodes, imageProxy.width, imageProxy.height)
                    //接收到结果后，就关闭解析
                    detector.close()
                }
            }
            .addOnFailureListener { Log.e("ztzt", "Error: ${it.message}") }
            .addOnCompleteListener { imageProxy.close() }
    }
}