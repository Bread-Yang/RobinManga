package template.ui.reader

import template.data.database.models.Chapter
import template.data.database.models.Manga

/**
 * Created by Robin Yeung on 9/11/18.
 */
class ReaderEvent(val manga: Manga, val chapter: Chapter)