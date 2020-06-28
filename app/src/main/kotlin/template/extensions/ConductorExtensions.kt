package template.extensions

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler

fun Router.popControllerWithTag(tag: String): Boolean {
    val controller = getControllerWithTag(tag)
    if (controller != null) {
        popController(controller)
        return true
    }
    return false
}

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