package com.example.networkeventcallback

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName
    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var fileNameTv: TextView
    private lateinit var progressTv: TextView
    private lateinit var tvDownloadUnit: TextView
    private lateinit var btnCancelAndDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        progressBar = findViewById(R.id.progressBar)
        fileNameTv = findViewById(R.id.tvFileName)
        progressTv = findViewById(R.id.tvProgress)
        tvDownloadUnit = findViewById(R.id.tvDownloadUnit)
        btnCancelAndDelete = findViewById(R.id.btnCancelAndDelete)

        val download = DownloadClient.Builder()
            .addDownloadUrl("https://download-installer.cdn.mozilla.net/pub/firefox/releases/114.0.1/mac/en-US/Firefox%20114.0.1.dmg")
            .addDownloadFileDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
            .addDownloadCallback(object : DownloadCallback {
                override fun onSuccess(downloadedFile: File) {
                    Log.d(MainActivity::class.java.simpleName, "download successfully")
                }

                override fun onFailure(failureReason: String) {
                    Log.d(MainActivity::class.java.simpleName, failureReason)
                }

                override fun onProgressUpdate(progressData: ProgressData) {
                    runOnUiThread {
                        fileNameTv.text = progressData.fileName
                        progressTv.text = progressData.progress.toString().plus("%")
                        progressBar.progress = progressData.progress.toInt()
                        tvDownloadUnit.text = progressData.downloadTillLength.plus(" / ").plus(progressData.totalFileLength)
                    }
                }
            })
            .build()

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            download.startDownload()
        }
        findViewById<Button>(R.id.btnPause).setOnClickListener {
            download.pauseDownload()
        }
        findViewById<Button>(R.id.btnResume).setOnClickListener {
            download.resumeDownload()
        }
    }


    /*    private fun networkRequestCallback(context: Context) {


            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "networkRequestCallback: onAvailable $network")
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (null != networkCapabilities) {
                        Log.d(TAG, "networkRequestCallback: onAvailable isEthernetConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        Log.d(TAG, "networkRequestCallback: onAvailable isWIFIConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        Log.d(TAG, "networkRequestCallback: onAvailable isCellularConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            (networkCapabilities?.transportInfo as WifiInfo)
                        }
                    }
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    if (null != linkProperties) {


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            (networkCapabilities?.transportInfo as WifiInfo).macAddress
                        } else {

                        }

                        Log.d(TAG, "networkRequestCallback: onAvailable " + linkProperties.interfaceName)
                    }
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "networkRequestCallback: onLost $network")
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (null != networkCapabilities) {
                        Log.d(TAG, "networkRequestCallback: onLost isEthernetConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        Log.d(TAG, "networkRequestCallback: onLost isWIFIConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        Log.d(TAG, "networkRequestCallback: onLost isCellularConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    }
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    if (null != linkProperties) {
                        Log.d(TAG, "networkRequestCallback: onLost " + linkProperties.interfaceName)
                    }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    Log.d(TAG, "networkRequestCallback: onCapabilitiesChanged $network")
                    Log.d(TAG, "networkRequestCallback: onCapabilitiesChanged isEthernetConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    Log.d(TAG, "networkRequestCallback: onCapabilitiesChanged isWIFIConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    Log.d(TAG, "networkRequestCallback: onCapabilitiesChanged isCellularConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    if (null != linkProperties) {
                        Log.d(TAG, "networkRequestCallback: onCapabilitiesChanged " + linkProperties.interfaceName)
                    }
                }

                override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                    Log.d(TAG, "networkRequestCallback: onLinkPropertiesChanged $network")
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (null != networkCapabilities) {
                        Log.d(TAG, "networkRequestCallback: onLinkPropertiesChanged isEthernetConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        Log.d(TAG, "networkRequestCallback: onLinkPropertiesChanged isWIFIConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        Log.d(TAG, "networkRequestCallback: onLinkPropertiesChanged isCellularConnected " + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    }
                    Log.d(TAG, "networkRequestCallback: onLinkPropertiesChanged " + linkProperties.interfaceName)
                }
            })
        }*/
}
