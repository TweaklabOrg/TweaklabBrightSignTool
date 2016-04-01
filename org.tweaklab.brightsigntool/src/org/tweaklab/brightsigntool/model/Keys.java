package org.tweaklab.brightsigntool.model;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Keys {
  private static final Logger LOGGER = Logger.getLogger(Keys.class.getName());

  // Application Keys
  public static final String SCRIPTS_DIRECTORY = "bs-scripts";
  public static final String INCLUDED_JAR_RELATIVE_PATH = "included_jar_relative_path";
  // PROPS KEY
  public static final String BS_SCRIPTS_PROPS_KEY = "scriptsToUpload";
  public static final String SCRIPT_VERSION_PROPS_KEY = "script_version";
  public static final String ClIENT_VERSION_PROPS_KEY = "client_version";
  public static final String APP_NAME_PROPS_KEY = "app_name";
  public static final String APP_NICE_NAME_PROPS_KEY = "app_nice_name";
//  public static final String IMAGE_REGEX_PROPS_KEY = "image_regex";
  public static final String VIDEO_REGEX_PROPS_KEY = "video_regex";
  public static final String AUDIO_REGEX_PROPS_KEY = "audio_regex";
  // GENERAL SETTINGS PROPS KEY
  public static final String DEFAULT_TCP_PORT_PROPS_KEY = "default_tcp_port";
  public static final String DEFAULT_MEDIA_FOLDER_PROPS_KEY = "default_mediaFolder";
  public static final String DEFAULT_DEBUG_PROPS_KEY = "default_debug";
  public static final String DEFAULT_HOSTNAME_PROPS_KEY = "default_hostname";
  public static final String DEFAULT_IP_PROPS_KEY = "default_ip";
  public static final String DEFAULT_GATEWAY_PROPS_KEY = "default_gateway";
  public static final String DEFAULT_NETWORK_PROPS_KEY = "default_netmask";
  public static final String DEFAULT_VOLUME_PROPS_KEY = "default_volume";
  public static final String DEFAULT_SSH_PASSWORD_PROPS_KEY = "default_ssh_password";
  public static final String DEFAULT_DHCP_PROPS_KEY = "default_dhcp";
  // DISPLAY PROPS KEY
  public static final String DEFAULT_DISPLAY_AUTO_PROPS_KEY = "default_display_auto";
  public static final String DEFAULT_DISPLAY_WIDTH_PROPS_KEY = "default_display_width";
  public static final String DEFAULT_DISPLAY_HEIGHT_PROPS_KEY = "default_display_height";
  public static final String DEFAULT_DISPLAY_FREQ_PROPS_KEY = "default_display_freq";
  public static final String DEFAULT_DISPLAY_INTERLACED_PROPS_KEY = "default_display_interlaced";
  // Path to the fxml files:
  public static final String CONNECT_SCREEN_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/ConnectScreen.fxml";
  public static final String GPIO_TAB_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/GpioTab.fxml";
  public static final String PLAYLIST_TAB_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/PlaylistTab.fxml";
  public static final String ROOT_PAGE_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/RootPage.fxml";
  public static final String UPLOAD_SCREEN_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/UploadScreen.fxml";
  public static final String WAIT_SCREEN_FXML_PATH = "/org/tweaklab/brightsigntool/gui/view/WaitScreen.fxml";
  private static String CONFIG_FILE_NAME = "config.properties";

  public static String loadProperty(String key) {
    Properties configFile = new Properties();
    String value = "";
    InputStream resource = Keys.class.getResourceAsStream("/" + CONFIG_FILE_NAME);
    try {
      configFile.load(resource);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't load " + CONFIG_FILE_NAME, e);
    }
    value = configFile.getProperty(key);
    return value;
  }
}
