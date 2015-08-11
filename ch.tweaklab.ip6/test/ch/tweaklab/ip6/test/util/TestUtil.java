package ch.tweaklab.ip6.test.util;

import java.io.File;
import java.net.URL;
import java.util.List;

import ch.tweaklab.ip6.media.MediaFile;

import java.util.ArrayList;

public class TestUtil {

  
  public static List<MediaFile> getMediaFiles(){
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
    
    URL path = MediaFile.class.getClassLoader().getResource("test-image.jpg");
    MediaFile mediaFile = new MediaFile(new File(path.getFile()));
    mediaFile.setDisplayTime(10);
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("test-movie.mp4");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("test-unknown.txt");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    path = mediaFile.getClass().getClassLoader().getResource("test-music.mp3");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    
    return mediaFiles;
  }
}
