package template.ui.library

import template.data.database.models.Manga

// https://proandroiddev.com/kotlin-sealed-classes-enums-with-swag-d3c4b799bcd4
sealed class LibrarySelectionEvent {

    class Selected(val manga: Manga) : LibrarySelectionEvent()
    class Unselected(val manga: Manga) : LibrarySelectionEvent()
    class Cleared : LibrarySelectionEvent()
}
