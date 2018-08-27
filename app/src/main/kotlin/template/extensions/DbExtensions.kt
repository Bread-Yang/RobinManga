package template.extensions

import com.pushtorefresh.storio3.sqlite.StorIOSQLite

inline fun StorIOSQLite.inTransaction(block: () -> Unit) {
    lowLevel().beginTransaction()
    try {
        block()
        lowLevel().setTransactionSuccessful()
    } finally {
        lowLevel().endTransaction()
    }
}

inline fun <T> StorIOSQLite.inTransactionReturn(block: () -> T): T {
    lowLevel().beginTransaction()
    try {
        val result = block()
        lowLevel().setTransactionSuccessful()
        return result
    } finally {
        lowLevel().endTransaction()
    }
}