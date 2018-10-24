package template.extensions

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import template.BuildConfig
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
    // https://blog.csdn.net/growing_tree/article/details/71190741
    // 对于面向 Android 7.0 的应用，Android 框架执行的 StrictMode API 政策禁止在您的应用外部公开 file:// URI。
    // 如果一项包含文件 URI 的 intent 离开您的应用，则应用出现故障，并出现 FileUriExposedException 异常。
    // 要在应用间共享文件，您应发送一项 content:// URI，并授予 URI 临时访问权限。
    // 进行此授权的最简单方式是使用 FileProvider 类。如需了解有关权限和共享文件的详细信息，请参阅共享文件。
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        // 需要传递三个参数。第二个参数便是 Manifest 文件中注册 FileProvider 时设置的 authorities 属性值，
        // 第三个参数为要共享的文件，并且这个文件一定位于第二步我们在 path 文件中添加的子目录里面
        // 如uri为 ：
        // file:///storage/emulated/0/Android/data/template.debug/cache/chapter_disk_cache/0ceb2aa2ba798cab786551f9b7739e36.0
        // 则返回 ：
        // content://template.debug.provider/ext_cache_files/chapter_disk_cache/0ceb2aa2ba798cab786551f9b7739e36.0
        // "cache"被替换成"ext_cache_files"(在provider_paths.xml中指明)
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", this)
    else
        Uri.fromFile(this)
}
