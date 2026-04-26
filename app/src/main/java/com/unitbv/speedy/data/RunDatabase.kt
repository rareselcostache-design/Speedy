package com.unitbv.speedy.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long,
    val distanceMeters: Float,
    val durationSeconds: Long,
    val runType: String,
    val caloriesBurned: Int
)

@Dao
interface RunDao {
    @Insert
    suspend fun insertRun(run: RunEntity)

    @Query("SELECT * FROM runs ORDER BY dateMillis DESC")
    suspend fun getAllRuns(): List<RunEntity>
}

@Database(entities = [RunEntity::class], version = 1)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao

    companion object {
        @Volatile private var INSTANCE: RunDatabase? = null

        fun getInstance(context: Context): RunDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, RunDatabase::class.java, "speedy_db")
                    .build().also { INSTANCE = it }
            }
    }
}