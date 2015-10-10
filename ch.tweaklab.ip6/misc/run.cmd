
@echo off

REM ##############################################
REM # FIS batch program to load scanned customer dossier
REM ##############################################
SET ENV=TEST
SET JAVA_HOME=C:/programme/Java/jdk1.8.0_45
SET FIS_HOME=C:/programme/FIS/FISscanning_%ENV%

REM #--------------------
REM # Java libs
REM #--------------------
SET JAVA_CP=%FIS_HOME%/lib/ch.allianz.adse.client-1.0.0.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/ch.allianz.auth.client.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/ch.allianz.filenet.safenet.client-1.0.0.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/ch.allianz.wis.base.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/ch.allianz.wis.jsf.base-1.0.0.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/ch.allianz.fis_client.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/commons-codec-1.3.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/commons-compress-1.4.1.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/commons-httpclient-3.1.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/commons-logging-1.1.1.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/db2jcc.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/db2jcc_license_cisuz.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/db2jcc_license_cu.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/itextpdf-5.2.1.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/log4j-1.2.15.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/mailapi.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/lib/smtp.jar
SET JAVA_CP=%JAVA_CP%;%FIS_HOME%/config