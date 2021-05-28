package com.example.timer

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.json.JSONObject

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        TimerService.registerChannel(flutterEngine,this)
    }

    override fun onResume() {
        super.onResume()
        if(TimerService.timerStatus == TimerService.TimerStatus.RUNNING)
            TimerService.makeBackground(context)
    }

    override fun onPause() {
        super.onPause()
        if(TimerService.timerStatus == TimerService.TimerStatus.RUNNING)
            TimerService. makeForeGround(context)
    }
}
