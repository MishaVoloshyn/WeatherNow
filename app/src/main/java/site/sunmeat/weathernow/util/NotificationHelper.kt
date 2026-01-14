package site.sunmeat.weathernow.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import site.sunmeat.weathernow.ForecastActivity
import site.sunmeat.weathernow.R

object NotificationHelper {

    private const val CHANNEL_ID = "weather_updates"
    private const val CHANNEL_NAME = "Weather updates"

    private const val NOTIF_ID = 7777

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    fun showTestNotification(
        context: Context,
        city: String,
        lat: Double,
        lon: Double,
        tempText: String,
        descText: String
    ) {
        ensureChannel(context)

        val intent = Intent(context, ForecastActivity::class.java).apply {
            putExtra(ForecastActivity.EXTRA_CITY_NAME, city)
            putExtra(ForecastActivity.EXTRA_LAT, lat)
            putExtra(ForecastActivity.EXTRA_LON, lon)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(context, 777, intent, flags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_cloud)
            .setContentTitle("WeatherNow: $city")
            .setContentText("$tempText • $descText (tap to open)")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
    }
}
