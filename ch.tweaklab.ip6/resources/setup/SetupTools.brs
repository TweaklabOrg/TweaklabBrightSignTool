

' taken from BrightScript setup files
Sub ClearRegistryKeys(registrySection As Object)

    ' Clear legacy registry keys
    registrySection.Delete("next")
    registrySection.Delete("event")
    registrySection.Delete("error")
    registrySection.Delete("deviceerror")
    registrySection.Delete("devicedownload")
    registrySection.Delete("recurl")
    registrySection.Delete("timezone")
    registrySection.Delete("unitName")
    registrySection.Delete("unitNamingMethod")
    registrySection.Delete("unitDescription")
    registrySection.Delete("timeBetweenNetConnects")
    registrySection.Delete("contentDownloadsRestricted")
    registrySection.Delete("contentDownloadRangeStart")
    registrySection.Delete("contentDownloadRangeLength")
    registrySection.Delete("useDHCP")
    registrySection.Delete("staticIPAddress")
    registrySection.Delete("subnetMask")
    registrySection.Delete("gateway")
    registrySection.Delete("broadcast")
    registrySection.Delete("dns1")
    registrySection.Delete("dns2")
    registrySection.Delete("dns3")
    registrySection.Delete("timeServer")
    registrySection.Delete("account")
    registrySection.Delete("user")
    registrySection.Delete("password")
    registrySection.Delete("group")

    ' Clear other keys in case they're no longer used
    registrySection.Delete("cdrs")
    registrySection.Delete("cdrl")
    registrySection.Delete("ps")
    registrySection.Delete("ss")
    registrySection.Delete("pp")
    registrySection.Delete("ncp2")
    registrySection.Delete("cwf")
    registrySection.Delete("twf")
    registrySection.Delete("hwf")
    registrySection.Delete("mwf")
    registrySection.Delete("lwf")
    registrySection.Delete("sip")
    registrySection.Delete("sip")
    registrySection.Delete("sm")
    registrySection.Delete("gw")
    registrySection.Delete("d1")
    registrySection.Delete("d2")
    registrySection.Delete("d3")
    registrySection.Delete("rlmow")
    registrySection.Delete("rlrow")
    registrySection.Delete("rlmiw")
    registrySection.Delete("rlriw")
    registrySection.Delete("rlmid")
    registrySection.Delete("rlrid")
    registrySection.Delete("sip2")
    registrySection.Delete("sm2")
    registrySection.Delete("gw2")
    registrySection.Delete("d12")
    registrySection.Delete("d22")
    registrySection.Delete("d32")
    registrySection.Delete("rlmow2")
    registrySection.Delete("rlrow2")
    registrySection.Delete("rlmiw2")
    registrySection.Delete("rlriw2")
    registrySection.Delete("rlmid2")
    registrySection.Delete("rlrid2")
    registrySection.Delete("uup")
    registrySection.Delete("cfv")

    registrySection.Delete("brightWallName")
    registrySection.Delete("brightWallScreenNumber")

End Sub

Sub UpdateNetworkSettings(settings As Object)
    changed = false
    nc = CreateObject("roNetworkConfiguration", 0)
    current = nc.GetCurrentConfig()
    if current.dhcp = false and settings.dhcp.getText() = "true"
        info("dhcp enabled")
        nc.setDHCP()
        changed = true
    else
        if current.ip4_address <> settings.ip.getText() then
            info("changing ip from " + current.ip4_address + " to " + settings.ip.getText())
            nc.setIP4Address(settings.ip.getText())
            changed = true
        end if
        if current.ip4_netmask <> settings.netmask.getText() then
            info("changing netmaske from " + current.ip4_netmask + " to " + settings.netmask.getText())
            nc.setIP4Netmask(settings.netmask.getText())
            changed = true
        end if
    end if
    if nc.getHostName() <> settings.name.getText() then
        info("changing name from " + nc.getHostName() + " to " + settings.name.getText())
        changed = true
        nc.setHostName(settings.name.getText())
    end if
    if (changed) then
        nc.apply()
    end if
End Sub

Sub SetAllLoggingEnabled(registrySection As Object)
    registrySection.Write("ple", "yes") 'playbackLoggingEnabled'
    registrySection.Write("ele", "yes") 'eventLoggingEnabled'
    registrySection.Write("sle", "yes") 'stateLoggingEnabled'
    registrySection.Write("dle", "yes") 'diagnosticLoggingEnabled'
    registrySection.Write("uab", "no")  'uploadLogFilesAtBoot'
    registrySection.Write("uat", "no")  'uploadLogFilesAtSpecificTime'
    registrySection.Write("ut", "0")    'uploadLogFilesTime'
End Sub

Function UpdateDisplaySettings() as Object
    changed = false

    displayXml = CreateObject("roXMLElement")
    if not displayXml.parseFile("/display.xml") then
        info("not able to parse /display.xml")
        stop
    end if

    videoMode = CreateObject("roVideoMode")

    'changed to autoformat?
    if displayXml.auto.getText() = "true" and videoMode.getModeForNextBoot() <> "auto"
        videoMode.SetModeForNextBoot("auto")
        info("changing display settings to autoformat")
        info("rebooting to make display settings taking effect. please reconnect after reboot!")
        changed = true
    end if

    'autoformat is disabled and format was changed?
    width = displayXml.width.getText()
    height = displayXml.height.getText()
    freq = displayXml.freq.getText()
    if displayXml.auto.getText() = "false" and videoMode.getMode() <> (width + "x" + height + "x" + freq) then 
        info("changing display settings from " + videoMode.getMode() + " to " + width + "x" + height + "x" + freq)
        videoMode.SetModeForNextBoot(width + "x" + height + "x" + freq)
        info("rebooting to make display settings taking effect. please reconnect after reboot!")
        changed = true
    end if

    return changed
end Function

sub info(message As String) 
    print message
    m.sysLog.SendLine("From Script: " + message)
end sub

