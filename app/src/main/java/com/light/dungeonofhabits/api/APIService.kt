package com.light.dungeonofhabits.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.light.dungeonofhabits.models.BattleResponse
import com.light.dungeonofhabits.models.CharacterResponse
import com.light.dungeonofhabits.models.ForgorRequest
import com.light.dungeonofhabits.models.LoginRequest
import com.light.dungeonofhabits.models.LoginResponse
import com.light.dungeonofhabits.models.RegisterRequest
import com.light.dungeonofhabits.models.GenericResponse
import com.light.dungeonofhabits.models.PickCharacterRequest
import com.light.dungeonofhabits.models.PickPetRequest
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.models.Task
import com.light.dungeonofhabits.models.TaskRequest
import com.light.dungeonofhabits.models.TaskResponse
import com.light.dungeonofhabits.models.ToggleTaskResponse
import com.light.dungeonofhabits.models.User
import com.light.dungeonofhabits.utils.SecurePrefs
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface APIService {
        @POST("/api/users/register")
        fun register(
            @Body registerRequest: RegisterRequest
        ): Call<GenericResponse>

        @POST("/api/users/login")
        fun login(
            @Body loginRequest: LoginRequest
        ): Call<LoginResponse>

        @GET("api/characters")
        fun getCharacters(): Call<CharacterResponse>

        @POST("api/users/pick-character")
        fun pickCharacter(
            @Body body: PickCharacterRequest
        ): Call<GenericResponse>

        @POST("api/pets/pick")
        fun pickPet(
            @Body body: PickPetRequest
        ): Call<GenericResponse>

        @GET("api/users/profile")
        fun getUserProfile(): Call<Profile>

        @POST("api/tasks/add")
        fun addTask(@Body task: TaskRequest): Call<TaskResponse>

        @GET("api/tasks/fetchTasks")
        fun getTasks(): Call<List<Task>>

        @GET("api/tasks/fetchDailies")
        fun getDailies(): Call<List<Task>>

        @PUT("api/tasks/edit/{taskId}")
        fun editTask(@Path("taskId") taskId: String, @Body task: TaskRequest): Call<GenericResponse>

        @PATCH("api/tasks/toggle/{taskId}")
        fun toggleTask(@Path("taskId") taskId: String): Call<ToggleTaskResponse>

        @DELETE("api/tasks/remove/{taskId}")
        fun deleteTask(@Path("taskId") taskId: String): Call<GenericResponse>

        @POST("api/battle/run")
        fun runBattle(): Call<BattleResponse>

        @POST("api/items/buy")
        fun buyItem(@Body body: Any): Call<GenericResponse>

        @POST("api/items/use")
        fun useItem(@Body body: Any): Call<GenericResponse>

        @PUT("api/users/update")
        fun updateProfile(@Body body: Any): Call<Profile>

        @POST("api/users/forgot-password")
        fun forgotPassword(@Body forgorRequest: ForgorRequest): Call<GenericResponse>

}
object ApiClient {
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        val hostname = "truenothingness.id.vn"
        val certificatePinner = CertificatePinner.Builder()
            .add(hostname, "sha256/Qm16xcp7BY8/CSm9aRy/QzaQPWNyjBijZklYlx4B7PQ=")
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = SecurePrefs.getToken(context)
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .certificatePinner(certificatePinner)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl("https://$hostname/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    val apiService: APIService
        get() = retrofit?.create(APIService::class.java)
            ?: throw IllegalStateException("ApiClient not initialized. Call ApiClient.init first.")
}
