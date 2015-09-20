package ch.tweaklab.player.configurator;

import java.io.IOException;
import java.util.Properties;

import ch.tweaklab.player.gui.controller.MainApp;
import ch.tweaklab.player.model.Keys;

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

    settings.mediaFolder = Keys.loadProperty(Keys.DEFAULT_MEDIA_FOLDER_PROPS_KEY);
    settings.hostname = Keys.loadProperty(Keys.DEFAULT_NAME_PROPS_KEY);
    settings.dhcp = Boolean.parseBoolean(Keys.loadProperty(Keys.DEFAULT_DHCP_PROPS_KEY));
    settings.ip = Keys.loadProperty(Keys.DEFAULT_IP_PROPS_KEY);

    settings.gateway = Keys.loadProperty(Keys.DEFAULT_GATEWAYS_PROPS_KEY);
    if (settings.gateway == null || settings.gateway.equals("")) {
      settings.gateway = settings.ip;
    }
    settings.netmask = Keys.loadProperty(Keys.DEFAULT_NETWORK_PROPS_KEY);

    settings.tcpPort = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_TCP_PORT_PROPS_KEY));
    settings.volume = Integer.parseInt(Keys.loadProperty(Keys.DEFAULT_VOLUME_PROPS_KEY));

    settings.mode = Keys.loadProperty(Keys.DEFAULT_MODE_PROPS_KEY);
    settings.scriptVersion = Keys.loadProperty(Keys.DEFAULT_SCRIPT_VERSION_PROPS_KEY);

    settings.sshPassword = Keys.loadProperty(Keys.DEFAULT_SSH_PASSWORD_PROPS_KEY);
    settings.debug = Boolean.parseBoolean((Keys.loadProperty(Keys.DEFAULT_DEBUG_PROPS_KEY)));
    settings.initialize = Boolean.parseBoolean((Keys.loadProperty(Keys.DEFAULT_INITIALIZE_PROPS_KEY)));

    return settings;
  }

  public String getMediaFolder() {
    return mediaFolder;
  }

  public String getHostname() {
    return hostname;
  }

  public String getIp() {
    return ip;
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

  public String getSshPassword() {
    return sshPassword;
  }

  public int getTcpPort() {
    return tcpPort;
  }

  public Boolean getDebug() {
    return debug;
  }

  public String getMode() {
    return mode;
  }

  public int getVolume() {
    return volume;
  }

  public Boolean getInitialize() {
    return initialize;
  }

  public String getScriptVersion() {
    return scriptVersion;
  }

  public void setMediaFolder(String mediaFolder) {
    this.mediaFolder = mediaFolder;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public void setNetmask(String netmask) {
    this.netmask = netmask;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public void setDhcp(Boolean dhcp) {
    this.dhcp = dhcp;
  }

  public void setSshPassword(String sshPassword) {
    this.sshPassword = sshPassword;
  }

  public void setTcpPort(int tcpPort) {
    this.tcpPort = tcpPort;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public void setInitialize(Boolean initialize) {
    this.initialize = initialize;
  }

  public void setScriptVersion(String scriptVersion) {
    this.scriptVersion = scriptVersion;
  }

}
