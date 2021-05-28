import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'constants.dart';

class NativeTimer {
  Function onTick;
  Function onStop;
  MethodChannel channel;
  int secs = 0;
  bool started = false;

  NativeTimer(this.onTick) {
    if(Platform.isAndroid) {
      channel = MethodChannel(channelName);
      channel.setMethodCallHandler((call) {
        switch (call.method) {
          case tick:
            onTick(call.arguments);
            break;
          case stop:
            onStop();
            break;
        }
      });
    }
  }

  void startTimer() {
    if (Platform.isAndroid)
      channel.invokeMethod("startTimer");
    else {
      secs = 0;
      onTick(secs);
      runTimer();
    }
    started = true;
  }

  void stopTimer() {
    if(Platform.isAndroid)
      channel.invokeMethod("stopTimer");
    else
      started = false;
  }

  runTimer(){
    Timer.periodic(Duration(seconds: 1), (timer) {
      secs++;
      if(!started) {
        timer.cancel();
        onTick(0);
      } else {
        onTick(secs);
      }
    });
  }

  static String getTimeStr(int secs) {
    var hrs = secs ~/ (60 * 60);
    var mins = (secs % (60 * 60)) ~/ 60;
    var secsR = (secs % (60 * 60)) % 60;
    return "${(hrs > 9) ? hrs : "0" + hrs.toString()} : ${(mins > 9)
        ? mins
        : "0" + mins.toString()} : ${(secsR > 9) ? secsR : "0" +
        secsR.toString()}";
  }
}