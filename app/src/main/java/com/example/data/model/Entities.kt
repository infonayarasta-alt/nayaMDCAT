package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String, // Simulating simple secure auth hashes/passwords
    val role: String,         // "USER" or "ADMIN"
    val fullName: String = "",
    val email: String = "",
    val gradeLevel: String = "",
    val studyGoal: String = "",
    val avatarColor: Int = 0xFF005FB0.toInt(),
    val phoneNumber: String = "",
    val studentType: String = "", // "Fresher" or "Repeater"
    val profileImageUri: String? = null, // Path for custom profile images
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val approvedMcqs: Int = 0,
    val rejectedMcqs: Int = 0,
    val subscriptionType: String = "TRIAL", // "TRIAL", "MONTHLY", "YEARLY", "SUSPENDED"
    val subscriptionStartAt: Long = System.currentTimeMillis(),
    val subscriptionExpiresAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["name"], unique = true)]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val iconName: String, // E.g., "computer", "science", "menu", etc.
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class Topic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val name: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "mcqs",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"]), Index(value = ["topicId"])]
)
data class Mcq(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val topicId: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val detailedExplanation: String,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val status: String = "APPROVED",
    val userId: Int = 0,
    @com.squareup.moshi.Json(name = "text_username") val username: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val subjectName: String = "",
    val topicName: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "test_results",
    indices = [Index(value = ["userId"])]
)
data class TestResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val subjectId: Int?,
    val topicId: Int?,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val scorePercentage: Double,
    val isPassed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val testType: String, // "SUBJECT", "TOPIC", "FULL"
    val metadataName: String, // E.g., "Computer Science - Data Structures" or "Algebra"
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "user_progress",
    indices = [Index(value = ["userId", "subjectId", "topicId"], unique = true)]
)
data class UserProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val subjectId: Int,
    val topicId: Int,
    val questionsAttempted: Int,
    val questionsCorrect: Int,
    val lastAttemptedTimestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "notes"
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val subjectId: Int? = null,
    val pdfUri: String? = null,
    val imageUri: String? = null,
    val docUri: String? = null,
    val previewPdfUri: String? = null,
    val previewImageUri: String? = null,
    val previewDocUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "push_notifications"
)
data class PushNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val targetUserId: Int? = null, // null means "All Users"
    val senderName: String = "Admin Desk",
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "note_access_requests",
    indices = [Index(value = ["userId", "noteId"], unique = true)]
)
data class NoteAccessRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val noteId: Int,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "posts"
)
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String,
    val fullName: String,
    val profileImageUri: String? = null,
    val avatarColor: Int = 0xFF005FB0.toInt(),
    val content: String,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val isSolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isClosedByAdmin: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "comments"
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val userId: Int,
    val username: String,
    val fullName: String,
    val profileImageUri: String? = null,
    val avatarColor: Int = 0xFF005FB0.toInt(),
    val content: String,
    val parentCommentId: Int? = null, // for nested comment replies
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "post_likes",
    primaryKeys = ["postId", "userId"]
)
data class PostLike(
    val postId: Int,
    val userId: Int,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "post_shares",
    primaryKeys = ["postId", "userId"]
)
data class PostShare(
    val postId: Int,
    val userId: Int,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "financial_transactions"
)
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "CREDIT" or "EXPENSE"
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "about_us"
)
data class AboutUs(
    @PrimaryKey val id: Int = 1,
    val text: String,
    val images: String, // Comma separated list of uploaded images
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "success_stories"
)
data class SuccessStory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val studentImage: String, // uploaded image path
    val achievement: String,  // E.g., "99.2% Marks, Board Exam"
    val storyText: String,    // A few lines about their journey
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "pending_mcqs"
)
data class PendingMcq(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    @com.squareup.moshi.Json(name = "text_username") val username: String,
    val subjectId: Int,
    val topicId: Int,
    val subjectName: String = "",
    val topicName: String = "",
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val detailedExplanation: String,
    val difficulty: String = "MEDIUM",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING",
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "announcements"
)
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,    // Pickable or input image url/placeholder
    val message: String,
    val targetUserIds: String, // Comma-separated user IDs, e.g. "1,2,3" or "all" for everyone
    val displayDuration: Int, // seconds: e.g. 5, 10
    val isCloseable: Boolean, // true if user can close before duration ends, false if not ("opened")
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "mdcat_countdown"
)
data class MdcatCountdown(
    @PrimaryKey val id: Int = 1,
    @com.squareup.moshi.Json(name = "test_date") val testDate: String, // ISO-8601 string e.g. "2026-09-15T09:00:00Z"
    @com.squareup.moshi.Json(name = "updated_by") val updatedBy: String = "Admin",
    @com.squareup.moshi.Json(name = "quotes") val quotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "reward_offers"
)
data class RewardOffer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requiredMcqs: Int,
    val rewardType: String, // "SUBSCRIPTION", "NOTES", "CASH"
    val notesIds: String = "", // Comma-separated ids of Notes
    val cashAmount: Double = 0.0,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "claimed_rewards"
)
data class ClaimedReward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val offerId: Int,
    val claimedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "app_settings"
)
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "student_fees"
)
data class StudentFee(
    @PrimaryKey val userId: Int,
    val monthlyFee: Double = 3000.0,
    val outstandingBalance: Double = 0.0,
    val feeDueDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val feeStatus: String = "UNPAID", // "PAID", "UNPAID", "PENDING"
    val remarks: String = "",
    val trialDays: Int = 7,
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable
