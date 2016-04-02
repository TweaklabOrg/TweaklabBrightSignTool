Library "tools_messaging.brs"
Library "tools_setup.brs"
Library "tools_tcp.brs"

sub main()
    ' size of the connections pool
    MAX_CONNECTIONS = 10

    ' deviceInfo contains infos about the player's model, his features and the installed firmware.
    deviceInfo = createObject("roDeviceInfo")

    ' test firmware compatibility
    miniumFirmwareVersionAsNumber = 6*65536 + 0*256 + 00
    if deviceInfo.GetVersionNumber() < miniumFirmwareVersionAsNumber then
        ScreenMessage("Firmware Version 6.0.0 or higher needed...", 3000) ' from tools_messaging.brs
        while true
        end while
    end if

    ' the syslog that will be used to log
    m.sysLog = createObject("roSystemLog")

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


	if createObject("roRegistry").GetSectionList().Count() > 0 then 
		' activate bonjour advertisement to delete it from other caches
	    props = { name: netConf.GetHostName(), type: "_tl._tcp", port: int(val(settings.tcp_port.getText())), _serial: deviceInfo.GetDeviceUniqueId() }
    	advert = CreateObject("roNetworkAdvertisement", props)
    	advert = invalid

	    deviceCust = CreateObject("roDeviceCustomization")
	    deviceCust.FactoryReset("confirm")
	else 
		screenContent = ScreenMessage("Player resetted. You can upload your configuration now.", 1000)

	    ' write default registry
	    WriteDefaultRegistry(settings) 

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
	end if
end sub

