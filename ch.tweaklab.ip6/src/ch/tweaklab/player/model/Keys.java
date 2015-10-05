package ch.tweaklab.player.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import ch.tweaklab.player.gui.controller.MainApp;

public class Keys {

  private static String CONFIG_FILE_NAME = "config.properties";

  // Application Keys
  public static final String APPLICATION_TITLE = "Tweaklab Media Player";
  public static final String APPLICATION_VERSION = "1.0";

  // PROPS KEY
  public static final String WORK_DIRECTORY_PROPS_KEY = "work_directory";
  public static final String SCRIPTS_DIRECTORY_PROPS_KEY = "scripts_directory";

  public static final String IMAGE_REGEX_PROPS_KEY = "image_regex";
  public static final String VIDEO_REGEX_PROPS_KEY = "video_regex";
  public static final String AUDIO_REGEX_PROPS_KEY = "audio_regex";

  public static final String SSH_USER_PROPS_KEY = "ssh_user";
  public static final String SSH_PORT_PROPS_KEY = "ssh_port";

  // GENERAL SETTINGS PROPS KEY
  public static final String DEFAULT_TCP_PORT_PROPS_KEY = "default_tcp_port";
  public static final String DEFAULT_MEDIA_FOLDER_PROPS_KEY = "default_mediaFolder";
  public static final String DEFAULT_DEBUG_PROPS_KEY = "default_debug";

  public static final String DEFAULT_HOSTNAME_PROPS_KEY = "default_hostname";
  public static final String DEFAULT_IP_PROPS_KEY = "default_ip";
  public static final String DEFAULT_GATEWAYS_PROPS_KEY = "default_gateways";
  public static final String DEFAULT_NETWORK_PROPS_KEY = "default_netmask";
  public static final String DEFAULT_VOLUME_PROPS_KEY = "default_volume";

  public static final String DEFAULT_SCRIPT_VERSION_PROPS_KEY = "default_script_version";
  public static final String DEFAULT_SSH_PASSWORD_PROPS_KEY = "default_ssh_password";
  public static final String DEFAULT_DHCP_PROPS_KEY = "default_dhcp";
  public static final String DEFAULT_MODE_PROPS_KEY = "default_mode";
  public static final String DEFAULT_INITIALIZE_PROPS_KEY = "default_initialize";

  // DISPLAY PROPS KEY
  public static final String DEFAULT_DISPLAY_AUTO_PROPS_KEY = "default__display_auto";
  public static final String DEFAULT_DISPLAY_WIDTH_PROPS_KEY = "default_display_width";
  public static final String DEFAULT_DISPLAY_HEIGHT_PROPS_KEY = "default_display_height";
  public static final String DEFAULT_DISPLAY_FREQ_PROPS_KEY = "default_display_freq";

  public static final String DEFAULT_DISPLAY_INTERLACED_PROPS_KEY = "default_display_interlaced";

  // Path to the fxml files:
  public static final String CONNECT_SCREEN_FXML_PATH = "/ch/tweaklab/player/gui/view/ConnectScreen.fxml";
  public static final String GPIO_TAB_FXML_PATH =       "/ch/tweaklab/player/gui/view/GpioTab.fxml";
  public static final String PLAYLIST_TAB_FXML_PATH =   "/ch/tweaklab/player/gui/view/PlaylistTab.fxml";
  public static final String ROOT_PAGE_FXML_PATH =      "/ch/tweaklab/player/gui/view/RootPage.fxml";
  public static final String UPLOAD_SCREEN_FXML_PATH =  "/ch/tweaklab/player/gui/view/UploadScreen.fxml";
  public static final String WAIT_SCREEN_FXML_PATH =    "/ch/tweaklab/player/gui/view/WaitScreen.fxml";

  public static String getAppFolderPath(){
	  String path = Keys.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.toString().substring(0, path.lastIndexOf("/")+1);
		return path;
  }
  
  public static String loadProperty(String key) {
    Properties configFile = new Properties();
    String value = "";
    try {
    		InputStream resource = new FileInputStream(getAppFolderPath() + Keys.CONFIG_FILE_NAME);
      configFile.load(resource);
      value = configFile.getProperty(key);
    } catch (Exception e) {
      e.printStackTrace();
      MainApp.showExceptionMessage(e);
    }
    return value;
  }

}
