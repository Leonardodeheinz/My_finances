package com.example.my_finances

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.my_finances.notification.NotificationChannelManager
import com.example.my_finances.worker.ReminderWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyFinancesApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    @Inject
    lateinit var reminderWorkScheduler: ReminderWorkScheduler

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        notificationChannelManager.createNotificationChannels()

        // Schedule periodic reminder worker
        reminderWorkScheduler.schedulePeriodicReminders()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
