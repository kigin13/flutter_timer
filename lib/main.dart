import 'package:flutter/material.dart';
import 'package:timer/NativeTimer.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Timer',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  NativeTimer timer = null;
  var secs = 0;
  bool started = false;
  @override
  void initState() {
    timer = NativeTimer((secs){
      setState(() {
        this.secs = secs as int;
        if(this.secs>0)
          started = true;
      });
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.white,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Text(
            "Timer",
            style: TextStyle(
              fontSize: 18,
              color: Colors.black,
              decoration: TextDecoration.none
            ),
          ),
          Container(
            margin: EdgeInsets.only(top: 50,bottom: 50),
            child: Text(
              "${NativeTimer.getTimeStr(secs)}",
              style: TextStyle(
                  fontSize: 32,
                  color: Colors.black,
                  decoration: TextDecoration.none
              ),
            ),
          ),
          ElevatedButton(
              onPressed: () {
                if(started){
                  timer.stopTimer();
                } else {
                  timer.startTimer();
                }
                setState(() {
                  started = !started;
                });
              },
              style: ButtonStyle(
                backgroundColor: MaterialStateProperty.all(Colors.blue),
              ),
              child: Text(
                started?"Stop Timer":"Start Timer",
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w500,
                  fontSize: 20,
                ),
              ))
        ],
      ),
    );
  }
}
