' @param settings The settings.xml converted into a roXMLElement.
Sub UpdateNetworkSettings(settings As Object)
    netConf = CreateObject("roNetworkConfiguration", 0)
    current = netConf.GetCurrentConfig()
    changed = false
    if settings.dhcp.getText() = "true" then
        if current.dhcp = false then
            info("changing dhcp to enabled")
            ScreenMessage("changing dhcp to enabled", 3000)
            netConf.setDHCP()
            changed = true
        end if
    else
        if current.ip4_address <> settings.ip.getText() then
            info("changing ip from " + current.ip4_address + " to " + settings.ip.getText())
            ScreenMessage("changing ip from " + current.ip4_address + " to " + settings.ip.getText(), 3000)
            netConf.setIP4Address(settings.ip.getText())
            changed = true
        end if
        if current.ip4_netmask <> settings.netmask.getText() then
            info("changing netmask from " + current.ip4_netmask + " to " + settings.netmask.getText())
            ScreenMessage("changing netmask from " + current.ip4_netmask + " to " + settings.netmask.getText(), 3000)
            netConf.setIP4Netmask(settings.netmask.getText())
            changed = true
        end if
        if current.ip4_gateway <> settings.gateway.getText() then
            info("changing gateway from " + current.ip4_gateway + " to " + settings.gateway.getText())
            netConf.setIP4Gateway(settings.gateway.getText())
            changed = true
        end if
    end if
    if netConf.getHostName() <> settings.name.getText() then
        info("changing name from " + netConf.getHostName() + " to " + settings.name.getText())
        ScreenMessage("changing name from " + netConf.getHostName() + " to " + settings.name.getText(), 3000)

        ' unpublish bonjour service under old name
        deviceInfo = createObject("roDeviceInfo")
        props = { name: netConf.GetHostName(), type: "_tl._tcp", port: int(val(settings.tcp_port.getText())), _serial: deviceInfo.GetDeviceUniqueId() }
        advert = CreateObject("roNetworkAdvertisement", props)
        advert = invalid

        netConf.setHostName(settings.name.getText())
        changed = true
    end if
    if changed then
        netConf.apply()
    end if
End Sub

Sub WriteDefaultRegistry(settings As Object)
    section = CreateObject("roRegistrySection", "networking")
    section.Write("ple", "yes") 'playbackLoggingEnabled'
    section.Write("ele", "yes") 'eventLoggingEnabled'
    section.Write("sle", "yes") 'stateLoggingEnabled'
    section.Write("dle", "yes") 'diagnosticLoggingEnabled'
    section.Write("uab", "no")  'uploadLogFilesAtBoot'
    section.Write("uat", "no")  'uploadLogFilesAtSpecificTime'
    section.Write("ut", "0")    'uploadLogFilesTime'

    ' enable diagnostic web server
    section.write("http_server", "80")

    ' mark player as "setup as tweaklab player"
    tweaklabRegistry = CreateObject("roRegistrySection", "tweaklab")
    tweaklabRegistry.Write("dummy", "yes")

    ' write changes
    section.flush()
    tweaklabRegistry.flush()
End Sub

' @param tweaklabRegistry The tweaklab Registry.
Function UpdateDisplaySettings(tweaklabRegistry as Object) as Object
    changed = false

    ' Quit script if display.xml is not available. 
    displaySettings = CreateObject("roXMLElement")
    if not displaySettings.parseFile("/display.xml") then
        info("Not able to parse display.xml. Stopping script. Verify or reset configuration.")
        screenContent = ScreenMessage("Not able to parse display.xml. Stopping script. Verify or reset configuration.", 1000)

        while true
        end while
    end if

    videoMode = CreateObject("roVideoMode")
    nextVideoMode = videoModeFromXML(displaySettings)

    ' Settings changed to auto-format?
    if displaySettings.auto.getText() = "true" and videoMode.getModeForNextBoot() <> "auto"
        videoMode.SetModeForNextBoot("auto")
        info("changing display settings to auto-format.")
        info("rebooting to make display settings taking effect. please reconnect after reboot!")
        ScreenMessage("changing display settings to auto-format.", 3000)
        tweaklabRegistry.write("resolutionValidity", "true")
        changed = true
    end if

    ' Auto-format is disabled and resolution Settings changed?
    if displaySettings.auto.getText() = "false" and videoMode.getMode() <> (nextVideoMode)
        if videoMode.SetModeForNextBoot(nextVideoMode) then
            info("changing display settings from " + videoMode.getMode() + " to " + nextVideoMode)
            info("rebooting to make display settings taking effect. please reconnect after reboot!")
            ScreenMessage("changing display settings from " + videoMode.getMode() + " to " + nextVideoMode + ".", 3000)
            tweaklabRegistry.write("resolutionValidity", "true")
        else
            videoMode.SetModeForNextBoot("auto") ' make sure error message can be displayed after next boot.    
            tweaklabRegistry.write("resolutionValidity", "false")

            info("Configured resolution not supported. Rebooting to show message on screen in auto-format mode.")
        end if
        changed = true
    end if

    ' This part of the code will be executed, if the resolution settings in display.xml are not compatible with the used 
    ' player and the player already tried to use those settings and was rebooted because of that issue. To communicate 
    ' that issue we need to use the screen, but as it would be set to an unknown resolution, we where setting the videoMode
    ' to "auto" before rebooting and are now ready to show the message and stop execution of the script. 
    if tweaklabRegistry.read("resolutionValidity") = "false" then
        info("CONFIGURED RESOLUTION IS NOT COMPATIBLE WITH THIS PLAYER. " + chr(10) + "CHANGE RESOLUTION, ENABLE AUTO-FORMAT OR USE ANOTHER PLAYER.")
        screenContent = ScreenMessage("CONFIGURED RESOLUTION IS NOT COMPATIBLE WITH THIS PLAYER. " + chr(10) + "CHANGE RESOLUTION, ENABLE AUTO-FORMAT OR USE ANOTHER PLAYER.", 3000)
        tweaklabRegistry.write("resolutionValidity", "true")

        while true
        end while
    end if

    ' this is needed, to show text messages on the screen after a video was played. Otherwise messages would be overlaid.
    EnableZoneSupport(true)
    videoMode.SetGraphicsZOrder("front")

    return changed
end Function

' helper for UpdateDisplaySettings
' @param displaySettings The display.xml converted to a roXMLElement 
function videoModeFromXML(displaySettings) as String
    ' Collect settings
    width = displaySettings.width.getText()
    height = displaySettings.height.getText()
    freq = displaySettings.freq.getText()
    if displaySettings.interlaced.getText() = "true" then
        interlaced = "i"
    else 
        interlaced = "p"
    end if 

    return width + "x" + height + "x" + freq + interlaced
end function

' Deletes the content of the media folder.
sub resetFileStructure()
    ' set mediaFolder from settings.xml
    settings = CreateObject("roXMLElement")
    if not settings.parseFile("/settings.xml") then
        print "not able to parse /settings.xml"
        stop
    end if
    mediaFolder = settings.mediaFolder.getText()

    DeleteDirectory("/" + mediaFolder)
    CreateDirectory("/" + mediaFolder)
end sub

' Deletes all files on the SD-Card
sub clearSD()
    for each e in ListDir("/")
        if not DeleteFile(e) then
            if not DeleteDirectory(e) then
                info(e + " is not deletable")
            end if
        end if
    end for
end sub

' Compares the settings in settings.xml with the set state of debug mode. If it changed, update the setting. Debug mode
' enables the ctrl-c signal to stop script over console, and makes the brightsign changing into debug console if a failure
' appears in a script. In normal mode, the BrightSign would reboot. 
'
' @param settings The settings.xml converted to a roXMLELement
function updateDebugSettings(settings as Object) as Object
    brightscriptRegistry = createObject("roRegistrySection", "brightscript")
    changed = false
    if settings.debug.getText() = "false" and brightscriptRegistry.read("debug") = "1" then
        info("Disabling debug mode.")
        ScreenMessage("Disabling debug mode.", 3000)
        brightscriptRegistry.Delete("debug")

        ' disable ssh
        section = CreateObject("roRegistrySection", "networking")
        section.Delete("ssh")
        section.flush()

        changed = true
    else if settings.debug.getText() = "true" and brightscriptRegistry.read("debug") <> "1" then
        info("Enabling debug mode.")
        ScreenMessage("Enabling debug mode.", 3000)
        brightscriptRegistry.write("debug", "1")

        ' enable ssh
        section = CreateObject("roRegistrySection", "networking")
        section.write("ssh","22")
        section.flush()

        ' set ssh password
        netConf = CreateObject("roNetworkConfiguration", 0)
        netConf.SetLoginPassword(settings.ssh_password.getText())
        netConf.apply()

        changed = true
    end if
    return changed
end function




