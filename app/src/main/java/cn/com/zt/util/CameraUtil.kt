package cn.com.zt.util

import android.app.Activity
import android.media.ExifInterface
import android.util.DisplayMetrics
import android.view.View


/**
 * date:2021/6/15
 * author:zhangteng
 * description:
 */
object CameraUtil {
    const val PHOTO_EXTENSION = ".jpg"
    const val VIDEO_EXTENSION = ".mp4"
    private const val MAX_RECORD_TIME = 15 * 60 * 1000L//视频录制最大时长 15分钟

    const val IMMERSIVE_FLAG_TIMEOUT = 500L
    const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    fun isMaxRecordTime(time: Long): Boolean {
        return time >= MAX_RECORD_TIME
    }

    fun getImageDegree(path: String): Int {
        val exifInterface = ExifInterface(path)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        val metric = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    fun getScreenHeight(activity: Activity): Int {
        val metric = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }
}