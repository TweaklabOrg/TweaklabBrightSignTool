package org.tweaklab.brightsigntool.test.configurator;

import org.tweaklab.brightsigntool.configurator.PlayerDisplaySettings;
import org.tweaklab.brightsigntool.configurator.PlayerGeneralSettings;
import org.tweaklab.brightsigntool.configurator.UploadFile;
import org.tweaklab.brightsigntool.configurator.XmlConfigCreator;
import org.tweaklab.brightsigntool.model.MediaFile;
import org.tweaklab.brightsigntool.test.util.TestUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class XMLTest {

  @Before
  public void setUp() throws Exception {

  }
  
  @Test
  public void createDisplaySettingsDefaultXML(){
    
    PlayerDisplaySettings defaultDisplaySettings = PlayerDisplaySettings.getDefaultDisplaySettings();
    XmlConfigCreator.createDisplaySettingsXml(defaultDisplaySettings);
  }

  @Test
  public void createGeneralSettingsDefaultXML(){
    
    PlayerGeneralSettings settings = PlayerGeneralSettings.getDefaulGeneralSettings();
    XmlConfigCreator.createGeneralSettingsXml(settings);
  }

  
  
  @Test
  public void createPlaylistXML() {

 
    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();

    UploadFile uploadFile = XmlConfigCreator.createPlayListXML(mediaFiles);

    File destFile = new File("work/playlist.xml");
    try {
		FileUtils.writeByteArrayToFile(destFile, uploadFile.getFileAsBytes());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    assertTrue(destFile.exists());

  }

  @Test
  public void createGpioXML() {

    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
    MediaFile[] mediaFilesArray = new MediaFile[10];
    for (int i = 0; i < mediaFiles.size(); i++) {
      mediaFilesArray[i] = mediaFiles.get(i);
    }

    mediaFilesArray[2] = null;
    UploadFile uploadFile = XmlConfigCreator.createGpioXML(mediaFiles.get(0), mediaFilesArray, true, "1000");

    File destFile = new File("work/gpio.xml");
    try {
		FileUtils.writeByteArrayToFile(destFile, uploadFile.getFileAsBytes());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    assertTrue(destFile.exists());


  }
}
