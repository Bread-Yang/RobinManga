package template.extensions

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main

fun launchUI(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, null, block)

// Setting it to the value of CoroutineStart.UNDISPATCHED has the effect of starting to execute
// coroutine immediately until its first suspension point as the following example shows:
//fun setup(hello: Text, fab: Circle) {
//    fab.onMouseClicked = EventHandler {
//        println("Before launch")
//        GlobalScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED) { // <--- Notice this change
//            println("Inside coroutine")
//            delay(100)                            // <--- And this is where coroutine suspends
//            println("After delay")
//        }
//        println("After launch")
//    }
//}
//  It prints the following messages on click, confirming that code in the coroutine starts to execute immediately:
//  Before launch
//  Inside coroutine
//  After launch
//  After delay
fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, null, block)