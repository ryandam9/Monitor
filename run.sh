#!/bin/sh

mvn clean install
cp ./target/JobMonitor-1.0-SNAPSHOT.jar ~/Desktop/monitor/  
cp -r src/main/resources ~/Desktop/monitor

cd ~/Desktop/monitor
java --module-path $PATH_TO_FX --add-modules javafx.controls --add-modules javafx.fxml -jar ./JobMonitor-1.0-SNAPSHOT.jar
