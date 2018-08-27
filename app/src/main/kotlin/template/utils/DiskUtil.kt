package template.utils

object DiskUtil {

    fun hashKeyForDisk(key: String): String {
        return Hash.md5(key)
    }
}