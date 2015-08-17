package ch.tweaklab.ip6.test.media;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.tweaklab.ip6.media.MediaFile;
import ch.tweaklab.ip6.media.XMLConfigCreator;
import ch.tweaklab.ip6.test.util.TestUtil;

public class XMLTest {

  String workDirectory = "work";

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void createPlaylistXML() {

    File xmlFile = new File(workDirectory + "/playlist.xml");
    if (xmlFile.exists()) {
      xmlFile.delete();
    }
    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();

    xmlFile = XMLConfigCreator.createPlayListXML(mediaFiles);

    assertTrue(xmlFile.exists());

  }
  
  @Test
  public void createButtonXML() {

    File xmlFile = new File(workDirectory + "/button.xml");
    if (xmlFile.exists()) {
      xmlFile.delete();
    }
    List<MediaFile> mediaFiles = TestUtil.getMediaFiles();
    MediaFile[] mediaFilesArray = new MediaFile[10];
    for(int i = 0; i < mediaFiles.size(); i++){
      mediaFilesArray[i] = mediaFiles.get(i);
    }

    xmlFile = XMLConfigCreator.createButtontXML(mediaFilesArray);

    assertTrue(xmlFile.exists());

  }
  
}
