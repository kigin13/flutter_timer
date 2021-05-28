package com.example.timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TimerService : Service() {
    var notificationBuilder: NotificationCompat.Builder? = null
    companion object {
        val channelName = "com.ss.timer"
        val SERVICE_CHANNEL = "timer_service_channel"
        val FOREGROUND_SERVICE_ID = 2
        var START_TIMER = "START_TIMER"
        var STOP_TIMER = "STOP_TIMER"
        var MAKE_FOREGROUND = "MAKE_FOREGROUND"
        var MAKE_BACKGROUND = "MAKE_BACKGROUND"
        var isForground = false;
        var TICK = "onTick"
        var timerStatus = TimerStatus.NOTRUNNING
        var secs = 0;
        var timer = Timer()
        var channel: MethodChannel? = null
        fun startTimer(context: Context) {
            if (timerStatus != TimerStatus.RUNNING) {
                val intent = Intent(context, TimerService::class.java)
                intent.action = START_TIMER
                context.startService(intent)
            }
        }

        fun stopTimer(context: Context) {
            if (timerStatus == TimerStatus.RUNNING) {
                val intent = Intent(context, TimerService::class.java)
                intent.action = STOP_TIMER
                context.startService(intent)
            }
        }

        fun makeBackground(context: Context){
            val intent = Intent(context, TimerService::class.java)
            intent.action = MAKE_BACKGROUND
            context.startService(intent)
        }

        fun makeForeGround(context: Context){
            val intent = Intent(context, TimerService::class.java)
            intent.action = MAKE_FOREGROUND
            context.startService(intent)
        }

        fun registerChannel(flutterEngine: FlutterEngine,context: Context) {
            channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            channel?.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result? ->
                if (call.method == "startTimer") {
                    startTimer(context)
                } else {
                    stopTimer(context)
                }
            }
        }
    }

    fun setTime(){
        try {
            notificationBuilder?.setContentText("${getTimeStr()}")
            val notification = notificationBuilder?.build();
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(FOREGROUND_SERVICE_ID, notification);
        } catch (e: Exception) {

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when(it){
                START_TIMER -> {
                    secs = 0
                    brordCostTime()
                    timerStatus = TimerStatus.RUNNING
                    timer = fixedRateTimer(initialDelay = 0, period = 1000, action = {
                         secs++
                        brordCostTime()
                    })
                }
                STOP_TIMER -> {
                    secs = 0
                    brordCostTime()
                    timerStatus = TimerStatus.NOTRUNNING
                    timer.cancel()
                    stopService()
                }
                MAKE_FOREGROUND -> {
                    makeForground()
                }
                MAKE_BACKGROUND -> {
                    makeBackground()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun brordCostTime() {
        Handler(Looper.getMainLooper()).post {
            if(isForground)
                setTime()
            channel?.invokeMethod(TICK,secs)
        }
    }

    fun getTimeStr():String{
        var hrs = secs/(60*60)
        var mins = (secs%(60*60))/60
        var secsR = (secs%(60*60))%60
        return "${if(hrs>9)hrs else "0"+hrs} : ${if(mins>9)mins else "0"+mins} : ${if(secsR>9)secsR else "0"+secsR}"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    private fun makeForground() {
        if(timerStatus != TimerStatus.RUNNING)
            return
        try {
            createNotificationChannel(
                    SERVICE_CHANNEL,
                    "Foreground Service"
            )
            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.action = java.lang.Long.toString(System.currentTimeMillis())
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder =
                    NotificationCompat.Builder(this, SERVICE_CHANNEL)
                            .setContentTitle("Timer")
                            .setContentText("Secs : ${secs}")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOngoing(true)
                            .setSound(null)
                            .setPriority(Notification.PRIORITY_HIGH)
                            /*.addAction(
                                R.mipmap.ic_launcher,
                                "CLICKBLE",
                                turnOffPendingIntent
                            )*/
            startForeground(FOREGROUND_SERVICE_ID, notificationBuilder!!.build())
            isForground = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(channelId: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId, name,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) != channel)
                manager?.createNotificationChannel(channel)
        }
    }

    private fun makeBackground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stopForeground(true)
        }
        isForground = false
    }

    private fun stopService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stopForeground(true)
            stopSelf()
        }
        isForground = false
    }

    inner class MyBinder : Binder() {
        val service: TimerService
            get() = this@TimerService
    }

    enum class TimerStatus {
        RUNNING,
        NOTRUNNING,
        PAUSE
    }
}