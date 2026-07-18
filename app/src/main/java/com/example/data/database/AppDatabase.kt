package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppDao
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Subject::class,
        Topic::class,
        Mcq::class,
        TestResult::class,
        UserProgress::class,
        Note::class,
        NoteAccessRequest::class,
        Post::class,
        Comment::class,
        PostLike::class,
        PostShare::class,
        PushNotification::class,
        FinancialTransaction::class,
        AboutUs::class,
        SuccessStory::class,
        PendingMcq::class,
        Announcement::class,
        MdcatCountdown::class,
        RewardOffer::class,
        ClaimedReward::class,
        AppSetting::class,
        StudentFee::class
    ],
    version = 24,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nayarasta_companion_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
