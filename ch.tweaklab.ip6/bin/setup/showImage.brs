Sub Main() 
  p = CreateObject("roVideoPlayer")
  print p.PlayStaticImage("/bild.jpg")
  'print p.PlayFile("/video.mp4")
  print "done"
  stop
End Sub