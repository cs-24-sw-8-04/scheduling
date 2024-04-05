package dk.scheduling.schedulingfrontend

import dk.scheduling.schedulingfrontend.api.ApiService
import dk.scheduling.schedulingfrontend.api.getApiClient
import dk.scheduling.schedulingfrontend.api.protocol.CreateDeviceRequest
import dk.scheduling.schedulingfrontend.api.protocol.CreateTaskRequest
import dk.scheduling.schedulingfrontend.api.protocol.Device
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest
import dk.scheduling.schedulingfrontend.api.protocol.Task
import dk.scheduling.schedulingfrontend.api.protocol.Timespan
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextULong

class ApiServiceTest {
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        apiService = getApiClient("http://localhost:3000")
    }

    /*
     * Accounts
     */

    @Test
    fun testRegisterAccount() {
        createAccount("register_test")
    }

    @Test
    fun testLoginToAccount() {
        runBlocking {
            val (_, username) = createAccount("login_test")

            val response = apiService.loginToAccount(RegisterOrLoginRequest(username, "test_password"))
            assert(response.isSuccessful) { printErrorContext(response) }

            val loginResponse = response.body()!!
        }
    }

    /*
     * Tasks
     */

    @Test
    fun testCreateTask() {
        runBlocking {
            val (authToken, _) = createAccount("create_task_test")

            val device = createDevice(authToken)

            val task = createTask(authToken, device)
        }
    }

    @Test
    fun testGetAllTasks() {
        runBlocking {
            val (authToken, _) = createAccount("get_all_tasks_test")

            val device = createDevice(authToken)

            val emptyResponse =
                apiService.getTasks(
                    authToken.toString(),
                )
            assert(emptyResponse.isSuccessful) { printErrorContext(emptyResponse) }

            val emptyTasks = emptyResponse.body()!!

            assert(emptyTasks.isEmpty())

            val task = createTask(authToken, device)

            val taskResponse = apiService.getTasks(authToken.toString())
            assert(taskResponse.isSuccessful) { printErrorContext(taskResponse) }

            val fullTasks = taskResponse.body()!!
            assert(fullTasks.size == 1)

            val gottenTask = fullTasks.single()

            assertEquals(task, gottenTask)
        }
    }

    @Test
    fun testGetEvents() {
        runBlocking {
            val (authToken, _) = createAccount("get_events_test")

            val response = apiService.getEvents(authToken.toString())
            assert(response.isSuccessful) { printErrorContext(response) }
        }
    }

    private fun createAccount(username: String): Pair<UUID, String> {
        return runBlocking {
            val randomNumber = Random.nextULong()
            val randomUsername = username + randomNumber
            val response = apiService.registerAccount(RegisterOrLoginRequest(randomUsername, "test_password"))
            assert(response.isSuccessful) { printErrorContext(response) }

            val registerResponse = response.body()!!
            return@runBlocking Pair(registerResponse.auth_token, randomUsername)
        }
    }

    private fun createDevice(authToken: UUID): Device {
        return runBlocking {
            val response = apiService.createDevice(authToken.toString(), CreateDeviceRequest(1000.0))
            assert(response.isSuccessful) { printErrorContext(response) }

            val device = response.body()!!
            return@runBlocking device
        }
    }

    private fun createTask(
        authToken: UUID,
        device: Device,
    ): Task {
        return runBlocking {
            val response =
                apiService.createTask(
                    authToken.toString(),
                    CreateTaskRequest(Timespan("2024-04-04T14:13:14.587Z", "2024-04-04T15:13:14.587Z"), 30 * 60 * 1000, device.id),
                )
            assert(response.isSuccessful) { printErrorContext(response) }

            val task = response.body()!!
            return@runBlocking task
        }
    }

    private fun <T> printErrorContext(response: Response<T>): String {
        val bodyString =
            if (response.errorBody() == null) {
                "NULL"
            } else {
                response.errorBody()!!.string()
            }
        return "Status: ${response.code()}\n" +
            "Message: ${response.message()}\n" +
            "Body: $bodyString"
    }
}
