#!/bin/bash
# *********************************
#     Run Tweaklab Player Client
# *********************************

JAVA="/appbase/java/jdk1.8.0_45/bin/java"

LIB="lib/*"

#Create Classpath

for f in $LIB;
do
   CLASSPATH=$CLASSPATH:$f
done
CLASSPATH=`echo $CLASSPATH | cut -c2-`


java -cp "$CLASSPATH:config.properties:tl-player.jar" ch.tweaklab.player.gui.controller.MainApp 

