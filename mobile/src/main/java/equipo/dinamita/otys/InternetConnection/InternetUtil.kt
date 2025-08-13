package equipo.dinamita.otys.InternetConnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object InternetUtil {

    fun isInternetAvailable(context: Context?, callback: (Boolean) -> Unit) {
        if (context == null) {
            callback(false)
            return
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val hasNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return callback(false)
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return callback(false)
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }

        if (!hasNetwork) {
            callback(false)
            return
        }

        Thread {
            try {
                val url = java.net.URL("https://www.google.com")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.connect()
                callback(connection.responseCode in 200..399)
            } catch (e: Exception) {
                callback(false)
            }
        }.start()
    }
}


