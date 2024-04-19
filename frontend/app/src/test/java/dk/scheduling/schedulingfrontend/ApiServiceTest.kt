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
import java.time.LocalDateTime
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
                apiService.getAllTasks(
                    authToken.toString(),
                )
            assert(emptyResponse.isSuccessful) { printErrorContext(emptyResponse) }

            val emptyTasks = emptyResponse.body()!!.tasks

            assert(emptyTasks.isEmpty())

            val task = createTask(authToken, device)

            val taskResponse = apiService.getAllTasks(authToken.toString())
            assert(taskResponse.isSuccessful) { printErrorContext(taskResponse) }

            val fullTasks = taskResponse.body()!!.tasks
            assert(fullTasks.size == 1)

            val gottenTask = fullTasks.single()

            assertEquals(task, gottenTask)
        }
    }

    @Test
    fun testGetAllEvents() {
        runBlocking {
            val (authToken, _) = createAccount("get_events_test")

            val response = apiService.getAllEvents(authToken.toString())
            assert(response.isSuccessful) { printErrorContext(response) }
        }
    }

    @Test
    fun testDeleteTask() {
        runBlocking {
            val (authToken, _) = createAccount("delete_task_test")
            val device = createDevice(authToken)
            val task = createTask(authToken, device)

            val deleteResponse = apiService.deleteTask(authToken.toString(), task.id)
            assert(deleteResponse.isSuccessful) { printErrorContext(deleteResponse) }

            val getTasksResponse = apiService.getAllTasks(authToken.toString())
            assert(getTasksResponse.isSuccessful) { printErrorContext(getTasksResponse) }

            val tasks = getTasksResponse.body()!!.tasks
            assert(tasks.isEmpty())
        }
    }

    @Test
    fun testCreateDevice() {
        runBlocking {
            val (authToken, _) = createAccount("create_task_test")

            val device = createDevice(authToken)
        }
    }

    @Test
    fun testGetAllDevices() {
        runBlocking {
            val (authToken, _) = createAccount("get_all_devices_test")
            val device = createDevice(authToken)

            val response = apiService.getAllDevices(authToken.toString())
            assert(response.isSuccessful) { printErrorContext(response) }

            val devices = response.body()!!.devices
            assert(devices.size == 1)
        }
    }

    @Test
    fun testDeleteDevices() {
        runBlocking {
            val (authToken, _) = createAccount("delete_devices_test")
            val device = createDevice(authToken)

            val deleteDeviceResponse = apiService.deleteDevice(authToken.toString(), device.id)
            assert(deleteDeviceResponse.isSuccessful) { printErrorContext(deleteDeviceResponse) }

            val getDevicesResponse = apiService.getAllDevices(authToken.toString())
            assert(getDevicesResponse.isSuccessful) { printErrorContext(getDevicesResponse) }

            val devices = getDevicesResponse.body()!!.devices
            assert(devices.isEmpty())
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
            val response = apiService.createDevice(authToken.toString(), CreateDeviceRequest("test", 1000.0))
            assert(response.isSuccessful) { printErrorContext(response) }

            val device = response.body()!!.device
            return@runBlocking device
        }
    }

    private fun createTask(
        authToken: UUID,
        device: Device,
    ): Task {
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(1)

        return runBlocking {
            val response =
                apiService.createTask(
                    authToken.toString(),
                    CreateTaskRequest(Timespan(startTime, endTime), 30 * 60 * 1000, device.id),
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
