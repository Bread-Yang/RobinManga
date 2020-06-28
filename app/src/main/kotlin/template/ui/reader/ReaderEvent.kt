package template.ui.reader

import template.data.database.models.Chapter
import template.data.database.models.Manga

class ReaderEvent(val manga: Manga, val chapter: Chapter)