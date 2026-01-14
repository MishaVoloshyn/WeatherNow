package site.sunmeat.weathernow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import site.sunmeat.weathernow.worker.OdesaWeatherWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val work = OneTimeWorkRequestBuilder<OdesaWeatherWorker>().build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
