package cn.com.zt.activity.scan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.com.zt.databinding.ActivityResultBinding

/**
 * date:2021/6/18
 * author:zhangteng
 * description:
 */
class QRCodeResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    companion object {
        const val RESULT_KEY = "result_key";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        binding.text.text = intent.getStringExtra(RESULT_KEY)
        binding.button.setOnClickListener {
            finish()
        }
    }
}