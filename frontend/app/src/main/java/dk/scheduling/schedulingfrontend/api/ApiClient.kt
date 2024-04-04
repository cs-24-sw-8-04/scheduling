package dk.scheduling.schedulingfrontend.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    /**
     * The base URL of the API
     * 10.0.2.2 is a special alias to your host loopback interface (127.0.0.1 on your development machine)
     * 2222 is the port where the server is running
     */
    private const val BASE_URL = "http://10.0.2.2:2222/"
    private val gson = GsonBuilder().setLenient().create()
    private val okHttpClient: OkHttpClient by lazy {
        val trustAllCertificates =
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                },
            )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCertificates, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCertificates[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Allow all hostnames
            .build()
    }
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

object ApiClient {
    val apiService: ApiService by lazy {
        RetrofitClient.retrofit.create(ApiService::class.java)
    }
}
