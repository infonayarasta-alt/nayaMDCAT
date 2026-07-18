package com.example.data.network

import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

@JvmSuppressWildcards
interface SupabaseApi {
    // --- USERS ---
    @GET("rest/v1/users?limit=100000")
    suspend fun getUsers(@Query("updated_at") updatedAtFilter: String? = null): List<User>

    @GET("rest/v1/users")
    suspend fun getUsersFiltered(@Query("username") usernameFilter: String): List<User>

    @GET("rest/v1/users")
    suspend fun getUsersByPhoneFiltered(@Query("phoneNumber") phoneNumberFilter: String): List<User>

    @GET("rest/v1/users")
    suspend fun getUsersByQueryMap(@QueryMap options: Map<String, String>): List<User>

    @POST("rest/v1/users")
    suspend fun insertUsers(
        @Query("on_conflict") onConflict: String = "username",
        @Body users: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<User>

    @PATCH("rest/v1/users")
    suspend fun updateUser(
        @Query("id") idFilter: String,
        @Body body: User
    ): List<User>

    @DELETE("rest/v1/users")
    suspend fun deleteUser(@Query("id") idFilter: String)

    // --- SUBJECTS ---
    @GET("rest/v1/subjects?limit=100000")
    suspend fun getSubjects(@Query("updated_at") updatedAtFilter: String? = null): List<Subject>

    @POST("rest/v1/subjects")
    suspend fun insertSubjects(
        @Query("on_conflict") onConflict: String = "name",
        @Body subjects: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<Subject>

    @PATCH("rest/v1/subjects")
    suspend fun updateSubject(
        @Query("id") idFilter: String,
        @Body body: Subject
    ): List<Subject>

    @DELETE("rest/v1/subjects")
    suspend fun deleteSubject(@Query("id") idFilter: String)

    // --- TOPICS ---
    @GET("rest/v1/topics?limit=100000")
    suspend fun getTopics(@Query("updated_at") updatedAtFilter: String? = null): List<Topic>

    @POST("rest/v1/topics")
    suspend fun insertTopics(
        @Query("on_conflict") onConflict: String = "name",
        @Body topics: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<Topic>

    @PATCH("rest/v1/topics")
    suspend fun updateTopic(
        @Query("id") idFilter: String,
        @Body body: Topic
    ): List<Topic>

    @DELETE("rest/v1/topics")
    suspend fun deleteTopic(@Query("id") idFilter: String)

    // --- MCQS ---
    @GET("rest/v1/mcqs?or=(status.eq.APPROVED,status.is.null)&limit=100000")
    suspend fun getMcqs(@Query("updated_at") updatedAtFilter: String? = null): List<Mcq>

    @GET("rest/v1/mcqs?or=(status.eq.APPROVED,status.is.null)&order=id.asc")
    suspend fun getMcqsPage(
        @Query("limit") limit: Int,
        @Query("id") idFilter: String,
        @Query("updated_at") updatedAtFilter: String? = null
    ): List<Mcq>

    @POST("rest/v1/mcqs")
    suspend fun insertMcqs(
        @Query("on_conflict") onConflict: String? = null,
        @Body mcqs: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<Mcq>

    @PATCH("rest/v1/mcqs")
    suspend fun updateMcq(
        @Query("id") idFilter: String,
        @Body body: Mcq
    ): List<Mcq>

    @DELETE("rest/v1/mcqs")
    suspend fun deleteMcq(@Query("id") idFilter: String)

    // --- TEST RESULTS ---
    @GET("rest/v1/test_results?limit=100000")
    suspend fun getTestResults(@Query("updated_at") updatedAtFilter: String? = null): List<TestResult>

    @POST("rest/v1/test_results")
    suspend fun insertTestResults(
        @Query("on_conflict") onConflict: String? = null,
        @Body results: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<TestResult>

    @DELETE("rest/v1/test_results")
    suspend fun deleteTestResult(@Query("id") idFilter: String)

    // --- USER PROGRESS ---
    @GET("rest/v1/user_progress?limit=100000")
    suspend fun getUserProgress(@Query("updated_at") updatedAtFilter: String? = null): List<UserProgress>

    @POST("rest/v1/user_progress")
    suspend fun insertUserProgress(
        @Query("on_conflict") onConflict: String? = null,
        @Body progress: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<UserProgress>

    @PATCH("rest/v1/user_progress")
    suspend fun updateUserProgress(
        @Query("id") idFilter: String,
        @Body body: UserProgress
    ): List<UserProgress>

    @DELETE("rest/v1/user_progress")
    suspend fun deleteUserProgress(@Query("id") idFilter: String)

    // --- NOTES ---
    @GET("rest/v1/notes?limit=100000")
    suspend fun getNotes(@Query("updated_at") updatedAtFilter: String? = null): List<Note>

    @POST("rest/v1/notes")
    suspend fun insertNotes(
        @Query("on_conflict") onConflict: String? = null,
        @Body notes: @JvmSuppressWildcards List<Map<String, Any?>>
    ): List<Note>

    @PATCH("rest/v1/notes")
    suspend fun updateNote(
        @Query("id") idFilter: String,
        @Body body: Note
    ): List<Note>

    @DELETE("rest/v1/notes")
    suspend fun deleteNote(@Query("id") idFilter: String)

    // --- NOTE ACCESS REQUESTS ---
    @GET("rest/v1/note_access_requests?limit=100000")
    suspend fun getNoteAccessRequests(@Query("updated_at") updatedAtFilter: String? = null): List<NoteAccessRequest>

    @POST("rest/v1/note_access_requests")
    suspend fun insertNoteAccessRequests(@Body requests: List<NoteAccessRequest>): List<NoteAccessRequest>

    @PATCH("rest/v1/note_access_requests")
    suspend fun updateNoteAccessRequest(
        @Query("id") idFilter: String,
        @Body body: NoteAccessRequest
    ): List<NoteAccessRequest>

    @DELETE("rest/v1/note_access_requests")
    suspend fun deleteNoteAccessRequest(@Query("id") idFilter: String)

    // --- POSTS ---
    @GET("rest/v1/posts?limit=100000")
    suspend fun getPosts(@Query("updated_at") updatedAtFilter: String? = null): List<Post>

    @POST("rest/v1/posts")
    suspend fun insertPosts(@Body posts: List<Post>): List<Post>

    @PATCH("rest/v1/posts")
    suspend fun updatePost(
        @Query("id") idFilter: String,
        @Body body: Post
    ): List<Post>

    @DELETE("rest/v1/posts")
    suspend fun deletePost(@Query("id") idFilter: String)

    // --- COMMENTS ---
    @GET("rest/v1/comments?limit=100000")
    suspend fun getComments(@Query("updated_at") updatedAtFilter: String? = null): List<Comment>

    @POST("rest/v1/comments")
    suspend fun insertComments(@Body comments: List<Comment>): List<Comment>

    @PATCH("rest/v1/comments")
    suspend fun updateComment(
        @Query("id") idFilter: String,
        @Body body: Comment
    ): List<Comment>

    @DELETE("rest/v1/comments")
    suspend fun deleteComment(@Query("id") idFilter: String)

    // --- POST LIKES ---
    @GET("rest/v1/post_likes?limit=100000")
    suspend fun getPostLikes(@Query("updated_at") updatedAtFilter: String? = null): List<PostLike>

    @POST("rest/v1/post_likes")
    suspend fun insertPostLike(@Body like: PostLike): List<Map<String, Any>>

    @DELETE("rest/v1/post_likes")
    suspend fun deletePostLike(
        @Query("postId") postIdFilter: String,
        @Query("userId") userIdFilter: String
    )

    // --- POST SHARES ---
    @GET("rest/v1/post_shares?limit=100000")
    suspend fun getPostShares(@Query("updated_at") updatedAtFilter: String? = null): List<PostShare>

    @POST("rest/v1/post_shares")
    suspend fun insertPostShare(@Body share: PostShare): List<Map<String, Any>>

    // --- FINANCIAL TRANSACTIONS ---
    @GET("rest/v1/financial_transactions?limit=100000")
    suspend fun getFinancialTransactions(@Query("updated_at") updatedAtFilter: String? = null): List<FinancialTransaction>

    @POST("rest/v1/financial_transactions")
    suspend fun insertFinancialTransactions(@Body transactions: List<FinancialTransaction>): List<FinancialTransaction>

    @DELETE("rest/v1/financial_transactions")
    suspend fun deleteFinancialTransaction(@Query("id") idFilter: String)

    // --- PUSH NOTIFICATIONS ---
    @GET("rest/v1/push_notifications?limit=100000")
    suspend fun getPushNotifications(@Query("updated_at") updatedAtFilter: String? = null): List<PushNotification>

    @POST("rest/v1/push_notifications")
    suspend fun insertPushNotifications(@Body list: List<PushNotification>): List<PushNotification>

    @DELETE("rest/v1/push_notifications")
    suspend fun deletePushNotification(@Query("id") idFilter: String)

    // --- PENDING MCQS ---
    @GET("rest/v1/mcqs?status=eq.PENDING&limit=100000")
    suspend fun getPendingMcqs(@Query("updated_at") updatedAtFilter: String? = null): List<PendingMcq>

    @POST("rest/v1/mcqs")
    suspend fun insertPendingMcqs(@Body list: @JvmSuppressWildcards List<Map<String, Any?>>): List<PendingMcq>

    @DELETE("rest/v1/mcqs")
    suspend fun deletePendingMcq(@Query("id") idFilter: String)

    // --- ABOUT US ---
    @GET("rest/v1/about_us?limit=100000")
    suspend fun getAboutUs(@Query("updated_at") updatedAtFilter: String? = null): List<AboutUs>

    @POST("rest/v1/about_us")
    suspend fun insertAboutUs(@Body body: List<AboutUs>): List<AboutUs>

    @PATCH("rest/v1/about_us")
    suspend fun updateAboutUs(
        @Query("id") idFilter: String,
        @Body body: AboutUs
    ): List<AboutUs>

    // --- SUCCESS STORIES ---
    @GET("rest/v1/success_stories?limit=100000")
    suspend fun getSuccessStories(@Query("updated_at") updatedAtFilter: String? = null): List<SuccessStory>

    @POST("rest/v1/success_stories")
    suspend fun insertSuccessStories(@Body body: List<SuccessStory>): List<SuccessStory>

    @PATCH("rest/v1/success_stories")
    suspend fun updateSuccessStory(
        @Query("id") idFilter: String,
        @Body body: SuccessStory
    ): List<SuccessStory>

    @DELETE("rest/v1/success_stories")
    suspend fun deleteSuccessStory(@Query("id") idFilter: String)

    // --- ANNOUNCEMENTS ---
    @GET("rest/v1/announcements?limit=100000")
    suspend fun getAnnouncements(@Query("updated_at") updatedAtFilter: String? = null): List<Announcement>

    @POST("rest/v1/announcements")
    suspend fun insertAnnouncements(@Body body: List<Announcement>): List<Announcement>

    @DELETE("rest/v1/announcements")
    suspend fun deleteAnnouncement(@Query("id") idFilter: String)

    // --- MDCAT COUNTDOWN ---
    @GET("rest/v1/mdcat_countdown?limit=100000")
    suspend fun getMdcatCountdown(@Query("updated_at") updatedAtFilter: String? = null): List<MdcatCountdown>

    @POST("rest/v1/mdcat_countdown")
    suspend fun insertMdcatCountdown(@Body body: List<MdcatCountdown>): List<MdcatCountdown>

    @PATCH("rest/v1/mdcat_countdown")
    suspend fun updateMdcatCountdown(
        @Query("id") idFilter: String,
        @Body body: MdcatCountdown
    ): List<MdcatCountdown>

    // --- REWARD OFFERS ---
    @GET("rest/v1/reward_offers?limit=100000")
    suspend fun getRewardOffers(@Query("updated_at") updatedAtFilter: String? = null): List<RewardOffer>

    @POST("rest/v1/reward_offers")
    suspend fun insertRewardOffers(@Body body: @JvmSuppressWildcards List<Map<String, Any?>>): List<RewardOffer>

    @DELETE("rest/v1/reward_offers")
    suspend fun deleteRewardOffer(@Query("id") idFilter: String)

    // --- CLAIMED REWARDS ---
    @GET("rest/v1/claimed_rewards?limit=100000")
    suspend fun getClaimedRewards(@Query("updated_at") updatedAtFilter: String? = null): List<ClaimedReward>

    @POST("rest/v1/claimed_rewards")
    suspend fun insertClaimedRewards(@Body body: @JvmSuppressWildcards List<Map<String, Any?>>): List<ClaimedReward>

    // --- STORAGE ---
    @POST("storage/v1/bucket")
    suspend fun createBucket(@Body body: Map<String, @JvmSuppressWildcards Any?>): okhttp3.ResponseBody

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadStorageFile(
        @Path("bucket") bucketName: String,
        @Path("path") filePath: String,
        @Body fileBody: okhttp3.RequestBody
    ): okhttp3.ResponseBody

    // --- APP SETTINGS ---
    @GET("rest/v1/app_settings?limit=100000")
    suspend fun getAppSettings(@Query("updated_at") updatedAtFilter: String? = null): List<AppSetting>

    @POST("rest/v1/app_settings")
    suspend fun insertAppSettings(
        @Query("on_conflict") onConflict: String = "key",
        @Body settings: @JvmSuppressWildcards List<AppSetting>
    ): List<AppSetting>

    // --- STUDENT FEES ---
    @GET("rest/v1/student_fees?limit=100000")
    suspend fun getStudentFees(@Query("updated_at") updatedAtFilter: String? = null): List<StudentFee>

    @POST("rest/v1/student_fees")
    suspend fun insertStudentFees(
        @Query("on_conflict") onConflict: String = "userId",
        @Body fees: @JvmSuppressWildcards List<StudentFee>
    ): List<StudentFee>

    @PATCH("rest/v1/student_fees")
    suspend fun updateStudentFee(
        @Query("userId") userIdFilter: String,
        @Body body: StudentFee
    ): List<StudentFee>

    @DELETE("rest/v1/student_fees")
    suspend fun deleteStudentFee(@Query("userId") userIdFilter: String)
}

object SupabaseClient {
    private val moshi: Moshi = Moshi.Builder()
        .add(object {
            @com.squareup.moshi.FromJson
            fun fromJson(reader: com.squareup.moshi.JsonReader): String {
                if (reader.peek() == com.squareup.moshi.JsonReader.Token.NULL) {
                    reader.nextNull<Any>()
                    return ""
                }
                return reader.nextString()
            }
            @com.squareup.moshi.ToJson
            fun toJson(writer: com.squareup.moshi.JsonWriter, value: String?) {
                writer.value(value ?: "")
            }
        })
        .add(object {
            @com.squareup.moshi.FromJson
            fun fromJson(reader: com.squareup.moshi.JsonReader): Int {
                if (reader.peek() == com.squareup.moshi.JsonReader.Token.NULL) {
                    reader.nextNull<Any>()
                    return 0
                }
                return try {
                    reader.nextInt()
                } catch (e: Exception) {
                    0
                }
            }
            @com.squareup.moshi.ToJson
            fun toJson(writer: com.squareup.moshi.JsonWriter, value: Int?) {
                writer.value(value ?: 0)
            }
        })
        .add(object {
            @com.squareup.moshi.FromJson
            fun fromJson(reader: com.squareup.moshi.JsonReader): Boolean {
                if (reader.peek() == com.squareup.moshi.JsonReader.Token.NULL) {
                    reader.nextNull<Any>()
                    return false
                }
                return try {
                    reader.nextBoolean()
                } catch (e: Exception) {
                    false
                }
            }
            @com.squareup.moshi.ToJson
            fun toJson(writer: com.squareup.moshi.JsonWriter, value: Boolean?) {
                writer.value(value ?: false)
            }
        })
        .add(object {
            @com.squareup.moshi.FromJson
            fun fromJson(reader: com.squareup.moshi.JsonReader): Long {
                if (reader.peek() == com.squareup.moshi.JsonReader.Token.NULL) {
                    reader.nextNull<Any>()
                    return 0L
                }
                return try {
                    reader.nextLong()
                } catch (e: Exception) {
                    0L
                }
            }
            @com.squareup.moshi.ToJson
            fun toJson(writer: com.squareup.moshi.JsonWriter, value: Long?) {
                writer.value(value ?: 0L)
            }
        })
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val keyMappings = listOf(
        "subjectId" to "subject_id",
        "topicId" to "topic_id",
        "subjectName" to "subject_name",
        "topicName" to "topic_name",
        "optionA" to "option_a",
        "optionB" to "option_b",
        "optionC" to "option_c",
        "optionD" to "option_d",
        "optionA" to "optiona",
        "optionB" to "optionb",
        "optionC" to "optionc",
        "optionD" to "optiond",
        "correctOption" to "correct_option",
        "detailedExplanation" to "detailed_explanation",
        "iconName" to "icon_name",
        "passwordHash" to "password_hash",
        "passwordHash" to "password",
        "fullName" to "full_name",
        "fullName" to "fullname",
        "gradeLevel" to "grade_level",
        "gradeLevel" to "gradelevel",
        "studyGoal" to "study_goal",
        "studyGoal" to "studygoal",
        "avatarColor" to "avatar_color",
        "avatarColor" to "avatarcolor",
        "phoneNumber" to "phone_number",
        "phoneNumber" to "phonenumber",
        "studentType" to "student_type",
        "studentType" to "studenttype",
        "profileImageUri" to "profile_image_uri",
        "profileImageUri" to "profileimageuri",
        "currentStreak" to "current_streak",
        "currentStreak" to "currentstreak",
        "longestStreak" to "longest_streak",
        "longestStreak" to "longeststreak",
        "lastActiveTimestamp" to "last_active_timestamp",
        "lastActiveTimestamp" to "lastactivetimestamp",
        "createdAt" to "created_at",
        "createdAt" to "createdat",
        "isPaid" to "is_paid",
        "isPaid" to "ispaid",
        "approvedMcqs" to "approved_mcqs",
        "approvedMcqs" to "approvedmcqs",
        "rejectedMcqs" to "rejected_mcqs",
        "rejectedMcqs" to "rejectedmcqs",
        "userId" to "user_id",
        "totalQuestions" to "total_questions",
        "correctAnswers" to "correct_answers",
        "wrongAnswers" to "wrong_answers",
        "scorePercentage" to "score_percentage",
        "isPassed" to "is_passed",
        "testType" to "test_type",
        "metadataName" to "metadata_name",
        "questionsAttempted" to "questions_attempted",
        "questionsCorrect" to "questions_correct",
        "lastAttemptedTimestamp" to "last_attempted_timestamp",
        "pdfUri" to "pdf_uri",
        "imageUri" to "image_uri",
        "docUri" to "doc_uri",
        "previewPdfUri" to "preview_pdf_uri",
        "previewImageUri" to "preview_image_uri",
        "previewDocUri" to "preview_doc_uri",
        "imageUrl" to "image_url",
        "targetUserId" to "target_user_id",
        "senderName" to "sender_name",
        "noteId" to "note_id",
        "postId" to "post_id",
        "audioUri" to "audio_uri",
        "isSolved" to "is_solved",
        "likeCount" to "like_count",
        "commentCount" to "comment_count",
        "shareCount" to "share_count",
        "isClosedByAdmin" to "is_closed_by_admin",
        "parentCommentId" to "parent_comment_id",
        "studentName" to "student_name",
        "studentImage" to "student_image",
        "storyText" to "story_text",
        "targetUserIds" to "target_user_ids",
        "displayDuration" to "display_duration",
        "isCloseable" to "is_closeable",
        "requiredMcqs" to "required_mcqs",
        "rewardType" to "reward_type",
        "notesIds" to "notes_ids",
        "cashAmount" to "cash_amount",
        "offerId" to "offer_id",
        "claimedAt" to "claimed_at",
        "subscriptionType" to "subscription_type",
        "subscriptionType" to "subscriptiontype",
        "subscriptionStartAt" to "subscription_start_at",
        "subscriptionStartAt" to "subscriptionstartat",
        "subscriptionExpiresAt" to "subscription_expires_at",
        "subscriptionExpiresAt" to "subscriptionexpiresat",
        "updatedAt" to "updated_at"
    )

    fun rewriteJsonToCamelCase(jsonStr: String): String {
        try {
            var res = jsonStr
            // Strip out null values to allow Moshi to fall back to Kotlin default values
            res = res.replace(Regex(",\\s*\"[a-zA-Z0-9_]+\"\\s*:\\s*null\\s*"), "")
            res = res.replace(Regex("\"[a-zA-Z0-9_]+\"\\s*:\\s*null\\s*,?\\s*"), "")

            for (mapping in keyMappings) {
                val camel = mapping.first
                val snake = mapping.second
                res = res.replace(Regex("\"$snake\"\\s*:"), "\"$camel\":")
            }
            return res
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonStr
        }
    }

    fun rewriteJsonToSnakeCase(jsonStr: String): String {
        try {
            var res = jsonStr
            for (mapping in keyMappings) {
                val camel = mapping.first
                val snake = mapping.second
                res = res.replace(Regex("\"$camel\"\\s*:"), "\"$snake\":")
            }
            return res
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonStr
        }
    }

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                val url = request.url
                val onConflictParam = url.queryParameter("on_conflict")
                if (onConflictParam != null) {
                    var newOnConflict: String = onConflictParam
                    for (mapping in keyMappings) {
                        val first = mapping.first
                        val second = mapping.second
                        if (second != null) {
                            newOnConflict = newOnConflict.replace(first, second)
                        }
                    }
                    if (newOnConflict != onConflictParam) {
                        request = request.newBuilder()
                            .url(url.newBuilder().setQueryParameter("on_conflict", newOnConflict).build())
                            .build()
                    }
                }
                val requestBuilder = request.newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")

                val path = request.url.encodedPath

                // Only add Content-Type: application/json if not already present and only for rest endpoints
                if (request.header("Content-Type") == null && path.contains("/rest/v1/")) {
                    requestBuilder.addHeader("Content-Type", "application/json")
                }
                
                // Only request representation and merge duplicates for REST endpoints if not already specified
                if (request.header("Prefer") == null && path.contains("/rest/v1/")) {
                    if (request.method == "POST") {
                        val hasOnConflict = request.url.queryParameter("on_conflict") != null
                        if (hasOnConflict) {
                            requestBuilder.addHeader("Prefer", "return=representation,resolution=merge-duplicates")
                        } else {
                            requestBuilder.addHeader("Prefer", "return=representation")
                        }
                    } else {
                        requestBuilder.addHeader("Prefer", "return=representation")
                    }
                }

                val method = request.method
                val hasBody = request.body != null
                if (hasBody && (method == "POST" || method == "PATCH" || method == "PUT") && path.contains("/rest/v1/")) {
                    try {
                        val body = request.body
                        val buffer = okio.Buffer()
                        body?.writeTo(buffer)
                        val charset = body?.contentType()?.charset(java.nio.charset.Charset.forName("UTF-8")) ?: java.nio.charset.Charset.forName("UTF-8")
                        val rawBodyString = buffer.readString(charset)
                        val convertedBodyString = rewriteJsonToSnakeCase(rawBodyString)
                        val bytes = convertedBodyString.toByteArray(charset)
                        val newRequestBody = okhttp3.RequestBody.Companion.run { bytes.toRequestBody(body?.contentType()) }
                        requestBuilder.method(method, newRequestBody)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                var response = chain.proceed(requestBuilder.build())
                
                // Rewrite JSON response keys from snake_case to camelCase safely without crash risk
                try {
                    if (response.isSuccessful && path.contains("/rest/v1/")) {
                        val responseBody = response.body
                        if (responseBody != null) {
                            val contentType = responseBody.contentType()
                            val rawBodyString = responseBody.string()
                            val convertedBodyString = try {
                                rewriteJsonToCamelCase(rawBodyString)
                            } catch (innerEx: Exception) {
                                innerEx.printStackTrace()
                                rawBodyString
                            }
                            val newResponseBody = okhttp3.ResponseBody.create(contentType, convertedBodyString)
                            response = response.newBuilder().body(newResponseBody).build()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                response
            }
            .addInterceptor(logging)
            .build()
    }

    val api: SupabaseApi by lazy {
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        val cleanUrl = if (rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http")) {
            "https://example.com/"
        } else {
            if (rawUrl.endsWith("/")) rawUrl else "$rawUrl/"
        }

        Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }

    private fun cleanNumbersInMap(map: Map<String, Any?>): Map<String, Any?> {
        return map.mapValues { (_, value) ->
            when (value) {
                is Double -> {
                    if (value % 1.0 == 0.0) {
                        try {
                            if (value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) {
                                value.toInt()
                            } else {
                                value.toLong()
                            }
                        } catch (e: Exception) {
                            value
                        }
                    } else {
                        value
                    }
                }
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    cleanNumbersInMap(value as Map<String, Any?>)
                }
                is List<*> -> {
                    value.map { item ->
                        if (item is Map<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            cleanNumbersInMap(item as Map<String, Any?>)
                        } else if (item is Double && item % 1.0 == 0.0) {
                            try {
                                if (item >= Int.MIN_VALUE && item <= Int.MAX_VALUE) {
                                    item.toInt()
                                } else {
                                    item.toLong()
                                }
                            } catch (e: Exception) {
                                item
                            }
                        } else {
                            item
                        }
                    }
                }
                else -> value
            }
        }
    }

    fun <T : Any> entityToMapListWithoutId(
        entities: List<T>,
        classType: Class<T>,
        alwaysDiscardId: Boolean = false
    ): List<Map<String, Any?>> {
        val adapter = moshi.adapter(classType)
        val mapAdapter = moshi.adapter(Map::class.java)
        return entities.map { entity ->
            val json = adapter.toJson(entity)
            val rawMap = mapAdapter.fromJson(json) as? Map<String, Any?> ?: emptyMap()
            val filteredMap = rawMap.filter { (key, value) ->
                !(key == "id" && (alwaysDiscardId || value == null || value == 0 || (value is Double && value == 0.0) || (value is Int && value == 0)))
            }
            cleanNumbersInMap(filteredMap)
        }
    }

    suspend fun uploadFile(bucketName: String, filePath: String, bytes: ByteArray, mimeType: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Upload direct binary to Supabase Storage first to save slow and redundant bucket checks
                val mediaType = okhttp3.MediaType.Companion.run { mimeType.toMediaTypeOrNull() }
                val requestBody = okhttp3.RequestBody.Companion.run { bytes.toRequestBody(mediaType) }
                
                try {
                    api.uploadStorageFile(bucketName, filePath, requestBody)
                } catch (uploadException: Exception) {
                    // Only try to create the bucket if the first direct upload attempt fails (e.g. 404 bucket not found)
                    try {
                        api.createBucket(mapOf("id" to bucketName, "name" to bucketName, "public" to true))
                    } catch (e: Exception) {
                        // Suppress error if bucket creation fails/unauthorized (standard user may lack permissions to create buckets)
                        e.printStackTrace()
                    }
                    // Retry upload once after attempting bucket creation
                    api.uploadStorageFile(bucketName, filePath, requestBody)
                }

                val rawUrl = BuildConfig.SUPABASE_URL.trim()
                val cleanUrl = if (rawUrl.isEmpty() || rawUrl.contains("YOUR_SUPABASE") || !rawUrl.startsWith("http")) {
                    "https://example.com"
                } else {
                    if (rawUrl.endsWith("/")) rawUrl.substring(0, rawUrl.length - 1) else rawUrl
                }
                "$cleanUrl/storage/v1/object/public/$bucketName/$filePath"
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
