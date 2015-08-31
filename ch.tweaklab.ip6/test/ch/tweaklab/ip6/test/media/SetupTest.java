package ch.tweaklab.ip6.test.media;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import ch.tweaklab.ip6.gui.controller.MainApp;

public class SetupTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void copySetupFiles() {
    try {
      File targetDirectory = new File("C:/temp/brightsignsetup/");
      FileUtils.cleanDirectory(targetDirectory);
      URL sourceUrl = this.getClass().getClassLoader().getResource("setup");
      File sourceDir = new File(sourceUrl.getPath()); 
      FileUtils.copyDirectory(sourceDir,targetDirectory);
    } catch (IOException e) {
    e.printStackTrace();
    }
  }

}
