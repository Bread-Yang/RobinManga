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
    // 对于面向 Android 7.0 的应用，Android 框架执行的 StrictMode API 政策禁止在您的应用外部公开 file:// URI。
    // 如果一项包含文件 URI 的 intent 离开您的应用，则应用出现故障，并出现 FileUriExposedException 异常。
    // 要在应用间共享文件，您应发送一项 content:// URI，并授予 URI 临时访问权限。
    // 进行此授权的最简单方式是使用 FileProvider 类。如需了解有关权限和共享文件的详细信息，请参阅共享文件。
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", this)
    else
        Uri.fromFile(this)
}
