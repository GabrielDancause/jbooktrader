echo off
set appHome=..
set javaHome=C:/WINDOWS/system32/

set cp=%appHome%
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/resources
set cp=%cp%;%appHome%/lib/ibapi-9.63.jar
set cp=%cp%;%appHome%/lib/jcommon-1.0.16.jar
set cp=%cp%;%appHome%/lib/jfreechart-1.0.13.jar
set cp=%cp%;%appHome%/lib/liquidlnf.jar

set mainClass=com.jarbitrager.platform.startup.JArbitrager

%javaHome%javaw.exe -cp "%cp%" -Xmx4000M %mainClass% "%appHome%"


