package ch.tweaklab.player.test.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.util.DiscoverServices;

public class TestUtil {

  public static List<File> getSystemFiles(){
    
    List<File> systemFiles = new ArrayList<File>();
    File file = XMLConfigCreator.createGeneralSettingsXml(PlayerGeneralSettings.getDefaulGeneralSettings());
    systemFiles.add(file);
    file = XMLConfigCreator.createDisplaySettingsXml(PlayerDisplaySettings.getDefaultDisplaySettings());
    systemFiles.add(file);
    
    return systemFiles;
    
  }
  
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
  
  
  @Test
  public void bonjourTest(){
    //Type: _tl._tcp.
   
    String servicename = "_tl._tcp.local";
    DiscoverServices discoverer = new DiscoverServices();
    discoverer.searchServices(servicename);
  }
  
}
