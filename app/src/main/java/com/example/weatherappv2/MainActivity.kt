package com.example.weatherappv2

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
lateinit var cityName:TextView
lateinit var description:TextView
lateinit var temperature:TextView
lateinit var humidity:TextView
lateinit var searchText:TextView
lateinit var searchButton:Button
class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cityName = findViewById(R.id.cityName)
        description = findViewById(R.id.description)
        temperature = findViewById(R.id.temperature)
        humidity = findViewById(R.id.humidity)
        searchText = findViewById(R.id.searchText)
        searchButton = findViewById(R.id.searchbutton)

        searchButton.setOnClickListener {
            fetchData()
        }

    }
    fun fetchData()
    {
        val ApiKey:String = "607992a1f15f6fe3b3c9f40df3fef1fc"
        val City: String = "${searchText.text}"
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=${City}&appid=$ApiKey"
        val request = Request.Builder()
            .url(url)
            .build()
        GlobalScope.launch(Dispatchers.IO)
        {
            try {
                val response: Response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null)
                {
                    val weatherData = parseData(responseBody)
                    displayWeatherData(weatherData)
                }
                else
                    displayErrorMessage()
            }
            catch (e:IOException)
            {
                displayErrorMessage()
            }
        }
    }

    private suspend fun displayErrorMessage() {
        withContext(Dispatchers.Main)
        {
            cityName.text = "error"
            temperature.text =""
            humidity.text = "Error"
            description.text= "Connection Failed"
        }
    }

    private fun parseData(responseBody: String):WeatherData {
        val json = JSONObject(responseBody)
        val cityName= json.getString("name")
        val main = json.getJSONObject("main")
        val temperature = main.getDouble("temp")
        val weatherArray = json.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val description = weatherObject.getString("description")
        val hum = main.getInt("humidity")

        return WeatherData(cityName,temperature,description,hum)
    }
private suspend fun displayWeatherData(weatherData: WeatherData){
    withContext(Dispatchers.Main)
        {
            cityName.text =  weatherData.name
            val decimalFormat = DecimalFormat("#.#")
            val tempInCels = weatherData.temperature?.minus(273)
            val tempInCelsFormatted = decimalFormat.format(tempInCels)
            temperature.text = "${tempInCelsFormatted} °C"
            description.text = "${weatherData.description}"
            humidity.text ="${weatherData.humidity}%"
        }
    }
}