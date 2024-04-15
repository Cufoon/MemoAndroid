package cufoon.memo.android.data.api

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import com.skydoves.sandwich.suspendOnSuccess
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cufoon.memo.android.data.constant.MoeMemosException
import cufoon.memo.android.data.model.MemosUserSettingKey
import cufoon.memo.android.data.model.Status
import cufoon.memo.android.ext.DataStoreKeys
import cufoon.memo.android.ext.dataStore
import cufoon.memo.android.util.console
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MemosApiService @Inject constructor(
    @ApplicationContext private val context: Context, private val okHttpClient: OkHttpClient
) {
    private var memosApi: MemosApi? = null
    var host: String? = null
        private set
    var status: Status? = null
        private set
    var client: OkHttpClient = okHttpClient
        private set

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)

    private fun loadStatus() = scope.launch {
        memosApi?.status()?.suspendOnSuccess {
            status = data
        }
    }

    init {
        runBlocking {
            context.dataStore.data.first().let {
                val host = it[DataStoreKeys.Host.key]
                val accessToken = it[DataStoreKeys.AccessToken.key]
                if (!host.isNullOrEmpty()) {
                    val (client, memosApi) = createClient(host, accessToken)
                    this@MemosApiService.client = client
                    this@MemosApiService.memosApi = memosApi
                    this@MemosApiService.host = host
                }
            }
        }
        loadStatus()
    }

    suspend fun update(host: String, accessToken: String?) {
        context.dataStore.edit {
            it[DataStoreKeys.Host.key] = host
            if (!accessToken.isNullOrEmpty()) {
                it[DataStoreKeys.AccessToken.key] = accessToken
            } else {
                it.remove(DataStoreKeys.AccessToken.key)
            }
        }

        mutex.withLock {
            val (client, memosApi) = createClient(host, accessToken)
            this.client = client
            this.memosApi = memosApi
            this.host = host
        }
        loadStatus()
    }

    fun createClient(host: String, accessToken: String?): Pair<OkHttpClient, MemosApi> {
        var client = okHttpClient

        if (!accessToken.isNullOrEmpty()) {
            client = client.newBuilder().addNetworkInterceptor { chain ->
                var request = chain.request()
                if (request.url.host == host.toHttpUrlOrNull()?.host) {
                    try {
                        request =
                            request.newBuilder().addHeader("Authorization", "Bearer $accessToken")
                                .build()
                    } catch (e: Throwable) {
                        console.log("MemosApiService createClient $e")
                    }
                }
                chain.proceed(request)
            }.build()
        }

        return client to Retrofit.Builder().baseUrl(host).client(client).addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder().add(
                    MemosUserSettingKey::class.java,
                    EnumJsonAdapter.create(MemosUserSettingKey::class.java)
                        .withUnknownFallback(MemosUserSettingKey.UNKNOWN)
                ).add(KotlinJsonAdapterFactory()).build()
            )
        ).addCallAdapterFactory(ApiResponseCallAdapterFactory.create()).build()
            .create(MemosApi::class.java)
    }

    suspend fun <T> call(block: suspend (MemosApi) -> ApiResponse<T>): ApiResponse<T> {
        return memosApi?.let { block(it) } ?: ApiResponse.exception(MoeMemosException.notLogin)
    }
}
