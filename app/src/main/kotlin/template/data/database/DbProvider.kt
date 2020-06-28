package template.data.database

import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite

interface DbProvider {

    val db: DefaultStorIOSQLite

}