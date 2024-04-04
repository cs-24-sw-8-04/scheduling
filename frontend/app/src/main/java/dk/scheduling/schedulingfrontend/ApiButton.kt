package dk.scheduling.schedulingfrontend

import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dk.scheduling.schedulingfrontend.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ApiButton() {
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
