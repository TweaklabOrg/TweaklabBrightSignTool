package ch.tweaklab.player.test.configurator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.tweaklab.player.configurator.PlayerDisplaySettings;
import ch.tweaklab.player.configurator.PlayerGeneralSettings;
import ch.tweaklab.player.configurator.XMLConfigCreator;
import ch.tweaklab.player.model.MediaFile;
import ch.tweaklab.player.test.util.TestUtil;

public class XMLTest {

  String workDirectory = "work";

  @Before
  public void setUp() throws Exception {

  }
  
  @Test
  public void createDisplaySettingsDefaultXML(){
    
    PlayerDisplaySettings defaultDisplaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();
    XMLConfigCreator.createDisplaySettingsXml(defaultDisplaySettings);
  }

  @Test
  public void createGeneralSettingsDefaultXML(){
    
    PlayerGeneralSettings settings = PlayerGeneralSettings.getDefaulGeneralSettings();
    XMLConfigCreator.createGeneralSettingsXml(settings);
  }

  
  
  @Test
  public void createPlaylistXML() {

 
    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();

    File xmlFile = XMLConfigCreator.createPlayListXML(mediaFiles);

    assertTrue(xmlFile.exists());

  }

  @Test
  public void createGpioXML() {

    File xmlFile1;
    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
    MediaFile[] mediaFilesArray = new MediaFile[10];
    for (int i = 0; i < mediaFiles.size(); i++) {
      mediaFilesArray[i] = mediaFiles.get(i);
    }

    mediaFilesArray[2] = null;
    xmlFile1 = XMLConfigCreator.createGpioXML(mediaFiles.get(2), mediaFilesArray, true, "1000");

    assertTrue(xmlFile1.exists());


  }
}
