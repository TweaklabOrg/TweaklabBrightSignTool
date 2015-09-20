package ch.tweaklab.player.configurator;

import java.io.IOException;
import java.util.Properties;

import ch.tweaklab.player.gui.controller.MainApp;

public class PlayerGeneralSettings {

  private String mediaFolder;

  private String hostname;
  private String ip;
  private String netmask;
  private String gateway;
  private Boolean dhcp;

  private String sshPassword;
  private int tcpPort;
  private Boolean debug;
  private String mode;

  private int volume;
  private Boolean initialize;
  private String scriptVersion;

  public static PlayerGeneralSettings getDefaulGeneralSettings() {

    PlayerGeneralSettings settings = new PlayerGeneralSettings();

    Properties configFile = new Properties();
    try {
      configFile.load(PlayerDisplaySettings.class.getClassLoader().getResourceAsStream("config.properties"));

      settings.mediaFolder = configFile.getProperty("mediaFolder");
      settings.hostname = configFile.getProperty("hostname");
      settings.dhcp = Boolean.parseBoolean(configFile.getProperty("dhcp"));
      settings.ip = configFile.getProperty("ip");

      settings.gateway = configFile.getProperty("gateway");
      if (settings.gateway == null || settings.gateway.equals("")) {
        settings.gateway = settings.ip;
      }
      settings.netmask = configFile.getProperty("netmask");

      settings.tcpPort = Integer.parseInt(configFile.getProperty("tcp_port"));
      settings.volume = Integer.parseInt(configFile.getProperty("volume"));

      settings.mode = configFile.getProperty("mode");
      settings.scriptVersion = configFile.getProperty("script_version");

      settings.sshPassword = configFile.getProperty("ssh_password");
      settings.debug = Boolean.parseBoolean((configFile.getProperty("debug")));
      settings.initialize = Boolean.parseBoolean((configFile.getProperty("initialize")));

    } catch (IOException e) {
      MainApp.showExceptionMessage(e);
    }

    return settings;
  }

  public String getScriptVersion() {
    return scriptVersion;
  }

  public String getHostname() {
    return hostname;
  }

  public String getIp() {
    return ip;
  }

  public int getVolume() {
    return volume;
  }

  public Boolean getInitialize() {
    return initialize;
  }

  public String getMediaFolder() {
    return mediaFolder;
  }

  public String getNetmask() {
    return netmask;
  }

  public String getGateway() {
    return gateway;
  }

  public Boolean getDhcp() {
    return dhcp;
  }

  public int getTcpPort() {
    return tcpPort;
  }

  public String getMode() {
    return mode;
  }

  public String getSshPassword() {
    return sshPassword;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

}
