package org.tweaklab.brightsigntool.configurator;

import org.tweaklab.brightsigntool.model.Keys;

public class PlayerDisplaySettings {

  private Boolean auto;
  private String width;
  private String height;
  private String freq;
  private Boolean interlaced;

  public static PlayerDisplaySettings getDefaultDisplaySettings() {

    PlayerDisplaySettings displaySettings = new PlayerDisplaySettings();

    displaySettings.auto = Boolean.parseBoolean(Keys.loadProperty(Keys.DEFAULT_DISPLAY_AUTO_PROPS_KEY));
    displaySettings.width = Keys.loadProperty(Keys.DEFAULT_DISPLAY_WIDTH_PROPS_KEY);
    displaySettings.height = Keys.loadProperty(Keys.DEFAULT_DISPLAY_HEIGHT_PROPS_KEY);
    displaySettings.freq = Keys.loadProperty(Keys.DEFAULT_DISPLAY_FREQ_PROPS_KEY);
    displaySettings.interlaced = Boolean.parseBoolean((Keys.loadProperty(Keys.DEFAULT_DISPLAY_INTERLACED_PROPS_KEY)));

    return displaySettings;
  }

  public Boolean getAuto() {
    return auto;
  }

  public String getWidth() {
    return width;
  }

  public String getHeight() {
    return height;
  }

  public String getFreq() {
    return freq;
  }

  public Boolean getInterlaced() {
    return interlaced;
  }

  public void setAuto(Boolean auto) {
    this.auto = auto;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public void setFreq(String freq) {
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
    result = prime * result + Integer.parseInt(freq);
    result = prime * result + Integer.parseInt(height);
    result = prime * result + ((interlaced == null) ? 0 : interlaced.hashCode());
    result = prime * result + Integer.parseInt(width);
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
    if (!freq.equals(other.freq))
      return false;
    if (!height.equals(other.height))
      return false;
    if (interlaced == null) {
      if (other.interlaced != null)
        return false;
    } else if (!interlaced.equals(other.interlaced))
      return false;
    if (!width.equals(other.width))
      return false;
    return true;
  }
}
