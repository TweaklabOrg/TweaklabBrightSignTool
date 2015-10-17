package ch.tweaklab.player.test.util;

import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.UploadFile;
import ch.tweaklab.player.configurator.XmlConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.util.DiscoverServices;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

  public static List<UploadFile> getSystemFiles(){
    
    List<UploadFile> systemFiles = new ArrayList<UploadFile>();
    UploadFile file = XmlConfigCreator.createGeneralSettingsXml(PlayerGeneralSettings.getDefaulGeneralSettings());
    systemFiles.add(file);
    file = XmlConfigCreator.createDisplaySettingsXml(PlayerDisplaySettings.getDefaultDisplaySettings());
    systemFiles.add(file);
    
    return systemFiles;
    
  }
  
  public static List<MediaFile> getMediaFiles(){
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
    
    MediaFile mediaFile = null;
    URL path = MediaFile.class.getClassLoader().getResource("testmedia/test-movie.mp4");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    path = MediaFile.class.getClassLoader().getResource("testmedia/test-audio.mp3");
    mediaFile = new MediaFile(new File(path.getFile()));
    mediaFiles.add(mediaFile);
    
    
    return mediaFiles;
  }
  
  
  @Test
  public void bonjourTest(){
    //Type: _tl._tcp.
   
    String servicename = "_tl._tcp.local";
    DiscoverServices discoverer = new DiscoverServices();
    try {
      discoverer.searchServices(servicename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
}
