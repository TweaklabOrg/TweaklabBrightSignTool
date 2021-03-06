' Shows a message in the center of the connected display for a defined time. Alternativly you can store the returned 
' roTextWidget to make the message persist on screen as long as the roTextWidget is refereced. 
' 
' @param message The message that will be shown. 
' @param duration The duration in milliseconds the message will be shown. 
function ScreenMessage(message as String, duration as Integer) as Object
    ' A videoMode object holds basic informations about the video settings.
    videoMode = CreateObject("roVideoMode")
    resX = videoMode.GetResX()
    resY = videoMode.GetResY()

    ' Define a frame where the message will be shown.
    r=CreateObject("roRectangle",0,resY/2-resY/64,resX,resY/16)

    ' Create a collection of settings
    twParams = CreateObject("roAssociativeArray")
    twParams.LineCount = 2 ' write everything in one line
    twParams.TextMode = 2 ' show it emediatley, no queue, ...
    twParams.Rotation = 0 ' 0 degrees rotation
    twParams.Alignment = 1 ' center the text in the frame

    tw=CreateObject("roTextWidget", r, twParams.LineCount, twParams.TextMode, twParams)
    tw.PushString(message)
    tw.Show()

    ' TODO: this is a bad solution, as event handler can't handle events while sleep is waiting.
    Sleep(duration)

    return tw
end function

' Shows a message in the console and the log. You must store the returned roTextWidget to make 
' the message persist on screen. It will pesist as long as the roTextWidget is refereced. 
sub info(message As String) 
    print message
    m.sysLog.SendLine("From Script: " + message)
end sub

' Shows a Simple welcome header. You must store the returned roTextWidget to make the message 
' persist on screen. It will pesist as long as the roTextWidget is refereced. 
function SimpelHeader() as Object
    border = 20 ' border around the whole screen

    ' A videoMode object holds basic informations about the video settings.
    videoMode = CreateObject("roVideoMode")
    width = videoMode.GetResX() - 2 * border
    height = (videoMode.GetResY() - 2 * border) / 3

    ' Define a frame where the header will be displaied.
    r=CreateObject("roRectangle", border, border, width, height)

    ' Create a collection of settings for the roTextWidget
    twParams = CreateObject("roAssociativeArray")
    twParams.LineCount = 10
    twParams.TextMode = 2
    twParams.Rotation = 0
    twParams.Alignment = 1

    tw=CreateObject("roTextWidget", r, twParams.LineCount, twParams.TextMode, twParams)
    content = CreateObject("roString")

    ' Convert settings.xml to a roXMLElement. We will need the scriptVersion later on.
    settings = CreateObject("roXMLElement")
    settings.parseFile("/settings.xml")

    ' Convert mode.xml to a roXMLElement.
    mode = CreateObject("roXMLElement")
    mode.parseFile("/mode.xml")

    app("", content)
    app("", content)
    app("", content)
    app("", content)
    app("", content)
    ' Title and script version
    app("TWEAKLAB BrightSign Player " + settings.scriptVersion.getText(), content)
    app("in " + mode.getText() + "-mode", content)
    app("", content)
    app("", content)
    app("", content)

    tw.PushString(content)
    ' tw.Show()

    return tw
end function

' Shows a detailed welcome screen. You must store the returned roTextWidget to make the message 
' persist on screen. It will pesist as long as the roTextWidget is refereced. 
function DeviceInfos() as Object
    border = 20 ' border around the whole screen

    ' A videoMode object holds basic informations about the video settings.
    videoMode = CreateObject("roVideoMode")
    resX = videoMode.GetResX() - 2 * border
    resY = videoMode.GetResY() - 2 * border

    ' Define a frame where the header will be displaied.
    r=CreateObject("roRectangle", border, border, resX, resY)

    ' Create a collection of settings for the roTextWidget
    twParams = CreateObject("roAssociativeArray")
    twParams.LineCount = 30
    twParams.TextMode = 2
    twParams.Rotation = 0
    twParams.Alignment = 1

    tw=CreateObject("roTextWidget", r, twParams.LineCount, twParams.TextMode, twParams)
    content = CreateObject("roString")

    ' Convert settings.xml to a roXMLElement. We will need the scriptVersion later on.
    settings = CreateObject("roXMLElement")
    settings.parseFile("/settings.xml")

    ' Convert mode.xml to a roXMLElement.
    mode = CreateObject("roXMLElement")
    mode.parseFile("/mode.xml")

    app("", content)
    app("", content)
    app("", content)
    app("", content)
    app("", content)
    ' Title and script version
    app("TWEAKLAB BrightSign Player " + settings.scriptVersion.getText(), content)
    app("in " + mode.getText() + "-mode", content)
    app("", content)
    app("", content)

    ' Copy network configurations to content
    net = CreateObject("roNetworkConfiguration", 0)
    conf = net.GetCurrentConfig()
    netDiacnostics = net.TestInterface()

    ' if a ethernet cable is connected, wait for otaining an ip.
    if netDiacnostics.diagnosis <> "Ethernet interface has no link" then
        waitForIP(net)
    end if

    app("Name: " + conf.hostname, content)
    if conf.dhcp then
        app("DHCP: enabled", content)
    else 
        app("DHCP: disabled", content)
    end if
    app("ip address: " + conf.ip4_address, content)
    app("netmask: " + conf.ip4_netmask, content)
    app("gateway: " + conf.ip4_gateway, content)
    netDiacnostics = net.TestInterface()
    if netDiacnostics.ok then
        app("ethernet: ok", content)
    else 
        app("ethernets first problem: " + netDiacnostics.diagnosis, content)
    end if
    app("MAC address: " + conf.ethernet_mac, content)

    ' video resoultion
    videoMode = CreateObject("roVideoMode")
    app("Video Resolution: " + videoMode.GetModeForNextBoot(), content)

    ' settings von settings.xml
    app("Volume: " + settings.volume.GetText(), content)

    ' verify debug setting
    if CreateObject("roRegistrySection", "brightscript").read("debug") = "1" then
        app("Debug-Mode: enabled.", content)
    else 
        app("Debug-Mode: disabled.", content)
    end if

    ' copy DeviceInfo to content'
    deviceInfo = CreateObject("roDeviceInfo")
    app("Model: " + deviceInfo.GetModel(), content)
    app("Firmware: " + deviceInfo.GetVersion(), content)
    app("Boot Firmware: " +  deviceInfo.GetBootVersion(), content)

    tw.PushString(content)
    ' tw.Show()

    return tw
end function

' small helper to simplify appending stings to the content including the line feed and length.
sub app(line as String, container as String)
    container.AppendString(line + chr(10), line.len() + 1)
end sub

sub waitForIP(net as Object)
    timer = CreateObject("roTimespan")
    timer.Mark()
    conf = net.GetCurrentConfig()
    while conf.ip4_address.Len() = 0 AND timer.totalMilliseconds() < 10000
        conf = net.GetCurrentConfig()
    end while

    if conf.ip4_address.Len() = 0 then
        info("Couldn't obtain IP address form DHCP for now...")
        ScreenMessage("Couldn't obtain IP address form DHCP for now...", 2000)
    else

    end if
end sub