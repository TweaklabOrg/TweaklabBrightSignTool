package ch.tweaklab.ip6.test.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.tweaklab.ip6.mediaLogic.MediaFile;

public class TestUtil {

  
  public static List<MediaFile> getMediaFiles(){
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
    
    URL path = MediaFile.class.getClassLoader().getResource("testmedia/test-image.jpg");
    MediaFile mediaFile = new MediaFile(new File(path.getFile()));
    mediaFile.setDisplayTime(10);
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("testmedia/test-movie.mp4");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("testmedia/test-unknown.txt");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("testmedia/test-audio.mp3");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    
    return mediaFiles;
  }
}
