Sub Main() 
    MEDIA_ENDED = 8

    vPlayer = CreateObject("roVideoPlayer")
    port = CreateObject("roMessagePort")
    vPlayer.SetPort(port)
    
    xml = CreateObject("roXMLElement")
    if not xml.parseFile("/playlist.xml") then stop

    files = xml.task
    if files.count() = 0 then stop

    ' set mediaFolder from general.xml
    generalXml = CreateObject("roXMLElement")
    if not generalXml.parseFile("/general.xml") then
        print "not able to parse /general.xml"
        stop
    end if
    mediaFolder = generalXml.mediaFolder.getText()
    vPlayer.setVolume(val(generalXml.volume.getText())


    '------ MAIN LOOP ------
    files.reset()
    nextFile = files.Next()
    vPlayer.playFile(mediaFolder + "/" + nextFile.GetText())
    print "playing ";nextFile.GetText()
    while true
        msg = wait(0, port)
        if type(msg) = "roVideoEvent" and msg.GetInt() = MEDIA_ENDED then 
            if not files.isNext() then
                files.Reset()
            end if
            nextFile = files.Next()
            vPlayer.playFile(mediaFolder + "/" + nextFile.GetText())
            print "playing ";nextFile.GetText()
        end if
    end while
End Sub