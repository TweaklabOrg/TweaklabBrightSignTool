sub main()
    ' set mediaFolder from general.xml
    generalXml = CreateObject("roXMLElement")
    if not generalXml.parseFile("/general.xml") then
        print "not able to parse /general.xml"
        stop
    end if
    mediaFolder = generalXml.mediaFolder.getText()

	DeleteDirectory("/" + mediaFolder)
	CreateDirectory("/" + mediaFolder)
end sub