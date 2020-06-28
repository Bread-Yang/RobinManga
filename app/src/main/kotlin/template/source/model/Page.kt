package template.source.model

import android.net.Uri
import io.reactivex.processors.FlowableProcessor
import template.network.DownloadProgressListener
import template.ui.reader.ReaderChapter


class Page(
        val index: Int,
        val url: String = "",
        var imageUrl: String? = null,       // uri 和 Url 的区别 : https://www.cnblogs.com/wuyun-blog/p/5706703.html
        @Transient var uri: Uri? = null     // 用transient关键字标记的成员变量不参与序列化过程
) : DownloadProgressListener {

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

    /**
     * 当前Page的状态 : QUEUE,LOAD_PAGE,DOWNLOAD_IMAGE,READY,ERROR
     */
    @Transient
    // 如果一个变量加了volatile关键字，就会告诉编译器和JVM的内存模型：这个变量是对所有线程共享的、可见的，每次JVM都会读取最新写入的值并使其最新值在所有CPU可见,但不能保证操作的原子性(所以有可能读取到脏数据)
    // 如果不加上volatile关键字,假如线程A和线程B都对该变量操作，则两个线程操作的都是该变量的副本，两个线程都不知道对方已经修改了该数据 https://blog.csdn.net/nugongahou110/article/details/49927667
    @Volatile
    var status: Int = 0
        set(value) {
            field = value
            statusProcessor?.onNext(value)
        }

    /**
     * 图片下载进度，取值范围 : 0～100
     */
    @Transient
    @Volatile
    var imageDownloadProgress: Int = 0

    @Transient
    private var statusProcessor: FlowableProcessor<Int>? = null // FlowableProcessor是PublicProcessor等等的基类

    override fun updateProgress(bytesRead: Long, contentLength: Long, done: Boolean) {
        imageDownloadProgress =
                if (contentLength > 0)
                    (100 * bytesRead / contentLength).toInt()
                else
                    -1
    }

    fun setStatusProcessor(subject: FlowableProcessor<Int>?) {
        this.statusProcessor = subject
    }
}