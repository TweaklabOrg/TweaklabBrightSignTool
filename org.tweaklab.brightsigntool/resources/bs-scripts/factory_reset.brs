Library "tools_messaging.brs"
Library "tools_setup.brs"
Library "tools_tcp.brs"

sub main()
    ' size of the connections pool
    MAX_CONNECTIONS = 10

    ' deviceInfo contains infos about the player's model, his features and the installed firmware.
    deviceInfo = createObject("roDeviceInfo")

    ' the syslog that will be used to log
    m.sysLog = createObject("roSystemLog")

    ' test firmware compatibility
    miniumFirmwareVersionAsNumber = 6*65536 + 0*256 + 00
    if deviceInfo.GetVersionNumber() < miniumFirmwareVersionAsNumber then
        ScreenMessage("Firmware Version 6.0.0 or higher needed...", 3000) ' from tools_messaging.brs
        while true
        end while
    end if
    minimumBootFirmwareVersionAsNumber = 4*65536 + 4*256 + 15
    if deviceInfo.GetBootVersionNumber() < minimumBootFirmwareVersionAsNumber then
        info("BOOT-FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.")
        info("")
        ScreenMessage("BOOT-FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.", 3000) ' from tools_messaging.brs
    end if

	' generate a AssociativeArray (aka Hash map) out of the settings.xml. Quit script if not available
    settings = CreateObject("roXMLElement")
    if not settings.parseFile("/settings.xml") then
        info("not able to parse settings.xml script stopped. verify or reset configuration.")
        screenContent = ScreenMessage("not able to parse settings.xml. script stopped. verify or reset configuration.", 1000) ' from tools_messaging.brs
        while true
        end while
    end if

    ' get network configuration
    netConf = CreateObject("roNetworkConfiguration", 0)

    ' dummy videoplayer, needed for handleStreamLineEvent in tools_tcp.brs
    videoPlayer = CreateObject("roVideoPlayer")

	tweaklabRegistry = CreateObject("roRegistrySection", "tweaklab")

	if createObject("roRegistry").GetSectionList().Count() = 0 ' this is the case after a factory reset
	    ' write default registry
	    WriteDefaultRegistry(settings) 

	    ' mark player as resetted to avoid from beeing resetted again after next boot
    	tweaklabRegistry.Write("resetted", "yes")

    	' reboot system to make shure all changes are setup right
		rebootSystem()

	else if tweaklabRegistry.read("resetted") = "yes" then ' this is the case when reset is completed
		screenContent = ScreenMessage("Player resetted. You can upload your configuration now.", 1000)

	    ' activate bonjour advertisement
	    props = { name: netConf.GetHostName(), type: "_tl._tcp", port: int(val(settings.tcp_port.getText())), _serial: deviceInfo.GetDeviceUniqueId() }
    	advert = CreateObject("roNetworkAdvertisement", props)

	    ' setup tcp server
	    server = createObject("roTCPServer")
    	server.bindToPort(int(val(settings.tcp_port.getText())))

    	' build tcp connection pool
    	connections = createObject("roArray", MAX_CONNECTIONS, false)
	    for i = 1 to MAX_CONNECTIONS step +1
	        connections.push(newConnection()) ' newConnection() from tools_tcp.brs
	    end for

	   ' The messageport receives interrupts from linked objects and will be used to launch eventhandlers in the main loop below.
	    port = CreateObject("roMessagePort")
    	server.setport(port)

	    ' mark player as not resetted as now the player is ready to use again
    	tweaklabRegistry.Write("resetted", "no")

		while true
		    ' Wait for the next event. The occuring event will be stored in msg.
	        msg = wait(0, port)
	        ' roTCPConnectEvent signals, that a TCP connection request arrived at server object
	        if type(msg) = "roTCPConnectEvent" then
	            handleTCPConnectEvent(msg, port, connections)
	        ' roStremLineEvent signals, that a open connection recieved a sting, terminated with a LF (0x0A)
	        else if type(msg) = "roStreamLineEvent" then 
	            handleStreamLineEvent(msg, port, videoPlayer, settings)
	        ' roStreamEndEvent signals, that a connection has been closed by a client.
	        else if type(msg) = "roStreamEndEvent" then
	            handleStreamEndEvent(msg)
	        end if
		end while

	else ' this is the case when a unresetted brightsign player executes this script
		' activate bonjour advertisement to delete it from other caches
	    props = { name: netConf.GetHostName(), type: "_tl._tcp", port: int(val(settings.tcp_port.getText())), _serial: deviceInfo.GetDeviceUniqueId() }
    	advert = CreateObject("roNetworkAdvertisement", props)
    	advert = invalid

	    deviceCust = CreateObject("roDeviceCustomization")
	    deviceCust.FactoryReset("confirm")
	end if
end sub

