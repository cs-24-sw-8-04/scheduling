package dk.scheduling.schedulingfrontend

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import dk.scheduling.schedulingfrontend.api.ApiClient
import dk.scheduling.schedulingfrontend.ui.theme.SchedulingFrontendTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchedulingFrontendTheme {
                Button(onClick = {
                    val call = ApiClient.apiService.test()

                    call.enqueue(
                        object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>,
                            ) {
                                if (response.isSuccessful) {
                                    // val post = response.body()
                                    Log.i("testAPI", "we got a response")
                                    // Handle the retrieved post data
                                } else {
                                    Log.w("testAPI", "we did not get a successful response")
                                    // Handle error
                                }
                            }

                            override fun onFailure(
                                call: Call<String>,
                                t: Throwable,
                            ) {
                                // Handle failure
                                Log.e("testAPI", "could not get a response")
                            }
                        },
                    )
                }) {
                    Text("Test API")
                }
            }
        }
    }
}
