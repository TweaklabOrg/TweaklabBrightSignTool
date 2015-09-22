package ch.tweaklab.player.configurator;

import java.io.IOException;
import java.util.Properties;

import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.Keys;

public class PlayerDisplaySettings {

  private Boolean auto;
  private int width;
  private int height;
  private int freq;
  private Boolean interlaced;

  public static PlayerDisplaySettings getDefaultDisplaySettings() {

    PlayerDisplaySettings displaySettings = new PlayerDisplaySettings();

    displaySettings.auto = Boolean.parseBoolean((Keys.loadProperty(Keys.DEFAULT_DISPLAY_AUTO_PROPS_KEY)));
    displaySettings.width = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_DISPLAY_WIDTH_PROPS_KEY));
    displaySettings.height = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_DISPLAY_HEIGHT_PROPS_KEY));
    displaySettings.freq = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_DISPLAY_FREQ_PROPS_KEY));
    displaySettings.interlaced = Boolean.parseBoolean((Keys.loadProperty(Keys.DEFAULT_DISPLAY_INTERLACED_PROPS_KEY)));

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


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((auto == null) ? 0 : auto.hashCode());
    result = prime * result + freq;
    result = prime * result + height;
    result = prime * result + ((interlaced == null) ? 0 : interlaced.hashCode());
    result = prime * result + width;
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PlayerDisplaySettings other = (PlayerDisplaySettings) obj;
    if (auto == null) {
      if (other.auto != null)
        return false;
    } else if (!auto.equals(other.auto))
      return false;
    if (freq != other.freq)
      return false;
    if (height != other.height)
      return false;
    if (interlaced == null) {
      if (other.interlaced != null)
        return false;
    } else if (!interlaced.equals(other.interlaced))
      return false;
    if (width != other.width)
      return false;
    return true;
  }

  
}
