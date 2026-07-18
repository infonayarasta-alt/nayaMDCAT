package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- USER QUERIES ---
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhoneNumber(phoneNumber: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'USER'")
    fun getRegularUserCount(): Flow<Int>

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    // --- SUBJECT QUERIES ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // --- TOPIC QUERIES ---
    @Query("SELECT * FROM topics ORDER BY name ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopicById(id: Int): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    // --- MCQ QUERIES ---
    @Query("SELECT * FROM mcqs")
    fun getAllMcqs(): Flow<List<Mcq>>

    @Query("SELECT * FROM mcqs WHERE subjectId = :subjectId")
    fun getMcqsForSubject(subjectId: Int): Flow<List<Mcq>>

    @Query("SELECT * FROM mcqs WHERE topicId = :topicId")
    fun getMcqsForTopic(topicId: Int): Flow<List<Mcq>>

    @Query("SELECT * FROM mcqs WHERE id = :id")
    suspend fun getMcqById(id: Int): Mcq?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcq(mcq: Mcq): Long

    @Update
    suspend fun updateMcq(mcq: Mcq)

    @Delete
    suspend fun deleteMcq(mcq: Mcq)

    @Query("SELECT COALESCE(MAX(id), 0) FROM mcqs")
    fun getMcqCount(): Flow<Int>

    @Query("SELECT MAX(id) FROM mcqs")
    suspend fun getMaxMcqId(): Int?

    // --- TEST RESULT QUERIES ---
    @Query("SELECT * FROM test_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTestResultsForUser(userId: Int): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllTestResults(): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResult): Long

    @Delete
    suspend fun deleteTestResult(result: TestResult)

    @Query("SELECT COUNT(*) FROM test_results")
    fun getTestCount(): Flow<Int>

    // --- USER PROGRESS QUERIES ---
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getProgressForUser(userId: Int): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress WHERE userId = :userId AND subjectId = :subjectId AND topicId = :topicId LIMIT 1")
    suspend fun getProgressForTopic(userId: Int, subjectId: Int, topicId: Int): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress): Long

    // --- NOTES QUERIES ---
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // --- NOTE ACCESS REQUESTS QUERIES ---
    @Query("SELECT * FROM note_access_requests ORDER BY createdAt DESC")
    fun getAllAccessRequests(): Flow<List<NoteAccessRequest>>

    @Query("SELECT * FROM note_access_requests WHERE userId = :userId")
    fun getAccessRequestsForUser(userId: Int): Flow<List<NoteAccessRequest>>

    @Query("SELECT * FROM note_access_requests WHERE userId = :userId AND noteId = :noteId LIMIT 1")
    suspend fun getAccessRequest(userId: Int, noteId: Int): NoteAccessRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessRequest(request: NoteAccessRequest): Long

    @Update
    suspend fun updateAccessRequest(request: NoteAccessRequest)

    @Delete
    suspend fun deleteAccessRequest(request: NoteAccessRequest)

    // --- STUDYFEED QUERIES ---
    @Query("SELECT * FROM posts ORDER BY createdAt ASC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    // --- COMMENTS ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Delete
    suspend fun deleteComment(comment: Comment)

    // --- LIKES ---
    @Query("SELECT EXISTS(SELECT 1 FROM post_likes WHERE postId = :postId AND userId = :userId)")
    fun hasUserLikedPost(postId: Int, userId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: PostLike)

    @Delete
    suspend fun deleteLike(like: PostLike)

    // --- SHARES ---
    @Query("SELECT EXISTS(SELECT 1 FROM post_shares WHERE postId = :postId AND userId = :userId)")
    fun hasUserSharedPost(postId: Int, userId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: PostShare)

    // --- PUSH NOTIFICATIONS ---
    @Query("SELECT * FROM push_notifications ORDER BY timestamp DESC")
    fun getAllPushNotifications(): Flow<List<PushNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPushNotification(notification: PushNotification): Long

    @Delete
    suspend fun deletePushNotification(notification: PushNotification)

    // --- PENDING MCQS ---
    @Query("SELECT * FROM pending_mcqs ORDER BY createdAt DESC")
    fun getAllPendingMcqs(): Flow<List<PendingMcq>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingMcq(pendingMcq: PendingMcq): Long

    @Delete
    suspend fun deletePendingMcq(pendingMcq: PendingMcq)

    // --- FINANCIAL TRANSACTIONS ---
    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    fun getAllFinancialTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialTransaction(transaction: FinancialTransaction): Long

    @Delete
    suspend fun deleteFinancialTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM financial_transactions")
    suspend fun clearAllFinancialTransactions()

    // --- ABOUT US ---
    @Query("SELECT * FROM about_us WHERE id = 1 LIMIT 1")
    fun getAboutUsFlow(): Flow<AboutUs?>

    @Query("SELECT * FROM about_us WHERE id = 1 LIMIT 1")
    suspend fun getAboutUsDirect(): AboutUs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAboutUs(aboutUs: AboutUs): Long

    // --- SUCCESS STORIES ---
    @Query("SELECT * FROM success_stories ORDER BY timestamp DESC")
    fun getAllSuccessStoriesFlow(): Flow<List<SuccessStory>>

    @Query("SELECT * FROM success_stories ORDER BY timestamp DESC")
    suspend fun getAllSuccessStoriesDirect(): List<SuccessStory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuccessStory(story: SuccessStory): Long

    @Delete
    suspend fun deleteSuccessStory(story: SuccessStory)

    @Query("DELETE FROM success_stories")
    suspend fun clearAllSuccessStories()

    // --- ANNOUNCEMENTS ---
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcements")
    suspend fun clearAllAnnouncements()

    // --- MDCAT COUNTDOWN ---
    @Query("SELECT * FROM mdcat_countdown WHERE id = 1 LIMIT 1")
    fun getMdcatCountdownFlow(): Flow<MdcatCountdown?>

    @Query("SELECT * FROM mdcat_countdown WHERE id = 1 LIMIT 1")
    suspend fun getMdcatCountdownDirect(): MdcatCountdown?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMdcatCountdown(countdown: MdcatCountdown): Long

    // --- REWARD OFFERS ---
    @Query("SELECT * FROM reward_offers ORDER BY createdAt DESC")
    fun getAllRewardOffers(): Flow<List<RewardOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardOffer(offer: RewardOffer): Long

    @Delete
    suspend fun deleteRewardOffer(offer: RewardOffer)

    // --- CLAIMED REWARDS ---
    @Query("SELECT * FROM claimed_rewards")
    suspend fun getAllClaimedRewards(): List<ClaimedReward>

    @Query("SELECT * FROM claimed_rewards WHERE userId = :userId")
    fun getClaimedRewardsForUser(userId: Int): Flow<List<ClaimedReward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaimedReward(claimedReward: ClaimedReward): Long

    @Delete
    suspend fun deleteClaimedReward(claimedReward: ClaimedReward)

    // --- APP SETTINGS ---
    @Query("SELECT * FROM app_settings")
    fun getAllAppSettingsFlow(): Flow<List<AppSetting>>

    @Query("SELECT * FROM app_settings")
    suspend fun getAllAppSettingsList(): List<AppSetting>

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getAppSettingByKey(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSetting(setting: AppSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettingsList(settings: List<AppSetting>)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteAppSettingByKey(key: String)

    // --- STUDENT FEES ---
    @Query("SELECT * FROM student_fees")
    fun getAllStudentFees(): Flow<List<StudentFee>>

    @Query("SELECT * FROM student_fees")
    suspend fun getAllStudentFeesList(): List<StudentFee>

    @Query("SELECT * FROM student_fees WHERE userId = :userId LIMIT 1")
    suspend fun getStudentFeeByUserId(userId: Int): StudentFee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentFee(fee: StudentFee): Long
}
