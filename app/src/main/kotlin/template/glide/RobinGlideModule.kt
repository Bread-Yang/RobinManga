package template.glide

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import template.data.database.models.Manga
import template.network.NetworkHelper
import java.io.InputStream

/**
 * Class used to update Glide module settings
 */
@GlideModule
class RobinGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 50 * 1024 * 1024))
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
        builder.setDefaultTransitionOptions(Drawable::class.java,
                DrawableTransitionOptions.withCrossFade())
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val networkHelper = NetworkHelper(context)
        val networkFactory = OkHttpUrlLoader.Factory(networkHelper.client)

        registry.replace(GlideUrl::class.java, InputStream::class.java, networkFactory)
        registry.append(Manga::class.java, InputStream::class.java, MangaModelLoader.Factory())
    }
}