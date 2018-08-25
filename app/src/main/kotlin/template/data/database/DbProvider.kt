package template.data.database

import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite

/**
 * Created by Robin Yeung on 8/25/18.
 */
interface DbProvider {

    val db: DefaultStorIOSQLite

}