package template.source.model

/**
 * Created by Robin Yeung on 8/23/18.
 */
data class FilterList(val list: List<Filter<*>>) : List<Filter<*>> by list {

    constructor(vararg fs: Filter<*>) :
            this(
                    if (fs.isNotEmpty())
                        fs.asList()
                    else
                        emptyList()
            )
}