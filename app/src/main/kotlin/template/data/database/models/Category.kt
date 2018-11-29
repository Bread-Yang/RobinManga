package template.data.database.models

import java.io.Serializable

interface Category : Serializable {

    var id: Int?

    var name: String

    var order: Int

    var flags: Int

    val nameLower: String
        get() = name.toLowerCase()

    companion object {

        fun create(name: String): Category = CategoryImpl().apply {
            this.name = name
        }

        fun createDefault(): Category = create("default").apply {
            id = 0
        }
    }
}
