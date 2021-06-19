package cn.com.zt.activity

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import cn.com.zt.util.CameraUtil

/**
 * date:2021/6/16
 * author:zhangteng
 * description:
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        window.decorView.postDelayed({
            window.decorView.systemUiVisibility = CameraUtil.FLAGS_FULLSCREEN
        }, CameraUtil.IMMERSIVE_FLAG_TIMEOUT)
    }
}