package dk.scheduling.schedulingfrontend.api
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("test")
    fun test(): Call<String>
}
