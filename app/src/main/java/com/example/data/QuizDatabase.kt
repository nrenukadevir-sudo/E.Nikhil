package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 100,
    val totalPlayed: Int = 0,
    val totalCorrect: Int = 0,
    val highScore: Int = 0
)

@Entity(tableName = "custom_questions")
data class CustomQuizQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", or "D"
    val category: String, // "GK", "Movies", "Gaming", "Sports", "Science", "Telugu", etc.
    val theme: String = "General", // "General", "Gaming", "Telugu"
    val timestamp: Long = System.currentTimeMillis()
)

// --- Daos ---

@Dao
interface QuizDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    @Query("SELECT * FROM custom_questions ORDER BY timestamp DESC")
    fun getAllCustomQuestions(): Flow<List<CustomQuizQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomQuestion(question: CustomQuizQuestion)

    @Query("DELETE FROM custom_questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Int)
    
    @Query("DELETE FROM custom_questions")
    suspend fun clearAllCustomQuestions()
}

// --- App Database ---

@Database(entities = [UserProgress::class, CustomQuizQuestion::class], version = 1, exportSchema = false)
abstract class QuizDatabase : RoomDatabase() {
    abstract val quizDao: QuizDao
}

// --- Repository Pattern ---

class QuizRepository(private val dao: QuizDao) {
    val userProgress: Flow<UserProgress?> = dao.getUserProgress()
    val customQuestions: Flow<List<CustomQuizQuestion>> = dao.getAllCustomQuestions()

    suspend fun updateProgress(progress: UserProgress) {
        dao.insertOrUpdateProgress(progress)
    }

    suspend fun addCustomQuestion(question: CustomQuizQuestion) {
        dao.insertCustomQuestion(question)
    }

    suspend fun deleteQuestion(id: Int) {
        dao.deleteQuestionById(id)
    }
    
    suspend fun clearQuestions() {
        dao.clearAllCustomQuestions()
    }
}
