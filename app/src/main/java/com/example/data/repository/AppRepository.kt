package com.example.data.repository

import com.example.BuildConfig
import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock

enum class SyncType {
    ESSENTIAL,
    FULL,
    REALTIME
}

class AppRepository(private val appDao: AppDao) {

    private val syncMutex = kotlinx.coroutines.sync.Mutex()

    // Users
    fun getAllUsers(): Flow<List<User>> = appDao.getAllUsers()
    fun getRegularUserCount(): Flow<Int> = appDao.getRegularUserCount()
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remoteList = com.example.data.network.SupabaseClient.api.getUsersFiltered("ilike.$username")
                if (remoteList.isNotEmpty()) {
                    val remoteUser = remoteList.first()
                    val local = appDao.getUserByUsername(username)
                    val userToInsert = if (local != null) remoteUser.copy(id = local.id) else remoteUser
                    appDao.insertUser(userToInsert)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        appDao.getUserByUsername(username)
    }
    suspend fun getUserByPhoneNumber(phoneNumber: String): User? = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            val columns = listOf("phone_number", "phonenumber", "phoneNumber")
            var remoteUser: User? = null
            for (col in columns) {
                try {
                    val remoteList = com.example.data.network.SupabaseClient.api.getUsersByQueryMap(mapOf(col to "eq.$phoneNumber"))
                    if (remoteList.isNotEmpty()) {
                        remoteUser = remoteList.first()
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (remoteUser != null) {
                try {
                    val local = appDao.getUserByPhoneNumber(phoneNumber)
                    val userToInsert = if (local != null) remoteUser.copy(id = local.id) else remoteUser
                    appDao.insertUser(userToInsert)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        appDao.getUserByPhoneNumber(phoneNumber)
    }
    suspend fun getUserById(id: Int): User? = withContext(Dispatchers.IO) {
        appDao.getUserById(id)
    }
    suspend fun getAllUsersList(): List<User> = withContext(Dispatchers.IO) {
        appDao.getAllUsersList()
    }
    // Captures the most recent remote-write error for user sync so the UI can
    // tell the user *why* a registration did not reach Supabase, instead of
    // silently reporting success. Null means the last insert synced cleanly.
    @Volatile
    var lastUserSyncError: String? = null

    // Turns a network/Retrofit failure into a short, human-readable reason
    // (including the HTTP status and PostgREST error body when present).
    private fun describeRemoteError(e: Throwable): String {
        return when (e) {
            is retrofit2.HttpException -> {
                val body = try {
                    e.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                "HTTP ${e.code()}: ${(body?.takeIf { it.isNotBlank() } ?: e.message())?.take(300)}"
            }
            is java.net.UnknownHostException -> "Cannot reach server (no network / DNS)."
            is java.net.SocketTimeoutException -> "Server timed out."
            else -> e.message ?: e.toString()
        }
    }

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertUser(user)
        try {
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                listOf(user),
                User::class.java,
                alwaysDiscardId = true
            )
            val remoteResult = com.example.data.network.SupabaseClient.api.insertUsers(users = remotePayload)
            if (remoteResult.isNotEmpty()) {
                val realRemoteUser = remoteResult[0]
                val tempUser = user.copy(id = id.toInt())
                appDao.deleteUser(tempUser)
                appDao.insertUser(realRemoteUser)
                lastUserSyncError = null
                return@withContext realRemoteUser.id.toLong()
            }
            // Request succeeded but no row came back — usually a permission/return
            // configuration issue on the server side.
            lastUserSyncError = "Server returned no row for the new user (check table INSERT permissions)."
        } catch (e: Exception) {
            e.printStackTrace()
            lastUserSyncError = describeRemoteError(e)
            android.util.Log.e("insertUser", "Failed to insert user on remote Supabase: ${e.message}", e)
        }
        id
    }
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        appDao.updateUser(user)
        try {
            com.example.data.network.SupabaseClient.api.updateUser("eq.${user.id}", user)
            lastUserSyncError = null
        } catch (e: Exception) {
            e.printStackTrace()
            lastUserSyncError = describeRemoteError(e)
            android.util.Log.e("updateUser", "Failed to update user on remote Supabase: ${e.message}", e)
        }
    }
    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
        appDao.deleteUser(user)
        try {
            com.example.data.network.SupabaseClient.api.deleteUser("eq.${user.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Subjects
    val allSubjects: Flow<List<Subject>> = appDao.getAllSubjects()
    suspend fun getSubjectById(id: Int): Subject? = withContext(Dispatchers.IO) {
        appDao.getSubjectById(id)
    }
    suspend fun insertSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        try {
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(subject), Subject::class.java, alwaysDiscardId = true)
            val remoteResult = com.example.data.network.SupabaseClient.api.insertSubjects(subjects = remotePayload)
            if (remoteResult.isNotEmpty()) {
                val realRemoteSubject = remoteResult[0]
                appDao.insertSubject(realRemoteSubject)
                return@withContext realRemoteSubject.id.toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val id = appDao.insertSubject(subject)
        id
    }
    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        appDao.updateSubject(subject)
        try {
            com.example.data.network.SupabaseClient.api.updateSubject("eq.${subject.id}", subject)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteSubject(subject: Subject) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.deleteSubject("eq.${subject.id}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        appDao.deleteSubject(subject)
    }

    // Topics
    val allTopics: Flow<List<Topic>> = appDao.getAllTopics()
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>> = appDao.getTopicsForSubject(subjectId)
    suspend fun getTopicById(id: Int): Topic? = withContext(Dispatchers.IO) {
        appDao.getTopicById(id)
    }
    suspend fun insertTopic(topic: Topic): Long = withContext(Dispatchers.IO) {
        try {
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(topic), Topic::class.java, alwaysDiscardId = true)
            val remoteResult = com.example.data.network.SupabaseClient.api.insertTopics(topics = remotePayload)
            if (remoteResult.isNotEmpty()) {
                val realRemoteTopic = remoteResult[0]
                appDao.insertTopic(realRemoteTopic)
                return@withContext realRemoteTopic.id.toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val id = appDao.insertTopic(topic)
        id
    }
    suspend fun updateTopic(topic: Topic) = withContext(Dispatchers.IO) {
        appDao.updateTopic(topic)
        try {
            com.example.data.network.SupabaseClient.api.updateTopic("eq.${topic.id}", topic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteTopic(topic: Topic) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.deleteTopic("eq.${topic.id}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        appDao.deleteTopic(topic)
    }

    // MCQs
    val allMcqs: Flow<List<Mcq>> = appDao.getAllMcqs()
    fun getMcqsForSubject(subjectId: Int): Flow<List<Mcq>> = appDao.getMcqsForSubject(subjectId)
    fun getMcqsForTopic(topicId: Int): Flow<List<Mcq>> = appDao.getMcqsForTopic(topicId)
    suspend fun getMcqById(id: Int): Mcq? = withContext(Dispatchers.IO) {
        appDao.getMcqById(id)
    }
    suspend fun getMaxMcqId(): Int? = withContext(Dispatchers.IO) {
        appDao.getMaxMcqId()
    }
    suspend fun insertMcq(mcq: Mcq): Long = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(mcq), Mcq::class.java, alwaysDiscardId = true)
                val remoteResult = com.example.data.network.SupabaseClient.api.insertMcqs(mcqs = remotePayload)
                if (remoteResult.isNotEmpty()) {
                    val realRemoteMcq = remoteResult[0]
                    appDao.insertMcq(realRemoteMcq)
                    return@withContext realRemoteMcq.id.toLong()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to insert MCQ into database: ${e.message}", e)
            }
        }
        val id = appDao.insertMcq(mcq)
        id
    }
    suspend fun insertMcqs(mcqs: List<Mcq>) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(mcqs, Mcq::class.java, alwaysDiscardId = true)
                val remoteResult = com.example.data.network.SupabaseClient.api.insertMcqs(mcqs = remotePayload)
                if (remoteResult.isNotEmpty()) {
                    for (realRemoteMcq in remoteResult) {
                        appDao.insertMcq(realRemoteMcq)
                    }
                    return@withContext
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to insert MCQs into database: ${e.message}", e)
            }
        }
        val mcqsWithIds = mcqs.map { mcq ->
            val id = appDao.insertMcq(mcq)
            if (mcq.id == 0) mcq.copy(id = id.toInt()) else mcq
        }
        if (isCustomSupabase) {
            try {
                val fallbackPayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(mcqsWithIds, Mcq::class.java, alwaysDiscardId = true)
                com.example.data.network.SupabaseClient.api.insertMcqs(mcqs = fallbackPayload)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun updateMcq(mcq: Mcq) = withContext(Dispatchers.IO) {
        appDao.insertMcq(mcq) // Ensure it is inserted/updated locally
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.updateMcq("eq.${mcq.id}", mcq)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to update MCQ in database: ${e.message}", e)
            }
        }
    }
    suspend fun deleteMcq(mcq: Mcq) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.deleteMcq("eq.${mcq.id}")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to delete MCQ from database: ${e.message}", e)
            }
        }
        appDao.deleteMcq(mcq)
    }
    val mcqCount: Flow<Int> = appDao.getMcqCount()

    // Test Results
    fun getTestResultsForUser(userId: Int): Flow<List<TestResult>> = appDao.getTestResultsForUser(userId)
    val allTestResults: Flow<List<TestResult>> = appDao.getAllTestResults()
    suspend fun insertTestResult(result: TestResult): Long = withContext(Dispatchers.IO) {
        try {
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(result), TestResult::class.java, alwaysDiscardId = true)
            val remoteResult = com.example.data.network.SupabaseClient.api.insertTestResults(results = remotePayload)
            if (remoteResult.isNotEmpty()) {
                val realRemoteResult = remoteResult[0]
                appDao.insertTestResult(realRemoteResult)
                return@withContext realRemoteResult.id.toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val id = appDao.insertTestResult(result)
        id
    }
    val testCount: Flow<Int> = appDao.getTestCount()

    // User Progress
    fun getProgressForUser(userId: Int): Flow<List<UserProgress>> = appDao.getProgressForUser(userId)
    suspend fun getProgressForTopic(userId: Int, subjectId: Int, topicId: Int): UserProgress? = withContext(Dispatchers.IO) {
        appDao.getProgressForTopic(userId, subjectId, topicId)
    }
    suspend fun updateProgress(userId: Int, subjectId: Int, topicId: Int, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val existing = appDao.getProgressForTopic(userId, subjectId, topicId)
        val updated = if (existing != null) {
            existing.copy(
                questionsAttempted = existing.questionsAttempted + 1,
                questionsCorrect = existing.questionsCorrect + (if (isCorrect) 1 else 0),
                lastAttemptedTimestamp = System.currentTimeMillis()
            )
        } else {
            UserProgress(
                userId = userId,
                subjectId = subjectId,
                topicId = topicId,
                questionsAttempted = 1,
                questionsCorrect = if (isCorrect) 1 else 0,
                lastAttemptedTimestamp = System.currentTimeMillis()
            )
        }
        val id = appDao.insertOrUpdateProgress(updated)
        try {
            val progressWithId = if (updated.id == 0) updated.copy(id = id.toInt()) else updated
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                listOf(progressWithId), 
                UserProgress::class.java,
                alwaysDiscardId = true
            )
            val remoteResult = com.example.data.network.SupabaseClient.api.insertUserProgress(
                onConflict = "userId,subjectId,topicId",
                progress = remotePayload
            )
            if (remoteResult.isNotEmpty()) {
                val realRemoteProgress = remoteResult[0]
                appDao.insertOrUpdateProgress(realRemoteProgress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                    listOf(updated), 
                    UserProgress::class.java,
                    alwaysDiscardId = true
                )
                com.example.data.network.SupabaseClient.api.insertUserProgress(
                    onConflict = null,
                    progress = remotePayload
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    suspend fun updateProgressBulk(userId: Int, attempts: List<Pair<com.example.data.model.Mcq, Boolean>>) = withContext(Dispatchers.IO) {
        val groups = attempts.groupBy { it.first.subjectId to it.first.topicId }
        val updatedProgressList = mutableListOf<UserProgress>()

        for ((key, attemptList) in groups) {
            val (subjectId, topicId) = key
            val totalAttempted = attemptList.size
            val totalCorrect = attemptList.count { it.second }

            val existing = appDao.getProgressForTopic(userId, subjectId, topicId)
            val updated = if (existing != null) {
                existing.copy(
                    questionsAttempted = existing.questionsAttempted + totalAttempted,
                    questionsCorrect = existing.questionsCorrect + totalCorrect,
                    lastAttemptedTimestamp = System.currentTimeMillis()
                )
            } else {
                UserProgress(
                    userId = userId,
                    subjectId = subjectId,
                    topicId = topicId,
                    questionsAttempted = totalAttempted,
                    questionsCorrect = totalCorrect,
                    lastAttemptedTimestamp = System.currentTimeMillis()
                )
            }
            val id = appDao.insertOrUpdateProgress(updated)
            val progressWithId = if (updated.id == 0) updated.copy(id = id.toInt()) else updated
            updatedProgressList.add(progressWithId)
        }

        if (updatedProgressList.isNotEmpty()) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                    updatedProgressList, 
                    UserProgress::class.java,
                    alwaysDiscardId = true
                )
                val remoteResult = com.example.data.network.SupabaseClient.api.insertUserProgress(
                    onConflict = "userId,subjectId,topicId",
                    progress = remotePayload
                )
                if (remoteResult.isNotEmpty()) {
                    for (realRemoteProgress in remoteResult) {
                        appDao.insertOrUpdateProgress(realRemoteProgress)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                        updatedProgressList, 
                        UserProgress::class.java,
                        alwaysDiscardId = true
                    )
                    com.example.data.network.SupabaseClient.api.insertUserProgress(
                        onConflict = null,
                        progress = remotePayload
                    )
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    }

    // --- NOTES SERVICES ---
    val allNotes: Flow<List<Note>> = appDao.getAllNotes()
    suspend fun getNoteById(id: Int): Note? = withContext(Dispatchers.IO) { appDao.getNoteById(id) }
    suspend fun insertNote(note: Note): Long = withContext(Dispatchers.IO) {
        try {
            val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(note), Note::class.java, alwaysDiscardId = true)
            val remoteResult = com.example.data.network.SupabaseClient.api.insertNotes(notes = remotePayload)
            if (remoteResult.isNotEmpty()) {
                val realRemoteNote = remoteResult[0]
                appDao.insertNote(realRemoteNote)
                return@withContext realRemoteNote.id.toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val id = appDao.insertNote(note)
        id
    }
    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        appDao.updateNote(note)
        try {
            com.example.data.network.SupabaseClient.api.updateNote("eq.${note.id}", note)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        appDao.deleteNote(note)
        try {
            com.example.data.network.SupabaseClient.api.deleteNote("eq.${note.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- NOTE ACCESS REQUESTS ---
    val allAccessRequests: Flow<List<NoteAccessRequest>> = appDao.getAllAccessRequests()
    fun getAccessRequestsForUser(userId: Int): Flow<List<NoteAccessRequest>> = appDao.getAccessRequestsForUser(userId)
    suspend fun getAccessRequest(userId: Int, noteId: Int): NoteAccessRequest? = withContext(Dispatchers.IO) {
        appDao.getAccessRequest(userId, noteId)
    }
    suspend fun insertAccessRequest(request: NoteAccessRequest): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertAccessRequest(request)
        try {
            val reqWithId = if (request.id == 0) request.copy(id = id.toInt()) else request
            com.example.data.network.SupabaseClient.api.insertNoteAccessRequests(listOf(reqWithId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        id
    }
    suspend fun updateAccessRequest(request: NoteAccessRequest) = withContext(Dispatchers.IO) {
        appDao.updateAccessRequest(request)
        try {
            com.example.data.network.SupabaseClient.api.updateNoteAccessRequest("eq.${request.id}", request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteAccessRequest(request: NoteAccessRequest) = withContext(Dispatchers.IO) {
        appDao.deleteAccessRequest(request)
        try {
            com.example.data.network.SupabaseClient.api.deleteNoteAccessRequest("eq.${request.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- POST SERVICES ---
    val allPosts: Flow<List<Post>> = appDao.getAllPosts()
    suspend fun getPostById(id: Int): Post? = withContext(Dispatchers.IO) { appDao.getPostById(id) }
    suspend fun insertPost(post: Post): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertPost(post)
        try {
            val postWithId = if (post.id == 0) post.copy(id = id.toInt()) else post
            com.example.data.network.SupabaseClient.api.insertPosts(listOf(postWithId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        id
    }
    suspend fun updatePost(post: Post) = withContext(Dispatchers.IO) {
        appDao.updatePost(post)
        try {
            com.example.data.network.SupabaseClient.api.updatePost("eq.${post.id}", post)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        appDao.deletePost(post)
        try {
            com.example.data.network.SupabaseClient.api.deletePost("eq.${post.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- COMMENTS SERVICES ---
    fun getCommentsForPost(postId: Int): Flow<List<Comment>> = appDao.getCommentsForPost(postId)
    suspend fun insertComment(comment: Comment): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertComment(comment)
        try {
            val commWithId = if (comment.id == 0) comment.copy(id = id.toInt()) else comment
            com.example.data.network.SupabaseClient.api.insertComments(listOf(commWithId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        id
    }
    suspend fun deleteComment(comment: Comment) = withContext(Dispatchers.IO) {
        appDao.deleteComment(comment)
        try {
            com.example.data.network.SupabaseClient.api.deleteComment("eq.${comment.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- LIKES & SHARES SERVICES ---
    fun hasUserLikedPost(postId: Int, userId: Int): Flow<Boolean> = appDao.hasUserLikedPost(postId, userId)
    suspend fun togglePostLike(postId: Int, userId: Int) = withContext(Dispatchers.IO) {
        val liked = appDao.hasUserLikedPost(postId, userId).first()
        val post = appDao.getPostById(postId)
        if (post != null) {
            if (liked) {
                appDao.deleteLike(PostLike(postId, userId))
                val updated = post.copy(likeCount = maxOf(0, post.likeCount - 1))
                appDao.updatePost(updated)
                try {
                    com.example.data.network.SupabaseClient.api.deletePostLike("eq.$postId", "eq.$userId")
                    com.example.data.network.SupabaseClient.api.updatePost("eq.$postId", updated)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                appDao.insertLike(PostLike(postId, userId))
                val updated = post.copy(likeCount = post.likeCount + 1)
                appDao.updatePost(updated)
                try {
                    com.example.data.network.SupabaseClient.api.insertPostLike(PostLike(postId, userId))
                    com.example.data.network.SupabaseClient.api.updatePost("eq.$postId", updated)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    suspend fun recordPostShare(postId: Int, userId: Int) = withContext(Dispatchers.IO) {
        val shared = appDao.hasUserSharedPost(postId, userId).first()
        if (!shared) {
            appDao.insertShare(PostShare(postId, userId))
            val post = appDao.getPostById(postId)
            if (post != null) {
                val updated = post.copy(shareCount = post.shareCount + 1)
                appDao.updatePost(updated)
                try {
                    com.example.data.network.SupabaseClient.api.insertPostShare(PostShare(postId, userId))
                    com.example.data.network.SupabaseClient.api.updatePost("eq.$postId", updated)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- PUSH NOTIFICATIONS ---
    val allPushNotifications: Flow<List<PushNotification>> = appDao.getAllPushNotifications()
    suspend fun insertPushNotification(notification: PushNotification): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertPushNotification(notification)
        try {
            val pushWithId = if (notification.id == 0) notification.copy(id = id.toInt()) else notification
            com.example.data.network.SupabaseClient.api.insertPushNotifications(listOf(pushWithId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        id
    }
    suspend fun deletePushNotification(notification: PushNotification) = withContext(Dispatchers.IO) {
        appDao.deletePushNotification(notification)
        try {
            com.example.data.network.SupabaseClient.api.deletePushNotification("eq.${notification.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- PENDING MCQS ---
    val allPendingMcqs: Flow<List<PendingMcq>> = appDao.getAllPendingMcqs()
    suspend fun insertPendingMcq(pendingMcq: PendingMcq): Long = withContext(Dispatchers.IO) {
        if (pendingMcq.username.trim().lowercase() == "user1") {
            return@withContext 0L
        }
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(pendingMcq), PendingMcq::class.java, alwaysDiscardId = true)
                val remoteResult = com.example.data.network.SupabaseClient.api.insertPendingMcqs(remotePayload)
                if (remoteResult.isNotEmpty()) {
                    val realRemotePending = remoteResult[0]
                    appDao.insertPendingMcq(realRemotePending)
                    return@withContext realRemotePending.id.toLong()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to insert user pending MCQ: ${e.message}", e)
            }
        }
        val id = appDao.insertPendingMcq(pendingMcq)
        id
    }
    suspend fun deletePendingMcq(pendingMcq: PendingMcq) = withContext(Dispatchers.IO) {
        appDao.deletePendingMcq(pendingMcq)
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.deletePendingMcq("eq.${pendingMcq.id}")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Database Error: Status ${e.code()}. $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to delete pending MCQ from database: ${e.message}", e)
            }
        }
    }
    suspend fun deletePendingMcqLocally(pendingMcq: PendingMcq) = withContext(Dispatchers.IO) {
        appDao.deletePendingMcq(pendingMcq)
    }

    // --- ANNOUNCEMENTS ---
    val allAnnouncements: Flow<List<Announcement>> = appDao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement): Long = withContext(Dispatchers.IO) {
        val id = appDao.insertAnnouncement(announcement)
        try {
            val announcementWithId = if (announcement.id == 0) announcement.copy(id = id.toInt()) else announcement
            com.example.data.network.SupabaseClient.api.insertAnnouncements(listOf(announcementWithId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        id
    }
    suspend fun deleteAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        appDao.deleteAnnouncement(announcement)
        try {
            com.example.data.network.SupabaseClient.api.deleteAnnouncement("eq.${announcement.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Seed preset accounts and initial sample data if SQLite is clean.
    suspend fun preloadInitialData() = withContext(Dispatchers.IO) {
        val checkUsers = appDao.getAllUsersList()
        val hasAdmin = checkUsers.any { it.username == "admin" }
        val hasUser = checkUsers.any { it.username == "user1" }

        if (!hasAdmin) {
            // Seed Users
            // Preset Admin: admin / admin123
            insertUser(
                User(
                    username = "admin",
                    passwordHash = "admin123", // Using plain-text for this easy-to-use prototype
                    fullName = "Administrator",
                    role = "ADMIN"
                )
            )
        }
        if (!hasUser) {
            // Preset User: user1 / user123
            insertUser(
                User(
                    username = "user1",
                    passwordHash = "user123",
                    fullName = "Student User",
                    role = "USER",
                    phoneNumber = "03001234567",
                    studentType = "Fresher"
                )
            )
        }

            // Seed default reward offers
            appDao.insertRewardOffer(
                RewardOffer(
                    requiredMcqs = 100,
                    rewardType = "SUBSCRIPTION",
                    description = "1 Month Free Subscription"
                )
            )
            appDao.insertRewardOffer(
                RewardOffer(
                    requiredMcqs = 50,
                    rewardType = "CASH",
                    cashAmount = 500.0,
                    description = "Rs. 500 Cash Reward"
                )
            )
        }

    // Financial Transactions
    fun getAllFinancialTransactions(): Flow<List<FinancialTransaction>> = appDao.getAllFinancialTransactions()
    suspend fun insertFinancialTransaction(transaction: FinancialTransaction): Long = withContext(Dispatchers.IO) {
        appDao.insertFinancialTransaction(transaction)
    }
    suspend fun deleteFinancialTransaction(transaction: FinancialTransaction) = withContext(Dispatchers.IO) {
        appDao.deleteFinancialTransaction(transaction)
    }
    suspend fun clearAllFinancialTransactions() = withContext(Dispatchers.IO) {
        appDao.clearAllFinancialTransactions()
    }

    // Student Fees
    fun getAllStudentFees(): Flow<List<StudentFee>> = appDao.getAllStudentFees()
    suspend fun getStudentFeeByUserId(userId: Int): StudentFee? = withContext(Dispatchers.IO) {
        appDao.getStudentFeeByUserId(userId)
    }
    suspend fun insertStudentFee(fee: StudentFee): Long = withContext(Dispatchers.IO) {
        val result = appDao.insertStudentFee(fee)
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.insertStudentFees(fees = listOf(fee))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        result
    }

    // Reward Offers & Claims
    val allRewardOffers: Flow<List<RewardOffer>> = appDao.getAllRewardOffers()
    suspend fun insertRewardOffer(offer: RewardOffer): Long = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                    listOf(offer),
                    RewardOffer::class.java,
                    alwaysDiscardId = true
                )
                val remoteResult = com.example.data.network.SupabaseClient.api.insertRewardOffers(body = remotePayload)
                if (remoteResult.isNotEmpty()) {
                    val realRemote = remoteResult[0]
                    appDao.insertRewardOffer(realRemote)
                    return@withContext realRemote.id.toLong()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Supabase Error (Status ${e.code()}): $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to upload reward offer: ${e.message}", e)
            }
        }
        val id = appDao.insertRewardOffer(offer)
        id
    }
    suspend fun deleteRewardOffer(offer: RewardOffer) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.deleteRewardOffer("eq.${offer.id}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        appDao.deleteRewardOffer(offer)
    }
    fun getClaimedRewardsForUser(userId: Int): Flow<List<ClaimedReward>> = appDao.getClaimedRewardsForUser(userId)
    suspend fun insertClaimedReward(claimedReward: ClaimedReward): Long = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                    listOf(claimedReward),
                    ClaimedReward::class.java,
                    alwaysDiscardId = true
                )
                val remoteResult = com.example.data.network.SupabaseClient.api.insertClaimedRewards(body = remotePayload)
                if (remoteResult.isNotEmpty()) {
                    val realRemote = remoteResult[0]
                    appDao.insertClaimedReward(realRemote)
                    return@withContext realRemote.id.toLong()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                throw Exception("Supabase Error (Status ${e.code()}): $errorBody", e)
            } catch (e: Exception) {
                throw Exception("Failed to register claim: ${e.message}", e)
            }
        }
        val id = appDao.insertClaimedReward(claimedReward)
        id
    }

    suspend fun getLastSyncTime(tableName: String): Long {
        return try {
            val setting = appDao.getAppSettingByKey("last_sync_time_$tableName")
            setting?.value?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun updateLastSyncTime(tableName: String, timestamp: Long) {
        try {
            appDao.insertAppSetting(AppSetting(key = "last_sync_time_$tableName", value = timestamp.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun pullAndSyncWithSupabase(syncType: SyncType = SyncType.FULL, forceWait: Boolean = false) = withContext(Dispatchers.IO) {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (!isCustomSupabase) return@withContext

        if (forceWait) {
            syncMutex.withLock {
                performSyncInternal(syncType)
            }
        } else {
            if (!syncMutex.tryLock()) {
                return@withContext
            }
            try {
                performSyncInternal(syncType)
            } finally {
                syncMutex.unlock()
            }
        }
    }

    private suspend fun performSyncInternal(syncType: SyncType) {
        try {
            val syncStartTime = System.currentTimeMillis()
            kotlinx.coroutines.supervisorScope {
                // 1. Users job (Always run for ESSENTIAL and FULL, skip for REALTIME)
                val jobUsers = if (syncType == SyncType.ESSENTIAL || syncType == SyncType.FULL) {
                    async {
                        try {
                            android.util.Log.d("SyncUsers", "Fetching remote users from Supabase...")
                            val lastSync = getLastSyncTime("users")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteUsers = com.example.data.network.SupabaseClient.api.getUsers(updatedAtFilter = filter)
                            android.util.Log.d("SyncUsers", "Fetched ${remoteUsers.size} users from Supabase.")
                            val localUsers = appDao.getAllUsersList()
                            val localMap = localUsers.associateBy { it.id }
                            
                            for (item in remoteUsers) {
                                try {
                                    val localUser = localMap[item.id]
                                    if (localUser == null || localUser != item) {
                                        val isUsernameTakenByOther = localUsers.any { 
                                            it.username.trim().lowercase() == item.username.trim().lowercase() && it.id != item.id 
                                        }
                                        if (isUsernameTakenByOther) {
                                            android.util.Log.w("SyncUsers", "Username conflict detected for '${item.username}'. Skipping...")
                                            continue
                                        }
                                        android.util.Log.d("SyncUsers", "Inserting/updating user locally: ID=${item.id}, Username=${item.username}")
                                        appDao.insertUser(item)
                                    }
                                } catch (innerE: Exception) {
                                    innerE.printStackTrace()
                                }
                            }
                            
                            // Push up any local unsynced users (e.g. registered offline)
                            val remoteUsernames = remoteUsers.map { it.username.trim().lowercase() }.toSet()
                            val unsyncedLocalUsers = localUsers.filter { !remoteUsernames.contains(it.username.trim().lowercase()) }
                            for (unsynced in unsyncedLocalUsers) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                                        listOf(unsynced),
                                        User::class.java,
                                        alwaysDiscardId = true
                                    )
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertUsers(users = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemoteUser = remoteResult[0]
                                        appDao.deleteUser(unsynced)
                                        appDao.insertUser(realRemoteUser)
                                        android.util.Log.d("SyncUsers", "Successfully synced offline registered user: '${unsynced.username}' to Supabase.")
                                    }
                                } catch (uploadEx: Exception) {
                                    android.util.Log.e("SyncUsers", "Failed to sync offline user '${unsynced.username}': ${uploadEx.message}")
                                }
                            }
                            updateLastSyncTime("users", syncStartTime)
                        } catch (e: Exception) {
                            android.util.Log.e("SyncUsers", "User sync completely failed: ${e.message}", e)
                        }
                    }
                } else null

                // 2. Student Fees job (Run for ESSENTIAL and FULL, skip for REALTIME)
                val jobStudentFees = if (syncType == SyncType.ESSENTIAL || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("student_fees")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteFees = com.example.data.network.SupabaseClient.api.getStudentFees(updatedAtFilter = filter)
                            val localFees = appDao.getAllStudentFeesList()
                            for (item in remoteFees) {
                                appDao.insertStudentFee(item)
                            }
                            // Push up any local unsynced fees
                            val remoteUserIds = remoteFees.map { it.userId }.toSet()
                            val unsyncedFees = localFees.filter { !remoteUserIds.contains(it.userId) }
                            if (unsyncedFees.isNotEmpty()) {
                                com.example.data.network.SupabaseClient.api.insertStudentFees(fees = unsyncedFees)
                            }
                            updateLastSyncTime("student_fees", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 3. AppSettings job (Run for ESSENTIAL and FULL, skip for REALTIME)
                val jobAppSettings = if (syncType == SyncType.ESSENTIAL || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("app_settings")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteSettings = com.example.data.network.SupabaseClient.api.getAppSettings(updatedAtFilter = filter)
                            if (remoteSettings.isNotEmpty()) {
                                appDao.insertAppSettingsList(remoteSettings)
                            }
                            updateLastSyncTime("app_settings", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 4. Subjects & Topics sequential sync (Run for ESSENTIAL and FULL, skip for REALTIME)
                val jobSubjectsAndTopics = if (syncType == SyncType.ESSENTIAL || syncType == SyncType.FULL) {
                    async {
                        // Sync Subjects
                        try {
                            val lastSyncSubjects = getLastSyncTime("subjects")
                            val subjectFilter = if (lastSyncSubjects > 0) "gt.$lastSyncSubjects" else null
                            val remoteSubjects = com.example.data.network.SupabaseClient.api.getSubjects(updatedAtFilter = subjectFilter)
                            val localSubjects = appDao.getAllSubjects().first()
                            val remoteIds = remoteSubjects.map { it.id }.toSet()
                            val remoteSubjectNames = remoteSubjects.map { it.name.trim().lowercase() }.toSet()
                            
                            val unsyncedSubjects = localSubjects.filter { 
                                !remoteIds.contains(it.id) && !remoteSubjectNames.contains(it.name.trim().lowercase())
                            }
                            for (unsynced in unsyncedSubjects) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(unsynced), Subject::class.java, alwaysDiscardId = true)
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertSubjects(subjects = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemoteSubject = remoteResult[0]
                                        appDao.deleteSubject(unsynced)
                                        appDao.insertSubject(realRemoteSubject)
                                        val localTopics = appDao.getAllTopics().first().filter { it.subjectId == unsynced.id }
                                        for (topic in localTopics) {
                                            appDao.updateTopic(topic.copy(subjectId = realRemoteSubject.id))
                                        }
                                        val localMcqs = appDao.getAllMcqs().first().filter { it.subjectId == unsynced.id }
                                        for (mcq in localMcqs) {
                                            appDao.updateMcq(mcq.copy(subjectId = realRemoteSubject.id))
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            val finalRemoteSubjects = com.example.data.network.SupabaseClient.api.getSubjects()
                            val finalLocalSubjects = appDao.getAllSubjects().first()
                            val localMap = finalLocalSubjects.associateBy { it.id }
                            for (item in finalRemoteSubjects) {
                                val localItem = localMap[item.id]
                                if (localItem == null) {
                                    val matchingLocal = finalLocalSubjects.find { it.name.trim().lowercase() == item.name.trim().lowercase() }
                                    if (matchingLocal != null) {
                                        if (matchingLocal.id != item.id) {
                                            appDao.deleteSubject(matchingLocal)
                                            appDao.insertSubject(item)
                                            val localTopics = appDao.getAllTopics().first().filter { it.subjectId == matchingLocal.id }
                                            for (topic in localTopics) {
                                                appDao.updateTopic(topic.copy(subjectId = item.id))
                                            }
                                            val localMcqs = appDao.getAllMcqs().first().filter { it.subjectId == matchingLocal.id }
                                            for (mcq in localMcqs) {
                                                appDao.updateMcq(mcq.copy(subjectId = item.id))
                                            }
                                        }
                                    } else {
                                        appDao.insertSubject(item)
                                    }
                                } else if (localItem.name != item.name || localItem.description != item.description) {
                                    appDao.insertSubject(item)
                                }
                            }
                            updateLastSyncTime("subjects", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Sync Topics
                        try {
                            val lastSyncTopics = getLastSyncTime("topics")
                            val topicFilter = if (lastSyncTopics > 0) "gt.$lastSyncTopics" else null
                            val remoteTopics = com.example.data.network.SupabaseClient.api.getTopics(updatedAtFilter = topicFilter)
                            val localTopics = appDao.getAllTopics().first()
                            val remoteIds = remoteTopics.map { it.id }.toSet()
                            val remoteTopicNames = remoteTopics.map { it.name.trim().lowercase() }.toSet()
                            
                            val unsyncedTopics = localTopics.filter { 
                                !remoteIds.contains(it.id) && !remoteTopicNames.contains(it.name.trim().lowercase())
                            }
                            for (unsynced in unsyncedTopics) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(unsynced), Topic::class.java, alwaysDiscardId = true)
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertTopics(topics = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemoteTopic = remoteResult[0]
                                        appDao.deleteTopic(unsynced)
                                        appDao.insertTopic(realRemoteTopic)
                                        val localMcqs = appDao.getAllMcqs().first().filter { it.topicId == unsynced.id }
                                        for (mcq in localMcqs) {
                                            appDao.updateMcq(mcq.copy(topicId = realRemoteTopic.id))
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            val finalRemoteTopics = com.example.data.network.SupabaseClient.api.getTopics()
                            val finalLocalTopics = appDao.getAllTopics().first()
                            val localMap = finalLocalTopics.associateBy { it.id }
                            for (item in finalRemoteTopics) {
                                val localItem = localMap[item.id]
                                if (localItem == null) {
                                    val matchingLocal = finalLocalTopics.find { it.name.trim().lowercase() == item.name.trim().lowercase() }
                                    if (matchingLocal != null) {
                                        if (matchingLocal.id != item.id) {
                                            appDao.deleteTopic(matchingLocal)
                                            appDao.insertTopic(item)
                                            val localMcqs = appDao.getAllMcqs().first().filter { it.topicId == matchingLocal.id }
                                            for (mcq in localMcqs) {
                                                appDao.updateMcq(mcq.copy(item.id))
                                            }
                                        }
                                    } else {
                                        appDao.insertTopic(item)
                                    }
                                } else if (localItem.subjectId != item.subjectId || localItem.name != item.name || localItem.description != item.description) {
                                    appDao.insertTopic(item)
                                }
                            }
                            updateLastSyncTime("topics", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 5. Heavy MCQs (Only FULL sync)
                val jobMcqs = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("mcqs")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            
                            val remoteMcqs = mutableListOf<Mcq>()
                            var lastMcqId = 0
                            while (true) {
                                try {
                                    val page = com.example.data.network.SupabaseClient.api.getMcqsPage(limit = 1000, idFilter = "gt.$lastMcqId", updatedAtFilter = filter)
                                    remoteMcqs.addAll(page)
                                    if (page.isEmpty()) break
                                    lastMcqId = page.maxOfOrNull { it.id } ?: break
                                    if (page.size < 1000) break
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    if (remoteMcqs.isEmpty()) throw e else break
                                }
                            }
                            
                            // Support syncing up any local unsynced MCQs
                            val localMcqs = appDao.getAllMcqs().first()
                            val remoteIds = remoteMcqs.map { it.id }.toSet()
                            val remoteQuestions = remoteMcqs.map { it.question.trim().lowercase() }.toSet()
                            
                            val unsyncedMcqs = localMcqs.filter { 
                                !remoteIds.contains(it.id) && !remoteQuestions.contains(it.question.trim().lowercase()) && it.username.trim().lowercase() != "user1"
                            }
                            for (unsynced in unsyncedMcqs) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(listOf(unsynced), Mcq::class.java, alwaysDiscardId = true)
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertMcqs(mcqs = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemoteMcq = remoteResult[0]
                                        appDao.deleteMcq(unsynced)
                                        appDao.insertMcq(realRemoteMcq)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            // Finally, store the newly downloaded remote MCQs
                            for (item in remoteMcqs) {
                                appDao.insertMcq(item)
                            }
                            updateLastSyncTime("mcqs", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 6. Test Results job (Only FULL sync)
                val jobTestResults = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("test_results")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteTestResults = com.example.data.network.SupabaseClient.api.getTestResults(updatedAtFilter = filter)
                            val remoteIds = remoteTestResults.map { it.id }.toSet()
                            val localTestResults = appDao.getAllTestResults().first()
                            
                            val unsyncedResults = localTestResults.filter { !remoteIds.contains(it.id) }
                            for (unsynced in unsyncedResults) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                                        listOf(unsynced), 
                                        TestResult::class.java, 
                                        alwaysDiscardId = true
                                    )
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertTestResults(results = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemote = remoteResult[0]
                                        appDao.deleteTestResult(unsynced)
                                        appDao.insertTestResult(realRemote)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            for (item in remoteTestResults) {
                                appDao.insertTestResult(item)
                            }
                            updateLastSyncTime("test_results", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 7. User Progress job (Only FULL sync)
                val jobProgress = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("user_progress")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteProgress = com.example.data.network.SupabaseClient.api.getUserProgress(updatedAtFilter = filter)
                            for (item in remoteProgress) {
                                val local = appDao.getProgressForTopic(item.userId, item.subjectId, item.topicId)
                                if (local == null || item.lastAttemptedTimestamp > local.lastAttemptedTimestamp) {
                                    appDao.insertOrUpdateProgress(item)
                                }
                            }
                            updateLastSyncTime("user_progress", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 8. Academic Notes job (Only FULL sync)
                val jobNotes = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("notes")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteNotes = com.example.data.network.SupabaseClient.api.getNotes(updatedAtFilter = filter)
                            val remoteIds = remoteNotes.map { it.id }.toSet()
                            val localNotes = appDao.getAllNotes().first()
                            
                            val unsyncedNotes = localNotes.filter { !remoteIds.contains(it.id) }
                            for (unsynced in unsyncedNotes) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                                        listOf(unsynced), 
                                        Note::class.java,
                                        alwaysDiscardId = true
                                    )
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertNotes(notes = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemoteNote = remoteResult[0]
                                        appDao.deleteNote(unsynced)
                                        appDao.insertNote(realRemoteNote)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            for (item in remoteNotes) {
                                appDao.insertNote(item)
                            }
                            updateLastSyncTime("notes", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 9. Note Access Requests job (REALTIME and FULL)
                val jobRequests = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("note_access_requests")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteRequests = com.example.data.network.SupabaseClient.api.getNoteAccessRequests(updatedAtFilter = filter)
                            for (item in remoteRequests) {
                                appDao.insertAccessRequest(item)
                            }
                            updateLastSyncTime("note_access_requests", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 10. Study Feed Posts job (REALTIME and FULL)
                val jobPosts = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("posts")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remotePosts = com.example.data.network.SupabaseClient.api.getPosts(updatedAtFilter = filter)
                            for (item in remotePosts) {
                                appDao.insertPost(item)
                            }
                            updateLastSyncTime("posts", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 11. Comments job (REALTIME and FULL)
                val jobComments = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("comments")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteComments = com.example.data.network.SupabaseClient.api.getComments(updatedAtFilter = filter)
                            for (item in remoteComments) {
                                appDao.insertComment(item)
                            }
                            updateLastSyncTime("comments", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 12. Likes job (REALTIME and FULL)
                val jobLikes = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("post_likes")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteLikes = com.example.data.network.SupabaseClient.api.getPostLikes(updatedAtFilter = filter)
                            for (item in remoteLikes) {
                                appDao.insertLike(item)
                            }
                            updateLastSyncTime("post_likes", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 13. Shares job (REALTIME and FULL)
                val jobShares = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("post_shares")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteShares = com.example.data.network.SupabaseClient.api.getPostShares(updatedAtFilter = filter)
                            for (item in remoteShares) {
                                appDao.insertShare(item)
                            }
                            updateLastSyncTime("post_shares", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 14. Push notifications job (REALTIME and FULL)
                val jobPush = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("push_notifications")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remotePush = com.example.data.network.SupabaseClient.api.getPushNotifications(updatedAtFilter = filter)
                            for (item in remotePush) {
                                appDao.insertPushNotification(item)
                            }
                            updateLastSyncTime("push_notifications", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 15. About Us job (Only FULL sync)
                val jobAboutUs = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("about_us")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteAboutUs = com.example.data.network.SupabaseClient.api.getAboutUs(updatedAtFilter = filter)
                            if (remoteAboutUs.isNotEmpty()) {
                                appDao.insertAboutUs(remoteAboutUs.first())
                            }
                            updateLastSyncTime("about_us", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 16. Success Stories job (Only FULL sync)
                val jobSuccessStories = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("success_stories")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteStories = com.example.data.network.SupabaseClient.api.getSuccessStories(updatedAtFilter = filter)
                            remoteStories.forEach {
                                appDao.insertSuccessStory(it)
                            }
                            updateLastSyncTime("success_stories", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 17. Pending MCQs job (REALTIME and FULL)
                val jobPendingMcqs = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("pending_mcqs")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remotePending = com.example.data.network.SupabaseClient.api.getPendingMcqs(updatedAtFilter = filter)
                            for (item in remotePending) {
                                if (item.username.trim().lowercase() != "user1") {
                                    appDao.insertPendingMcq(item)
                                }
                            }
                            updateLastSyncTime("pending_mcqs", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 18. Announcements job (REALTIME and FULL)
                val jobAnnouncements = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("announcements")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteAnnouncements = com.example.data.network.SupabaseClient.api.getAnnouncements(updatedAtFilter = filter)
                            for (item in remoteAnnouncements) {
                                appDao.insertAnnouncement(item)
                            }
                            updateLastSyncTime("announcements", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 19. MDCAT Countdown job (REALTIME and FULL)
                val jobMdcatCountdown = if (syncType == SyncType.REALTIME || syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("mdcat_countdown")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteMdcat = com.example.data.network.SupabaseClient.api.getMdcatCountdown(updatedAtFilter = filter)
                            if (remoteMdcat.isNotEmpty()) {
                                appDao.insertMdcatCountdown(remoteMdcat.first())
                            }
                            updateLastSyncTime("mdcat_countdown", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 20. Reward Offers job (Only FULL sync)
                val jobRewardOffers = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("reward_offers")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteOffers = com.example.data.network.SupabaseClient.api.getRewardOffers(updatedAtFilter = filter)
                            val remoteIds = remoteOffers.map { it.id }.toSet()
                            val localOffers = appDao.getAllRewardOffers().first()

                            // Sync Up: Upload local offers not on remote
                            val unsyncedOffers = localOffers.filter { !remoteIds.contains(it.id) }
                            for (unsynced in unsyncedOffers) {
                                try {
                                    val remotePayload = com.example.data.network.SupabaseClient.entityToMapListWithoutId(
                                        listOf(unsynced),
                                        RewardOffer::class.java,
                                        alwaysDiscardId = true
                                    )
                                    val remoteResult = com.example.data.network.SupabaseClient.api.insertRewardOffers(body = remotePayload)
                                    if (remoteResult.isNotEmpty()) {
                                        val realRemote = remoteResult[0]
                                        appDao.deleteRewardOffer(unsynced)
                                        appDao.insertRewardOffer(realRemote)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            for (item in remoteOffers) {
                                appDao.insertRewardOffer(item)
                            }
                            updateLastSyncTime("reward_offers", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // 21. Claimed Rewards job (Only FULL sync)
                val jobClaimedRewards = if (syncType == SyncType.FULL) {
                    async {
                        try {
                            val lastSync = getLastSyncTime("claimed_rewards")
                            val filter = if (lastSync > 0) "gt.$lastSync" else null
                            val remoteClaims = com.example.data.network.SupabaseClient.api.getClaimedRewards(updatedAtFilter = filter)
                            for (item in remoteClaims) {
                                appDao.insertClaimedReward(item)
                            }
                            updateLastSyncTime("claimed_rewards", syncStartTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else null

                // Wait for all non-null jobs to complete in parallel
                val activeJobs = listOfNotNull(
                    jobUsers, jobStudentFees, jobAppSettings, jobSubjectsAndTopics, jobMcqs, jobTestResults, jobProgress,
                    jobNotes, jobRequests, jobPosts, jobComments, jobLikes, jobShares, jobPush,
                    jobAboutUs, jobSuccessStories, jobPendingMcqs, jobAnnouncements, jobMdcatCountdown,
                    jobRewardOffers, jobClaimedRewards
                )
                kotlinx.coroutines.awaitAll(*activeJobs.toTypedArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- ABOUT US ---
    fun getAboutUsFlow(): Flow<AboutUs?> = appDao.getAboutUsFlow()
    suspend fun getAboutUsDirect(): AboutUs? = appDao.getAboutUsDirect()
    suspend fun insertAboutUs(aboutUs: AboutUs) = withContext(Dispatchers.IO) {
        appDao.insertAboutUs(aboutUs)
        try {
            val existing = com.example.data.network.SupabaseClient.api.getAboutUs()
            if (existing.any { it.id == aboutUs.id }) {
                com.example.data.network.SupabaseClient.api.updateAboutUs("eq.${aboutUs.id}", aboutUs)
            } else {
                com.example.data.network.SupabaseClient.api.insertAboutUs(listOf(aboutUs))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- SUCCESS STORIES ---
    fun getAllSuccessStoriesFlow(): Flow<List<SuccessStory>> = appDao.getAllSuccessStoriesFlow()
    suspend fun getAllSuccessStoriesDirect(): List<SuccessStory> = appDao.getAllSuccessStoriesDirect()
    suspend fun insertSuccessStory(story: SuccessStory) = withContext(Dispatchers.IO) {
        val rowId = appDao.insertSuccessStory(story)
        val finalStory = if (story.id == 0) story.copy(id = rowId.toInt()) else story
        try {
            val existing = com.example.data.network.SupabaseClient.api.getSuccessStories()
            if (existing.any { it.id == finalStory.id }) {
                com.example.data.network.SupabaseClient.api.updateSuccessStory("eq.${finalStory.id}", finalStory)
            } else {
                com.example.data.network.SupabaseClient.api.insertSuccessStories(listOf(finalStory))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun deleteSuccessStory(story: SuccessStory) = withContext(Dispatchers.IO) {
        appDao.deleteSuccessStory(story)
        try {
            com.example.data.network.SupabaseClient.api.deleteSuccessStory("eq.${story.id}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- MDCAT COUNTDOWN ---
    fun getMdcatCountdownFlow(): Flow<MdcatCountdown?> = appDao.getMdcatCountdownFlow()
    suspend fun getMdcatCountdownDirect(): MdcatCountdown? = appDao.getMdcatCountdownDirect()
    suspend fun insertMdcatCountdown(countdown: MdcatCountdown) = withContext(Dispatchers.IO) {
        appDao.insertMdcatCountdown(countdown)
        try {
            val existing = com.example.data.network.SupabaseClient.api.getMdcatCountdown()
            if (existing.any { it.id == countdown.id }) {
                com.example.data.network.SupabaseClient.api.updateMdcatCountdown("eq.${countdown.id}", countdown)
            } else {
                com.example.data.network.SupabaseClient.api.insertMdcatCountdown(listOf(countdown))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- APP SETTINGS ---
    fun getAllAppSettingsFlow(): Flow<List<AppSetting>> = appDao.getAllAppSettingsFlow()
    
    suspend fun getAllAppSettingsList(): List<AppSetting> = withContext(Dispatchers.IO) {
        appDao.getAllAppSettingsList()
    }

    suspend fun getAppSettingByKey(key: String): AppSetting? = withContext(Dispatchers.IO) {
        appDao.getAppSettingByKey(key)
    }

    suspend fun saveAppSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        val setting = AppSetting(key, value)
        appDao.insertAppSetting(setting)
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.insertAppSettings(settings = listOf(setting))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveAppSettingsList(settings: List<AppSetting>) = withContext(Dispatchers.IO) {
        appDao.insertAppSettingsList(settings)
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val isCustomSupabase = !(rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http"))
        if (isCustomSupabase) {
            try {
                com.example.data.network.SupabaseClient.api.insertAppSettings(settings = settings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Local-only insertions for incremental background loading
    suspend fun insertUserLocally(user: User) = withContext(Dispatchers.IO) {
        appDao.insertUser(user)
    }

    suspend fun insertMcqLocally(mcq: Mcq) = withContext(Dispatchers.IO) {
        appDao.insertMcq(mcq)
    }

    suspend fun insertPostLocally(post: Post) = withContext(Dispatchers.IO) {
        appDao.insertPost(post)
    }

    suspend fun insertNoteLocally(note: Note) = withContext(Dispatchers.IO) {
        appDao.insertNote(note)
    }

    suspend fun insertMdcatCountdownLocally(countdown: MdcatCountdown) = withContext(Dispatchers.IO) {
        appDao.insertMdcatCountdown(countdown)
    }
}

data class BaseMcq(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val detailedExplanation: String
)

data class GeneratedMcq(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val detailedExplanation: String,
    val difficulty: String
)

