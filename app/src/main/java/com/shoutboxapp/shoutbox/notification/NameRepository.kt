package com.shoutboxapp.shoutbox.notification

class NameRepository(private val dao: NameDao) {
    suspend fun isNameAvailable(): Boolean {
        return dao.getName()?.isNotEmpty() == true
    }

    suspend fun setName(name: String) {
        dao.insertName(NameEntity(name = name))
    }

    suspend fun clearName() {
        dao.clearName()
    }

    suspend fun getName(): String? {
        return dao.getName()
    }
}
