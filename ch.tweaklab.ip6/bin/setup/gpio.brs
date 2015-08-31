sub main()
    ' used states for roVideoEvent and vPlayerState
    PLAYING = 3
    MEDIA_ENDED = 8
    READY = 10

    vPlayer = CreateObject("roVideoPlayer")
    vPlayerState = READY
    
    gpio = CreateObject("roControlPort", "BrightSign")
    ' Buttons
    gpio.EnableInput(0)
    gpio.EnableInput(1)
    gpio.EnableInput(2)
    gpio.EnableInput(3)
    ' LED's
    gpio.EnableOutput(4)
    gpio.EnableOutput(5)
    gpio.EnableOutput(6)
    gpio.EnableOutput(7)
    
    port = CreateObject("roMessagePort")
    gpio.SetPort(port)
    vPlayer.SetPort(port)

    ' set mediaFolder from general.xml
    generalXml = CreateObject("roXMLElement")
    if not generalXml.parseFile("/general.xml") then
        print "not able to parse /general.xml"
        stop
    end if
    mediaFolder = generalXml.mediaFolder.getText()
    vPlayer.setVolume(val(generalXml.volume.getText())

    gpioXml = CreateObject("roXMLElement")
    if not gpioXml.parseFile("/gpio.xml") then
        print "not able to parse /gpio.xml"
        stop
    end if

    ' set retriggerEnabled from gpio.xml
    if (gpioXml.retriggerEnabled.count() = 0) then 
        retriggerEnabled = true 'default
    else if (gpioXml.retriggerEnabled.getText() = "true") then 
        retriggerEnabled = true
    else 
        retriggerEnabled = false
    end if

    ' set retriggerDelay from gpio.xml
    if (gpioXml.retriggerDelay.count() = 0) then 
        retriggerDelay = 0 'default
    else if retriggerEnabled then 
        retriggerDelay = val(gpioXml.retriggerDelay.getText())
    else 
        retriggerDelay = 0
    end if

    ' always start with the loop, if loop is defined
    if gpioXml.loop.count() > 0 then 
        vPlayer.playFile(mediaFolder + "/" + gpioXml.loop.getText())
        print "playing loop file"
    end if

    retriggerTimer = CreateObject("roTimespan")
    retriggerTimer.Mark()

    ' ---- MAIN LOOP ----
    while true
        msg = wait(0, port)
        if type(msg) = "roControlDown" and ((not retriggerEnabled and vPlayerState =  READY) or (retriggerEnabled and retriggerTimer.totalMilliseconds() > retriggerDelay)) then
            if msg.getInt() = 0 and gpioXml.gpio0.count() > 0 then
                vPlayer.playFile(mediaFolder + "/" + gpioXml.gpio0.getText())
                gpio.SetWholeState(0)
                gpio.SetOutputState(4, 1)
                vPlayerState = PLAYING
                retriggerTimer.Mark()
                print "playing ";gpioXml.gpio0.getText()
            else if msg.getInt() = 1 and gpioXml.gpio1.count() > 0 then 
                vPlayer.playFile(mediaFolder + "/" + gpioXml.gpio1.getText())
                gpio.SetWholeState(0)
                gpio.SetOutputState(5, 1)
                vPlayerState = PLAYING
                retriggerTimer.Mark()
                print "playing ";gpioXml.gpio1.getText()
            else if msg.getInt() = 2 and gpioXml.gpio2.count() > 0 then 
                vPlayer.playFile(mediaFolder + "/" + gpioXml.gpio2.getText())
                gpio.SetWholeState(0)
                gpio.SetOutputState(6, 1)
                vPlayerState = PLAYING
                retriggerTimer.Mark()
                print "playing ";gpioXml.gpio2.getText()
            else if msg.getInt() = 3 and gpioXml.gpio3.count() > 0 then 
                vPlayer.playFile(mediaFolder + "/" + gpioXml.gpio3.getText())
                gpio.SetWholeState(0)
                gpio.SetOutputState(7, 1)
                vPlayerState = PLAYING
                retriggerTimer.Mark()
                print "playing ";gpioXml.gpio3.getText()
            end if
        end if
        if type(msg) = "roVideoEvent" and msg.GetInt() = MEDIA_ENDED then
            if gpioXml.loop.count() > 0 then 
                vPlayer.playFile(mediaFolder + "/" + gpioXml.loop.getText())
                print "playing loop file"
            else
                print "file ended, no loop file defined"
            end if
            gpio.SetWholeState(0)
            vPlayerState = READY
        end if
    end while 
end sub
