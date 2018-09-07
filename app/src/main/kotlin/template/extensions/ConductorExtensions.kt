package template.extensions

import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler

/**
 * Created by Robin Yeung on 8/22/18.
 */
fun Controller.requestPermissionsSafe(permissions: Array<String>, requestCode: Int) {
    val activity = activity ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(it), requestCode)
            }
        }
    }
}

fun Controller.withFadeTransaction(): RouterTransaction {
    return RouterTransaction.with(this)
            .pushChangeHandler(FadeChangeHandler())
            .popChangeHandler(FadeChangeHandler())
}