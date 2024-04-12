package dk.scheduling.schedulingfrontend.pages

import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dk.scheduling.schedulingfrontend.api.getApiClient
import kotlinx.coroutines.launch

@Composable
fun ApiButton() {
    val coroutineScope = rememberCoroutineScope()
    val apiService = getApiClient("http://10.0.2.2:2222")

    Button(onClick = {
        coroutineScope.launch {
            try {
                val response = apiService.getAllTasks("NOT_VALID")

                if (response.isSuccessful) {
                    // val post = response.body()
                    Log.i("testAPI", "we got a response")
                    // Handle the retrieved post data
                } else {
                    Log.w("testAPI", "we did not get a successful response")
                    Log.w("testAPI", response.message())
                    // Handle error
                }
            } catch (e: Exception) {
                Log.e("testApi", e.toString())
                throw e
            }
        }
    }) {
        Text("Test API")
    }
}
