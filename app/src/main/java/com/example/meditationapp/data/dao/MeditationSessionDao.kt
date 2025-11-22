package com.example.meditationapp.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.meditationapp.data.models.MeditationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationSessionDao {

    @Insert
    suspend fun insert(session: MeditationSession)

    @Query("SELECT SUM(durationSeconds) FROM meditation_sessions")
    fun getTotalDurationSeconds(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM meditation_sessions")
    fun getSessionsCount(): Flow<Int>

    @Query("""
    SELECT DISTINCT strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch')) 
    FROM meditation_sessions 
    ORDER BY 1 DESC
""")
    fun getAllUniqueDates(): Flow<List<String>>

}
