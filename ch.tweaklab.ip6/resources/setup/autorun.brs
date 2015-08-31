Library "SetupTools.brs"

' Tweaklab Custom BrightScript for BrightSign Players
' ---------------------------------------------------

' This Script is responsible to bring a Bright Sign Player in a valid state to run main scripts. In order
' to do that, it parses the general.xml and display.xml and adapts registry settings. 

' Version 0.1
' Stephan Brunner
' Tweaklab

sub main()
    info( "------- Tweaklab Custom BrightScript Version ";generalXml.scriptVersion.getText();" -------")
    info()

    ' generate a AssociativeArray (aka Hashmap) out of the general.xml settings
    generalXml = CreateObject("roXMLElement")
    if not generalXml.parseFile("/general.xml") then
        info("not able to parse /general.xml")
        stop
    end if

    ' a reboot might be necessarry depending on changes
    reboot = false

    ' if a initialisation is wanted, all registry entries are cleared and reset to a appropriate state.
    if generalXml.initialize.getText() = "true" 
        if m.DEBUG then info("setting player back to initial settings. rebooting...")

        networkRegistry = CreateObject("roRegistrySection", "networking")

        ClearRegistryKeys(networkRegistry) ' method from SetupTools.brs
        SetAllLoggingEnabled(networkRegistry) ' method from SetupTools.brs'

        ' enable webserver and diacnostic webserver
        networkRegistry.write("http_server", "80")

        ' enable ssh
        networkRegistry.write("ssh","22")
        netConf=CreateObject("roNetworkConfiguration", 0)
        netConf.SetLoginPassword(generalXml.ssh_password.getText())
        netConf.Apply()

        ' set initialize back to false in generalXml.xml (to avoid reboot loop)
        ' TODO unfortuantly this kills the formating and makes the xml almost anreadable
        generalXml.initialize.simplify().setbody("false")
        out = CreateObject("roByteArray")
        out.FromASCIIString(generalXml.GenXML(true))
        out.WriteFile("general.xml")

        reboot = true
    end if

    ' if display.xml changed, update settings. needs a reboot
    if UdateDisplaySettings() = true then ' method from SetupTools.brs
        reboot = true
    end if

    ' enable webserver if not enabled
    ' networkRegistry = CreateObject("roRegistrySection", "networking")
    ' if (networkRegistry.read("http_server") <> "80")
    '     networkRegistry.write("http_server", "80")
    '     reboot = true
    ' end if

    if (reboot) then
        rebootSystem()
    end if

    'compare networksettings with general.xml and change settings if necessary
    ' netConf = CreateObject("roNetworkConfiguration", 0)
    ' currNetConf = netConf.getCurrentConfig()
    ' if currNetConf.dhcp <> (generalXml.dhcp.getText() = "true") then
    '     netConf.setDHCP(generalXml.dhcp.getText())
    '     info( "changing dhcp from ";currNetConf.dhcp;" to ";generalXml.dhcp.getText())
    ' end if
    ' if generalXml.dhcp.getText() = "false" and currNetConf.ip4_address <> generalXml.ip.getText() then
    '     netConf.setIP4Address(generalXml.ip.getText())
    '     info( "changing ip from ";currNetConf.ip4_address;" to ";generalXml.ip.getText())
    ' end if
    ' if generalXml.dhcp.getText() = "false" and currNetConf.ip4_netmask <> generalXml.netmask.getText() then
    '     netConf.setIP4Netmask(generalXml.netmask.getText())
    '     info( "changing netmaske from ";currNetConf.netmask;" to ";generalXml.netmask.getText())
    ' end if
    ' if generalXml.name.getText() <> netConf.getHostName() then
    '     netConf.setHostName(generalXml.name.getText())
    ' end if
    ' netConf.apply()

    ' if network settings changed, update settings. Doesn't need a reboot.
    UpdateNetworkSettings(settings) ' method from SetupTools.brs

    'start main script. Any scriptname can be chosen in general.xml as long as a corresponding script exists.
    run(generalXml.mode.getText() + ".brs")
end sub