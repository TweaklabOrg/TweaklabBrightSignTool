Library "gpio.brs"
Library "playlist.brs"
Library "tools_setup.brs"
Library "tools_messaging.brs"
Library "tools_tcp.brs"

' Tweaklab Custom BrightScript for BrightSign Players
' ---------------------------------------------------
'
' This Script is responsible to bring a Bright Sign Player in a valid state and to run the script chosen with the <mode> setting. 
' In order to do that, it parses the settings.xml and display.xml, adapts registry settings and creates objects that are
' used by the scripts anyway.
'
' Version 1.0
' Stephan Brunner
' Tweaklab
' 11.09.2015

sub tweaklabPlayer()
    m.DEBUG = true ' will be set again as soon as settings.xml is read. Should be true to make it possible to show an error if settings.xml is not parsable.

    ' deviceInfo contains infos about the player's model, his features and the installed firmware.
    deviceInfo = createObject("roDeviceInfo")

    ' screenContent is used to store the displayed content while other jobs are done. Show simple header until device info is collected and shown at the end of the script.
    screenContent = SimpelHeader()
    screenContent.show()

    ' the syslog that will be used to log
    m.sysLog = createObject("roSystemLog")

    ' size of the connections pool
    MAX_CONNECTIONS = 10

    ' generate a AssociativeArray (aka Hash map) out of the settings.xml. Quit script if not available
    settings = CreateObject("roXMLElement")
    if not settings.parseFile("/settings.xml") then
        info("not able to parse settings.xml script stopped. verify or reset configuration.")
        screenContent = ScreenMessage("not able to parse settings.xml. script stopped. verify or reset configuration.", 1000) ' from tools_messaging.brs

        while true
        end while
    end if

    ' generate a AssociativeArray (aka Hash map) out of the mode.xml. Quit script if not available
    mode = CreateObject("roXMLElement")
    if not mode.parseFile("/mode.xml") then
        info("not able to parse mode.xml script stopped. verify or reset configuration.")
        screenContent = ScreenMessage("not able to parse mode.xml. script stopped. verify or reset configuration.", 1000) ' from tools_messaging.brs

        while true
        end while
    end if

    if settings.debug.getText() = "true" then
        m.DEBUG = true
    else
        m.DEBUG = false
    end if

    info("------- TWEAKLAB Custom BrightScript Version " + settings.scriptVersion.getText() + " -------")
    info("")

    ' test firmware compatibility
    miniumFirmwareVersionAsNumber = 6*65536 + 0*256 + 25
    if deviceInfo.GetVersionNumber() < miniumFirmwareVersionAsNumber then
        info("FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.")
        info("")
        ScreenMessage("FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.", 3000) ' from tools_messaging.brs
    end if
    minimumBootFirmwareVersionAsNumber = 4*65536 + 9*256 + 29
    if deviceInfo.GetBootVersionNumber() < minimumBootFirmwareVersionAsNumber then
        info("BOOT-FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.")
        info("")
        ScreenMessage("BOOT-FIRMWAREVERSION NOT SUPPORTED. ISSUES MAY OCCURE.", 3000) ' from tools_messaging.brs
    end if

    ' Version 6.0.51 changed the server communication and is - for now - not supporting uploading via our the Tweaklab BrightSign Tool
    unsupportedFirmwareVersion = 6*65536 + 0*256 + 51
    if deviceInfo.GetVersionNumber() = unsupportedFirmwareVersion then
        info("Version 6.0.51 is currently not supported. Downgrade to 6.0.25.")
        info("")
        ScreenMessage("Version 6.0.51 is currently not supported. Downgrade to 6.0.25.", 3000) ' from tools_messaging.brs
    end if

    ' write default content to registry if registry is empty
    if createObject("roRegistry").GetSectionList().Count() = 0 then    
        WriteDefaultRegistry(settings) ' from tools_setup.brs
    end if

    ' if tweaklab registry isn't available, factory reset, as player seems not to be set up as tweaklab player.
    registrylist = createObject("roRegistry").GetSectionList()
    registrylist.Reset() ' unfortunately has to be reset manually as it seemed
    found = false
    while registrylist.IsNext()
        if registrylist.Next() = "tweaklab" then
            found = true
        end if
    end while
    if not found then
        info("Tweaklab settings not available. Setting player to initial state.")
        ScreenMessage("Tweaklab settings not available. Setting player to initial state.", 3000) ' from tools_messaging.brs

        info("rebooting...")
        ' store the next message in a variable, to make it visible until the player reboots.
        temp = ScreenMessage("rebooting...", 1000)

        ' set device back to factory configuration
        deviceCust = CreateObject("roDeviceCustomization")
        deviceCust.FactoryReset("confirm")
    end if

    ' a reboot might be necessary depending on changes. In this case this variable can be set to true and the reboot 
    ' will be executed when all settings are up to date.
    reboot = false

    ' If display.xml changed and Player can play video, update settings. needs a reboot
    if deviceInfo.HasFeature("hdmi") or deviceInfo.HasFeature("vga") or deviceInfo.HasFeature("component video") then
        tweaklabRegistry = CreateObject("roRegistrySection", "tweaklab")
        if UpdateDisplaySettings(tweaklabRegistry) = true then ' method from tools_setup.brs
            reboot = true
        end if
    end if

    ' Compare debug mode with settings and change mode if necessary.
    if updateDebugSettings(settings) then
        reboot = true
    end if

    ' If network settings changed, update settings.
    '
    ' Doesn't need a reboot but must be before the rebootSystem() to have the right network settings set 
    ' after the reboot. They might be used. For example if someone wants to connect via ssh.
    UpdateNetworkSettings(settings) ' method from tools_setup.brs

    if reboot then
        info("rebooting...")
        ' store the next message in a variable, to make it visible until the player reboots.
        temp = ScreenMessage("rebooting...", 1000)
        rebootSystem()
    end if

    ' setup tcp server
    server = createObject("roTCPServer")
    connections = createObject("roArray", MAX_CONNECTIONS, false)
    server.bindToPort(int(val(settings.tcp_port.getText())))

    ' Fill connections pool with roTCPStreams
    for i = 1 to MAX_CONNECTIONS step +1
        connections.push(newConnection()) ' newConnection() from tools_tcp.brs
    end for

    ' activate bonjour advertisement
    props = { name: settings.name.getText(), type: "_tl._tcp", port: int(val(settings.tcp_port.getText())), _serial: deviceInfo.GetDeviceUniqueId() }
    advert = CreateObject("roNetworkAdvertisement", props)

    ' shoe device info
    ' screenContent = invalid
    ' sleep(500)
    screenContent = DeviceInfos() ' from tools_messaging.brs
    screenContent.show()
    sleep(10000) ' show Diagnostic screen for ... milliseconds
    screenContent = invalid


    ' start script chosen with the <mode> setting
    if mode.getText() = "gpio" then
        gpioMain(settings, server, connections)
    else if mode.getText() = "playlist" then
        playlistMain(settings, server, connections)
    end if
end sub