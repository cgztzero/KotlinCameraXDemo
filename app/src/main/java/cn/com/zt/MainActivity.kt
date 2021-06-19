package cn.com.zt

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.com.zt.activity.controller.CameraControllerActivity
import cn.com.zt.activity.scan.QRCodeActivity
import cn.com.zt.activity.TakePictureActivity
import cn.com.zt.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX

/**
 * date:2021/6/15
 * author:zhangteng
 * description:
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        binding.takePicture.setOnClickListener {
            checkPermission(0)
        }
        binding.cameraController.setOnClickListener {
            checkPermission(1)
        }
        binding.qrCode.setOnClickListener {
            checkPermission(2)
        }
    }

    private fun intentToTakePicture() {
        val intent = Intent(this, TakePictureActivity::class.java)
        startActivity(intent)
    }

    private fun intentToCameraController() {
        val intent = Intent(this, CameraControllerActivity::class.java)
        startActivity(intent)
    }


    private fun checkPermission(mode: Int) {
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
//            .explainReasonBeforeRequest()
//            .onExplainRequestReason { scope, deniedList ->
//                scope.showRequestReasonDialog(
//                    deniedList,
//                    "拍摄照片需要相机权限,授权后才可以使用", "授权", "取消"
//                )
//            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    //所有权限已经授权
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show()
                    intentTo(mode)
                } else {
                    Toast.makeText(this, "拒绝权限: $deniedList", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun intentTo(mode: Int) {
        when (mode) {
            0 -> intentToTakePicture()
            1 -> intentToCameraController()
            2 -> intentTOQRCode()
        }
    }

    private fun intentTOQRCode() {
        val intent = Intent(this, QRCodeActivity::class.java)
        startActivity(intent)
    }
}