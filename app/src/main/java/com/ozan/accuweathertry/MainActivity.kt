package com.ozan.accuweathertry

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class MainActivity : AppCompatActivity(), LocationListener {

    val accuApiKey = "6RLKPwCWFbOBxgh8Y7iW0DnFkPckfOIr"

    private lateinit var edittext : EditText
    private lateinit var button : Button
    private lateinit var textView : TextView
    private lateinit var imageView : ImageView
    private lateinit var spinner : Spinner

    var locationManager : LocationManager? = null

    var globalLatitute = "0.0"
    var globalLongitute = "0.0"

    var selectedCityKey = ""

    var girisTuru: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edittext = findViewById(R.id.autoComplete)
        button = findViewById(R.id.button)
        textView = findViewById(R.id.textview)
        imageView = findViewById(R.id.imageview)
        spinner  = findViewById(R.id.spinner)



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100);
        }
        else {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 , 10F , this)
        }

        button.setOnClickListener { view: View? ->

            hideKeyboard()
            val str = edittext.text.toString()
            getAutocomplete(str)
        }


        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {

                var selected = girisTuru[position]

                val value = getItemAfterHypen(selected)
                val key   = getItemBeforeHypen(selected)

                edittext.setText(value)
                getWeatherInfo(key)

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action

                Toast.makeText(this@MainActivity, "Hata", Toast.LENGTH_SHORT).show()
            }
        }



    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0 , 0F , this@MainActivity)

        }


    }

    fun getAutocomplete(input : String){

        val destinationService  = ServiceBuilder.buildService(Service::class.java)
        val requestCall =destinationService.getAutoComplete(accuApiKey,"tr-TR",input)

        requestCall.enqueue(object : Callback<List<AutoSearchModel>> {

            override fun onFailure(call: Call<List<AutoSearchModel>>?, t: Throwable?) {

                Toast.makeText(this@MainActivity, t?.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<AutoSearchModel>>?,
                response: Response<List<AutoSearchModel>>?
            ) {

                if (response != null && response.isSuccessful && response.body() != null) {

                    var listAuto: List<AutoSearchModel>? = response.body()


                    girisTuru = ArrayList()

                    if (listAuto != null) {
                        for (auto in listAuto){

                            girisTuru.add(auto.Key + "-" + auto.LocalizedName)
                        }
                    }


                    val adapter = ArrayAdapter(this@MainActivity,
                            R.layout.spinner_item, girisTuru)

                        spinner.adapter = adapter



                    if (girisTuru.isNotEmpty()) {
                        spinner.performClick()
                    }
                    else{
                        Toast.makeText(this@MainActivity, "No City found", Toast.LENGTH_SHORT).show()
                    }

            }

        }

    })
    }

    fun getWeatherInfo(input: String){

        val destinationService  = ServiceBuilder.buildService(Service::class.java)
        val requestCall =destinationService.getWeather(input,"tr-TR",accuApiKey)

        selectedCityKey = input

        requestCall.enqueue(object : Callback<List<GetWeatherModel>> {

            override fun onFailure(call: Call<List<GetWeatherModel>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()

            }


            override fun onResponse(
                call: Call<List<GetWeatherModel>>,
                response: Response<List<GetWeatherModel>>
            ) {
                if (response != null && response.isSuccessful && response.body() != null) {

                    var listWeatherModel: List<GetWeatherModel>? = response.body()

                    if (listWeatherModel != null) {
                        if (listWeatherModel.isNotEmpty()){

                            val weather = listWeatherModel[0]

                            val valu = weather.Temperature?.Metric?.Value
                            val exp = weather.WeatherText
                            val icon = weather.WeatherIcon.toString()

                            textView.setText(valu.toString() + " " + exp)

                            val path = "icon$icon"

                            val pathId = resources.getIdentifier(path, "drawable", packageName)

                            if (pathId != 0) {
                                Picasso.get().load(pathId).into(imageView)
                            }
                        }
                    }

                }


            }


        })

    }

    fun AppCompatActivity.getString(name: String): String {
        return resources.getString(resources.getIdentifier(name, "string", packageName))
    }

    override fun onLocationChanged(p0: Location?) {


        val latitute = p0?.latitude ?: 0.0
        val longitute = p0?.longitude ?: 0.0

        globalLatitute = latitute.toString()
        globalLongitute = longitute.toString()

        String()


        if (selectedCityKey.isEmpty()){

            var filter = "{latitude},{longitude}"
            filter = filter.replace("{latitude}", globalLatitute)
            filter = filter.replace("{longitude}", globalLongitute)

            val destinationService  = ServiceBuilder.buildService(Service::class.java)
            val requestCall =destinationService.getCityKeyLoc(accuApiKey,"tr-TR",filter)

            requestCall.enqueue(object : Callback<AutoSearchModel>{

                override fun onFailure(call: Call<AutoSearchModel>, t: Throwable) {

                    Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }



                override fun onResponse(
                    call: Call<AutoSearchModel>,
                    response: Response<AutoSearchModel>
                ) {

                    var listResponse: AutoSearchModel? = response.body()

                    if (listResponse != null){

                        val key = listResponse.Key
                        val name = listResponse.LocalizedName

                        edittext.setText(name)
                        getWeatherInfo(key)

                    }


                }


            })



        }

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null && currentFocus!!.windowToken != null) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }


    }

    fun getItemBeforeHypen(word : String) : String {

        var wordDivided = word
        val splitCharacter = "-"

        if (wordDivided.contains(splitCharacter)){

            val strs = wordDivided.split(splitCharacter).toTypedArray()

            if (strs.isNotEmpty()){
                wordDivided = strs[0]
            }
        }
        return wordDivided
    }

    fun getItemAfterHypen(word : String) : String {

        var wordDivided = word
        val splitCharacter = "-"

        if (wordDivided.contains(splitCharacter)){

            val strs = wordDivided.split(splitCharacter).toTypedArray()

            if (strs.isNotEmpty() && strs.size > 1){
                wordDivided = strs[1]
            }
        }
        return wordDivided
    }
}

//Interface to call service
interface Service {

    @GET("/locations/v1/cities/autocomplete?")
    fun getAutoComplete (@Query("apikey") apiKey : String,@Query("language") language : String, @Query("q") q : String) : Call<List<AutoSearchModel>>

    @GET("locations/v1/cities/geoposition/search?")
    fun getCityKeyLoc (@Query("apikey") apiKey : String,@Query("language") language : String, @Query("q") q : String) : Call<AutoSearchModel>

    @GET("currentconditions/v1/{key}")
    fun getWeather (@Path("key") key : String, @Query("language") language : String, @Query("apikey") apiKey : String) : Call<List<GetWeatherModel>>
}

//REtrofit Builder
object ServiceBuilder {
    private const val URL ="http://dataservice.accuweather.com/"
    //CREATE HTTP CLIENT
    private val okHttp = OkHttpClient.Builder()

    //retrofit builder
    private val builder = Retrofit.Builder().baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp.build())

    //create retrofit Instance
    private val retrofit = builder.build()

    //we will use this class to create an anonymous inner class function that
    //implements Country service Interface


    fun <T> buildService (serviceType :Class<T>):T{
        return retrofit.create(serviceType)
    }

}