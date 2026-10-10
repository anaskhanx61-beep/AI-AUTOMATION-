package com.example.data

import com.example.model.ShortVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShortsRepository(private val shortsDao: ShortsDao) {
    val allShorts: Flow<List<ShortVideo>> = shortsDao.getAllShorts().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getShortById(id: String): ShortVideo? {
        return shortsDao.getShortById(id)?.toDomain()
    }

    suspend fun saveShort(video: ShortVideo) {
        shortsDao.insertShort(ShortsEntity.fromDomain(video))
    }

    suspend fun updateShort(video: ShortVideo) {
        shortsDao.updateShort(ShortsEntity.fromDomain(video))
    }

    suspend fun deleteShort(id: String) {
        shortsDao.deleteById(id)
    }
}
