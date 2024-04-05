package dk.scheduling.schedulingfrontend.api
import dk.scheduling.schedulingfrontend.api.protocol.CreateDeviceRequest
import dk.scheduling.schedulingfrontend.api.protocol.CreateTaskRequest
import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.api.protocol.GetEventsResponse
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginResponse
import dk.scheduling.schedulingfrontend.api.protocol.Task
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    /*
     * Accounts
     */
    @POST("accounts/register")
    suspend fun registerAccount(
        @Body registerOrLoginRequest: RegisterOrLoginRequest,
    ): Response<RegisterOrLoginResponse>

    @POST("accounts/login")
    suspend fun loginToAccount(
        @Body registerOrLoginRequest: RegisterOrLoginRequest,
    ): Response<RegisterOrLoginResponse>

    /*
     * Devices
     */
    @GET("devices/all")
    suspend fun getDevices(
        @Header("X-Auth-Token") authToken: String,
    ): Response<List<Device>>

    @POST("devices/create")
    suspend fun createDevice(
        @Header("X-Auth-Token") authToken: String,
        @Body createDeviceRequest: CreateDeviceRequest,
    ): Response<Device>

    @DELETE("devices/delete")
    suspend fun deleteDevice(
        @Header("X-Auth-Token") authToken: String,
        @Query("id") deviceId: Long,
    ): Response<Void>

    /*
     * Tasks
     */
    @GET("tasks/all")
    suspend fun getTasks(
        @Header("X-Auth-Token") authToken: String,
    ): Response<List<Task>>

    @POST("tasks/create")
    suspend fun createTask(
        @Header("X-Auth-Token") authToken: String,
        @Body createTaskRequest: CreateTaskRequest,
    ): Response<Task>

    @DELETE("tasks/delete")
    suspend fun deleteTask(
        @Header("X-Auth-Token") authToken: String,
        @Query("id") taskId: Long,
    ): Response<Void>

    @GET("events/all")
    suspend fun getEvents(
        @Header("X-Auth-Token") authToken: String,
    ): Response<GetEventsResponse>
}
