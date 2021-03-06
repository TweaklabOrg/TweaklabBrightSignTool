' Handles commands sent from the client. Can be easyly extended by adding other else ifs
' 
' @param msg The occured roStreamLineEvent.
sub handleStreamLineEvent(msg as Object, port as Object, videoplayer as Object, settings as Object)
    MEDIA_ENDED = 8

    ' reboot player
    if msg.GetString() = "reboot" then
        connection = msg.GetUserData()
        connection.stream.SendLine("OK")
        connection.stream.Flush()
        info("Received reboot command from client.")
        ScreenMessage("rebooting...", 3000)
        rebootSystem()
    ' delete mediafolder
    else if msg.GetString() = "resetFilestructure" then
        resetFilestructure() ' from tools_setup.brs
        connection = msg.GetUserData()
        connection.stream.SendLine("OK")
        connection.stream.Flush()
        info("Received resetFilestrucure command from client.")
    ' clear SD card
    else if msg.GetString() = "clearSD" then
        clearSD() ' from tool_setup.brs
        connection = msg.GetUserData()
        connection.stream.SendLine("OK")
        connection.stream.Flush()
        info("Received clearSD command from client.")
    ' signal that player is receiving data
    else if msg.GetString() = "receiveData" then
        connection = msg.GetUserData()
        connection.stream.SendLine("OK")
        connection.stream.Flush()
        videoPlayer.StopClear()
        info("Received receiving data command from client.")
        ScreenMessage("receiving data... ", 3000)
    ' play a video file
    else if msg.GetString().Left(6) = "play: " then
        info("Received play command from client.")
        file = settings.mediaFolder.getText() + "/" + msg.GetString().Mid(6)
        videoPlayer.StopClear()
        if videoPlayer.playFile(file) then
            info("playing " + file)
        else
            ' act as if video has ended
            endMessage = CreateObject("roVideoEvent")
            endMessage.SetInt(MEDIA_ENDED)
            port.PostMessage(endMessage)
            info("not able to play " + file)
            ScreenMessage("not able to play " + file, 3000)
        end if
    ' get SD format
    else if msg.GetString() = "SDFormat" then
        storageInfo = CreateObject("roStorageInfo", "SD:/")
        connection = msg.GetUserData()
        connection.stream.SendLine(storageInfo.GetFileSystemType())
        connection.stream.Flush()
    ' validate requested video format
    else 
        videoMode = createObject("roVideoMode")
        connection = msg.GetUserData()
        current = videoMode.GetMode()
        if videoMode.SetModeForNextBoot(msg.GetString()) then
            connection.stream.SendLine("supported")
            info("requested video format supported: " + msg.GetString())
        else
            connection.stream.SendLine("not supported")
            info("requested video format not supported: " + msg.GetString())
        end if
        videoMode.SetModeForNextBoot(current)
        connection.stream.Flush()
    end if
end sub

' Handles TCP Connection requests from a client.
'
' @param msg The occured roTCPConnectEvent
' @param port The message port that will be registered in the new roTCPStream object.
' @param connections The pool of roTCPStreams that will be used to store the new connection.
sub handleTCPConnectEvent(msg as Object, port as Object, connections as Object)
    stream = createObject("roTCPStream") ' always has to be rebuilt after last acception
    if not connections.isNext()
        connections.reset()
    end if 
    connection = connections.next()
    connection.msg = msg
    connection.stream = stream
    connection.stream.SetUserData(connection)
    connection.stream.SetLineEventPort(port)
    ' Accept must be at the end of the method, as problems occured that the first string sent wasn't handeled otherwise. 
    ' Looks like the network card waits to acknowledge the connection until the Accept is called. If the Accept is called 
    ' as late as possible, all the objects are allready build and linked so most of the work is done and the first string
    ' sent can be received.
    ' I made tests what happens if the Accept isn't called at all after a connection request was received. It looked like
    ' the network protocol sends the achnowledge anyway what would make the TCP protocol unnecessary.... (?) I'd say that
    ' there might be situations of network faults, that could signal en established TCP connection, but actualy not handle
    ' any sent strings. Using devices with this script might show reliability.
    if connection.stream.Accept(msg) then
        info("connected with " + msg.GetSourceAddress())
    else 
        info("connection with " + msg.GetSourceAddress() + " failed")
    end if
end sub

' Shows that a connection has been closed
' 
' @param msg The occured roStreamEndEvent
sub handleStreamEndEvent(msg as Object)
    info("connection to " + msg.GetUserData().msg.GetSourceAddress() + " has been closed")
    ' GarbageColelctor must be runned as the connection is linked with the UserData of the TCPStream stored in the connection 
    ' itself. The BrightScript GarbageCollector identifies those objects as selfreferencing and unused, and deletes them. 
    if RunGarbageCollector().orphaned > 0 ' to make selfreferencing connections are deleted
        info("garbage collector removed an object")
    end if
end sub

' Pseudo object of a new connection containing all necessary infromation, and if possible build needed objects allready.
function newConnection() As Object
    c = createObject("roAssociativeArray")
    c.msg = invalid
    c.stream = invalid
    return c
end function