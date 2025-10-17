package com.tuusuario.lab6.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearchEntity)

    @Query("SELECT * FROM recent_searches ORDER BY lastUsedAt DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<RecentSearchEntity>>

    @Query("""
        DELETE FROM recent_searches 
        WHERE query NOT IN (
            SELECT query FROM recent_searches 
            ORDER BY lastUsedAt DESC 
            LIMIT :limit
        )
    """)
    suspend fun cleanOldSearches(limit: Int = 10)

    @Query("DELETE FROM recent_searches")
    suspend fun deleteAll()
}