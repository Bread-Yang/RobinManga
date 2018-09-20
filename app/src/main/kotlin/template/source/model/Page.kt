package template.source.model

import android.net.Uri
import io.reactivex.processors.FlowableProcessor
import template.network.ProgressListener
import template.ui.reader.ReaderChapter

/**
 * Created by Robin Yeung on 8/22/18.
 */
class Page(
        val index: Int,
        val url: String = "",
        var imageUrl: String? = null,
        @Transient var uri: Uri? = null     // 用transient关键字标记的成员变量不参与序列化过程
) : ProgressListener {

    companion object {
        const val QUEUE = 0
        const val LOAD_PAGE = 1
        const val DOWNLOAD_IMAGE = 2
        const val READY = 3
        const val ERROR = 4
    }

    val number: Int
        get() = index + 1

    @Transient
    lateinit var chapter: ReaderChapter

    // 如果一个变量加了volatile关键字，就会告诉编译器和JVM的内存模型：这个变量是对所有线程共享的、可见的，每次JVM都会读取最新写入的值并使其最新值在所有CPU可见,但不能保证操作的原子性(所以有可能读取到脏数据)
    // 如果不加上volatile关键字,假如线程A和线程B都对该变量操作，则两个线程操作的都是该变量的副本，两个线程都不知道对方已经修改了该数据 https://blog.csdn.net/nugongahou110/article/details/49927667
    @Transient
    @Volatile
    var status: Int = 0
        set(value) {
            field = value
            statusProcessor?.onNext(value)
        }

    @Transient
    @Volatile
    var progress: Int = 0

    @Transient
    private var statusProcessor: FlowableProcessor<Int>? = null

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        progress =
                if (contentLength > 0)
                    (100 * bytesRead / contentLength).toInt()
                else
                    -1
    }

    fun setStatusProcessor(subject: FlowableProcessor<Int>?) {
        this.statusProcessor = subject
    }
}