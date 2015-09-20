package ch.tweaklab.player.configurator;

import java.io.IOException;
import java.util.Properties;

import ch.tweaklab.player.gui.controller.MainApp;

public class PlayerDisplaySettings {

  private Boolean auto;
  private int width;
  private int height;
  private int freq;
  private Boolean interlaced;

  public static PlayerDisplaySettings getDefaultDisplaySettings() {

    PlayerDisplaySettings displaySettings = new PlayerDisplaySettings();

    Properties configFile = new Properties();
    try {
      configFile.load(PlayerDisplaySettings.class.getClassLoader().getResourceAsStream("config.properties"));

      displaySettings.auto = Boolean.parseBoolean((configFile.getProperty("auto")));
      displaySettings.width = Integer.parseInt(configFile.getProperty("width"));
      displaySettings.height = Integer.parseInt(configFile.getProperty("height"));
      displaySettings.freq = Integer.parseInt(configFile.getProperty("freq"));
      displaySettings.interlaced = Boolean.parseBoolean((configFile.getProperty("interlaced")));

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

    return displaySettings;
  }

  public Boolean getAuto() {
    return auto;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getFreq() {
    return freq;
  }

  public Boolean getInterlaced() {
    return interlaced;
  }

  public void setAuto(Boolean auto) {
    this.auto = auto;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setFreq(int freq) {
    this.freq = freq;
  }

  public void setInterlaced(Boolean interlaced) {
    this.interlaced = interlaced;
  }

}
