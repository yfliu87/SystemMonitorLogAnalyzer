#!bin/bash
cd /home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer
date >> logtime.txt
/usr/lib/jvm/java-7-sun/bin/java -jar logIndexer.jar -cp /usr/lib/jvm/java-7-sun/lib/tools.jar:/usr/lib/jvm/java-7-sun/lib/dt.jar:/home/web-dev/Documents/eclipse-javaee/SystemMonitorAnalyzer/logIndexer.jar:. >> logtime.txt
