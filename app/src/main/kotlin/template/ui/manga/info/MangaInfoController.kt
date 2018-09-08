package template.ui.manga.info

import android.app.Dialog
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import jp.wasabeef.glide.transformations.CropSquareTransformation
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlinx.android.synthetic.main.manga_info_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.database.models.Manga
import template.data.notification.NotificationReceiver
import template.extensions.getResourceColor
import template.extensions.toast
import template.extensions.truncateCenter
import template.glide.GlideApp
import template.source.Source
import template.source.model.SManga
import template.source.online.HttpSource
import template.ui.MainActivity
import template.ui.base.controller.DialogController
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController
import template.ui.manga.MangaController
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*

/**
 * Fragment that shows manga information.
 * Uses R.layout.manga_info_controller.
 * UI related actions should be called from here.
 */
@Layout(R.layout.manga_info_controller)
@RequiresPresenter(MangaInfoPresenter::class)
class MangaInfoController : NucleusController<MangaInfoPresenter>() {

    override fun onViewCreated(view: View) {

        // Set onclickListener to toggle favorite when FAB clicked.
        fabFavorite.clicks().subscribeUntilDestroy {
            onFabClick()
        }

        // Set SwipeRefresh to refresh manga data.
        swipeRefresh.refreshes().subscribeUntilDestroy {
            fetchMangaFromSource()
        }

        tvMangaFullTitle.longClicks().subscribeUntilDestroy {
            copyToClipboard(view.context.getString(R.string.title), tvMangaFullTitle.text.toString())
        }

        tvMangaFullTitle.clicks().subscribeUntilDestroy {
            performGlobalSearch(tvMangaFullTitle.text.toString())
        }

        tvMangaArtist.longClicks().subscribeUntilDestroy {
            copyToClipboard(tvMangaArtistLabel.text.toString(), tvMangaArtist.text.toString())
        }

        tvMangaArtist.clicks().subscribeUntilDestroy {
            performGlobalSearch(tvMangaArtist.text.toString())
        }

        tvMangaAuthor.longClicks().subscribeUntilDestroy {
            copyToClipboard(tvMangaAuthor.text.toString(), tvMangaAuthor.text.toString())
        }

        tvMangaAuthor.clicks().subscribeUntilDestroy {
            performGlobalSearch(tvMangaAuthor.text.toString())
        }

        tvMangaSummary.longClicks().subscribeUntilDestroy {
            copyToClipboard(view.context.getString(R.string.description), tvMangaSummary.text.toString())
        }

        //manga_genres_tags.setOnTagClickListener { tag -> performGlobalSearch(tag) }

        ivMangaCover.longClicks().subscribeUntilDestroy {
            copyToClipboard(view.context.getString(R.string.title), presenter.manga.title)
        }
    }

    override fun initPresenterOnce() {
        val ctrl = parentController as MangaController
        presenter.init(ctrl)
    }


    /**
     * Check if manga is initialized.
     * If true update view with manga information,
     * if false fetch manga information
     *
     * @param manga manga object containing information about manga.
     * @param source the source of the manga.
     */
    fun onNextManga(manga: Manga, source: Source) {
        if (manga.initialized) {
            // Update view.
            setMangaInfo(manga, source)
        } else {
            // Initialize manga.
            fetchMangaFromSource()
        }
    }

    /**
     * Update the view with manga information.
     *
     * @param manga manga object containing information about manga.
     * @param source the source of the manga.
     */
    private fun setMangaInfo(manga: Manga, source: Source?) {
        val view = view ?: return

        //update full title TextView.
        tvMangaFullTitle.text = if (manga.title.isBlank()) {
            view.context.getString(R.string.unknown)
        } else {
            manga.title
        }

        // Update artist TextView.
        tvMangaArtist.text = if (manga.artist.isNullOrBlank()) {
            view.context.getString(R.string.unknown)
        } else {
            manga.artist
        }

        // Update author TextView.
        tvMangaAuthor.text = if (manga.author.isNullOrBlank()) {
            view.context.getString(R.string.unknown)
        } else {
            manga.author
        }

        // If manga source is known update source TextView.
        tvMangaSource.text = if (source == null) {
            view.context.getString(R.string.unknown)
        } else {
            source.toString()
        }

        // Update genres list
        if (manga.genre.isNullOrBlank().not()) {
            tagGroupMangaGenres.setTags(manga.genre?.split(", "))
        }

        // Update description TextView.
        tvMangaSummary.text = if (manga.description.isNullOrBlank()) {
            view.context.getString(R.string.unknown)
        } else {
            manga.description
        }

        // Update status TextView.
        tvMangaStatus.setText(when (manga.status) {
            SManga.ONGOING -> R.string.ongoing
            SManga.COMPLETED -> R.string.completed
            SManga.LICENSED -> R.string.licensed
            else -> R.string.unknown
        })

        // Set the favorite drawable to the correct one.
        setFavoriteDrawable(manga.favorite)

        // Set cover if it wasn't already.
        if (ivMangaCover.drawable == null && !manga.thumbnail_url.isNullOrEmpty()) {
            GlideApp.with(view.context)
                    .load(manga)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(ivMangaCover)

            if (ivBackdrop != null) {
                GlideApp.with(view.context)
                        .load(manga)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .into(ivBackdrop)
            }
        }
    }

    override fun onDestroyView(view: View) {
        tagGroupMangaGenres.setOnTagClickListener(null)
        super.onDestroyView(view)
    }

    /**
     * Update chapter count TextView.
     *
     * @param count number of chapters.
     */
    fun setChapterCount(count: Float) {
        if (count > 0f) {
            /**
            比实际数字的位数多，不足的地方用0补上。
            new DecimalFormat("00.00").format(3.14)  //结果：03.14
            new DecimalFormat("0.000").format(3.14)  //结果： 3.140
            new DecimalFormat("00.000").format(3.14)  //结果：03.140
            比实际数字的位数少：整数部分不改动，小数部分，四舍五入
            new DecimalFormat("0.000").format(13.146)  //结果：13.146
            new DecimalFormat("00.00").format(13.146)  //结果：13.15
            new DecimalFormat("0.00").format(13.146)  //结果：13.15
            #：
            比实际数字的位数多，不变。
            new DecimalFormat("##.##").format(3.14)  //结果：3.14
            new DecimalFormat("#.###").format(3.14)  //结果： 3.14
            new DecimalFormat("##.###").format(3.14)  //结果：3.14
            比实际数字的位数少：整数部分不改动，小数部分，四舍五入
            new DecimalFormat("#.###").format(13.146)  //结果：13.146
            new DecimalFormat("##.##").format(13.146)  //结果：13.15
            new DecimalFormat("#.##").format(13.146)  //结果：13.15
             */
            tvMangaChapters?.text = DecimalFormat("#.#").format(count)
        } else {
            tvMangaChapters?.text = resources?.getString(R.string.unknown)
        }
    }

    fun setLastUpdateDate(date: Date) {
        if (date.time != 0L) {
            tvMangaLastUpdate?.text = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
        } else {
            tvMangaLastUpdate?.text = resources?.getString(R.string.unknown)
        }
    }

    /**
     * Toggles the favorite status and asks for confirmation to delete downloaded chapters.
     */
    private fun toggleFavorite() {
        val view = view

//        val isNowFavorite = presenter.toggleFavorite()
//        if (view != null && !isNowFavorite && presenter.hasDownloads()) {
//            view.snack(view.context.getString(R.string.delete_downloads_for_manga)) {
//                setAction(R.string.action_delete) {
//                    presenter.deleteDownloads()
//                }
//            }
//        }
    }

    /**
     * Open the manga in browser.
     */
    private fun openInBrowser() {
        val context = view?.context ?: return
        val source = presenter.source as? HttpSource ?: return

        try {
            val url = Uri.parse(source.mangaDetailsRequest(presenter.manga).url().toString())
            val intent = CustomTabsIntent.Builder()
                    .setToolbarColor(context.getResourceColor(R.attr.colorPrimary))
                    .build()
            intent.launchUrl(activity, url)
        } catch (e: Exception) {
            context.toast(e.message)
        }
    }

    /**
     * Called to run Intent with [Intent.ACTION_SEND], which show share dialog.
     */
    private fun shareManga() {
        val context = view?.context ?: return

        val source = presenter.source as? HttpSource ?: return
        try {
            val url = source.mangaDetailsRequest(presenter.manga).url().toString()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
        } catch (e: Exception) {
            context.toast(e.message)
        }
    }

    /**
     * Update FAB with correct drawable.
     *
     * @param isFavorite determines if manga is favorite or not.
     */
    private fun setFavoriteDrawable(isFavorite: Boolean) {
        // Set the Favorite drawable to the correct one.
        // Border drawable if false, filled drawable if true.
        fabFavorite?.setImageResource(if (isFavorite)
            R.drawable.ic_bookmark_white_24dp
        else
            R.drawable.ic_add_to_library_24dp)
    }

    /**
     * Start fetching manga information from source.
     */
    private fun fetchMangaFromSource() {
        setRefreshing(true)
        // Call presenter and start fetching manga information
        presenter.fetchMangaFromSource()
    }

    /**
     * Update swipe refresh to stop showing refresh in progress spinner.
     */
    fun onFetchMangaDone() {
        setRefreshing(false)
    }

    /**
     * Update swipe refresh to start showing refresh in progress spinner.
     */
    fun onFetchMangaError() {
        setRefreshing(false)
    }

    /**
     * Set swipe refresh status.
     *
     * @param value whether it should be refreshing or not.
     */
    private fun setRefreshing(value: Boolean) {
        swipeRefresh?.isRefreshing = value
    }

    /**
     * Called when the fab is clicked.
     */
    private fun onFabClick() {
        val manga = presenter.manga
        toggleFavorite()
//        if (manga.favorite) {
//            val categories = presenter.getCategories()
//            val defaultCategory = categories.find { it.id == preferences.defaultCategory() }
//            when {
//                defaultCategory != null -> presenter.moveMangaToCategory(manga, defaultCategory)
//                categories.size <= 1 -> // default or the one from the user
//                    presenter.moveMangaToCategory(manga, categories.firstOrNull())
//                else -> {
//                    val ids = presenter.getMangaCategoryIds(manga)
//                    val preselected = ids.mapNotNull { id ->
//                        categories.indexOfFirst { it.id == id }.takeIf { it != -1 }
//                    }.toTypedArray()
//
//                    ChangeMangaCategoriesDialog(this, listOf(manga), categories, preselected)
//                            .showDialog(router)
//                }
//            }
//            activity?.toast(activity?.getString(R.string.manga_added_library))
//        } else {
//            activity?.toast(activity?.getString(R.string.manga_removed_library))
//        }
    }

    /**
     * Add a shortcut of the manga to the home screen
     */
    private fun addToHomeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createShortcutForShape()
        } else {
            ChooseShapeDialog(this).showDialog(router)
        }
    }

    /**
     * Dialog to choose a shape for the icon.
     */
    private class ChooseShapeDialog(bundle: Bundle? = null) : DialogController(bundle) {

        constructor(target: MangaInfoController) : this() {
            targetController = target
        }

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val modes = intArrayOf(R.string.circular_icon,
                    R.string.rounded_icon,
                    R.string.square_icon,
                    R.string.star_icon)

            return MaterialDialog.Builder(activity!!)
                    .title(R.string.icon_shape)
                    .negativeText(android.R.string.cancel)
                    .items(modes.map { activity?.getString(it) })
                    .itemsCallback { _, _, i, _ ->
                        (targetController as? MangaInfoController)?.createShortcutForShape(i)
                    }
                    .build()
        }
    }

    /**
     * Retrieves the bitmap of the shortcut with the requested shape and calls [createShortcut] when
     * the resource is available.
     *
     * @param i The shape index to apply. Defaults to circle crop transformation.
     */
    private fun createShortcutForShape(i: Int = 0) {
        if (activity == null) return
        GlideApp.with(activity!!)
                .asBitmap()
                .load(presenter.manga)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .apply {
                    when (i) {
                        0 -> circleCrop()
                        1 -> transform(RoundedCorners(5))
                        2 -> transform(CropSquareTransformation())
                        3 -> centerCrop().transform(MaskTransformation(R.drawable.mask_star))
                    }
                }
                .into(object : SimpleTarget<Bitmap>(96, 96) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        createShortcut(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        activity?.toast(R.string.icon_creation_fail)
                    }
                })
    }

    /**
     * Copies a string to clipboard
     *
     * @param label Label to show to the user describing the content
     * @param content the actual text to copy to the board
     */
    private fun copyToClipboard(label: String, content: String) {
        if (content.isBlank()) return

        val activity = activity ?: return
        val view = view ?: return

        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(label, content)

        activity.toast(view.context.getString(R.string.copied_to_clipboard, content.truncateCenter(20)),
                Toast.LENGTH_SHORT)
    }

    /**
     * Perform a global search using the provided query.
     *
     * @param query the search query to pass to the search controller
     */
    fun performGlobalSearch(query: String) {
        val router = parentController?.router ?: return
//        router.pushController(CatalogueSearchController(query).withFadeTransaction())
    }

    /**
     * Create shortcut using ShortcutManager.
     *
     * @param icon The image of the shortcut.
     */
    private fun createShortcut(icon: Bitmap) {
        val activity = activity ?: return
        val mangaControllerArgs = parentController?.args ?: return

        // Create the shortcut intent.
        val shortcutIntent = activity.intent
                .setAction(MainActivity.SHORTCUT_MANGA)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(MangaController.MANGA_EXTRA,
                        mangaControllerArgs.getLong(MangaController.MANGA_EXTRA))

        // Check if shortcut placement is supported
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(activity)) {
            val shortcutId = "manga-shortcut-${presenter.manga.title}-${presenter.source.name}"

            // Create shortcut info
            val shortcutInfo = ShortcutInfoCompat.Builder(activity, shortcutId)
                    .setShortLabel(presenter.manga.title)
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .setIntent(shortcutIntent)
                    .build()

            val successCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the CallbackIntent.
                val intent = ShortcutManagerCompat.createShortcutResultIntent(activity, shortcutInfo)

                // Configure the intent so that the broadcast receiver gets the callback successfully.
                PendingIntent.getBroadcast(activity, 0, intent, 0)
            } else {
                NotificationReceiver.shortcutCreatedBroadcast(activity)
            }

            // Request shortcut.
            ShortcutManagerCompat.requestPinShortcut(activity, shortcutInfo,
                    successCallback.intentSender)
        }
    }
}