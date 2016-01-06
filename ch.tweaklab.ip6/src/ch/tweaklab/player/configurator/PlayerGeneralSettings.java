package ch.tweaklab.player.configurator;

import ch.tweaklab.player.model.Keys;

public class PlayerGeneralSettings {

  private String mediaFolder;

  private String hostname = "";
  private String ip= "";
  private String netmask= "";
  private String gateway= "";
  private Boolean dhcp;

  private String sshPassword= "";
  private String tcpPort;
  private Boolean debug;
  private String volume;
  private Boolean initialize= true;
  private String scriptVersion= "";

  public static PlayerGeneralSettings getDefaulGeneralSettings() {

    PlayerGeneralSettings settings = new PlayerGeneralSettings();

    settings.mediaFolder = Keys.loadProperty(Keys.DEFAULT_MEDIA_FOLDER_PROPS_KEY);
    settings.hostname = Keys.loadProperty(Keys.DEFAULT_HOSTNAME_PROPS_KEY);
    settings.dhcp = Boolean.parseBoolean(Keys.loadProperty(Keys.DEFAULT_DHCP_PROPS_KEY));
    settings.ip = Keys.loadProperty(Keys.DEFAULT_IP_PROPS_KEY);

    settings.gateway = Keys.loadProperty(Keys.DEFAULT_GATEWAY_PROPS_KEY);
    if (settings.gateway == null || settings.gateway.equals("")) {
      settings.gateway = settings.ip;
    }
    settings.netmask = Keys.loadProperty(Keys.DEFAULT_NETWORK_PROPS_KEY);

    settings.tcpPort = Keys.loadProperty(Keys.DEFAULT_TCP_PORT_PROPS_KEY);
    settings.volume = Keys.loadProperty(Keys.DEFAULT_VOLUME_PROPS_KEY);
    settings.scriptVersion = Keys.loadProperty(Keys.SCRIPT_VERSION_PROPS_KEY);

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

  public String getTcpPort() {
    return tcpPort;
  }

  public Boolean getDebug() {
    return debug;
  }


  public String getVolume() {
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
    this.tcpPort = String.valueOf(tcpPort);
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }


  public void setVolume(int volume) {
    if (volume > 100) {
      this.volume = "100";
    } else {
      this.volume = String.valueOf(volume);
    }
  }

  public void setInitialize(Boolean initialize) {
    this.initialize = initialize;
  }

  public void setScriptVersion(String scriptVersion) {
    this.scriptVersion = scriptVersion;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((debug == null) ? 0 : debug.hashCode());
    result = prime * result + ((dhcp == null) ? 0 : dhcp.hashCode());
    result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
    result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
    result = prime * result + ((initialize == null) ? 0 : initialize.hashCode());
    result = prime * result + ((ip == null) ? 0 : ip.hashCode());
    result = prime * result + ((mediaFolder == null) ? 0 : mediaFolder.hashCode());
    result = prime * result + ((netmask == null) ? 0 : netmask.hashCode());
    result = prime * result + ((scriptVersion == null) ? 0 : scriptVersion.hashCode());
    result = prime * result + ((sshPassword == null) ? 0 : sshPassword.hashCode());
    result = prime * result + ((tcpPort == null) ? 0 : tcpPort.hashCode());
    result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
    PlayerGeneralSettings other = (PlayerGeneralSettings) obj;
    if (debug == null) {
      if (other.debug != null)
        return false;
    } else if (!debug.equals(other.debug))
      return false;
    if (dhcp == null) {
      if (other.dhcp != null)
        return false;
    } else if (!dhcp.equals(other.dhcp))
      return false;
    if (gateway == null) {
      if (other.gateway != null)
        return false;
    } else if (!gateway.equals(other.gateway))
      return false;
    if (hostname == null) {
      if (other.hostname != null)
        return false;
    } else if (!hostname.equals(other.hostname))
      return false;
    if (initialize == null) {
      if (other.initialize != null)
        return false;
    } else if (!initialize.equals(other.initialize))
      return false;
    if (ip == null) {
      if (other.ip != null)
        return false;
    } else if (!ip.equals(other.ip))
      return false;
    if (mediaFolder == null) {
      if (other.mediaFolder != null)
        return false;
    } else if (!mediaFolder.equals(other.mediaFolder))
      return false;
    if (netmask == null) {
      if (other.netmask != null)
        return false;
    } else if (!netmask.equals(other.netmask))
      return false;
    if (scriptVersion == null) {
      if (other.scriptVersion != null)
        return false;
    } else if (!scriptVersion.equals(other.scriptVersion))
      return false;
    if (sshPassword == null) {
      if (other.sshPassword != null)
        return false;
    } else if (!sshPassword.equals(other.sshPassword))
      return false;
    if (tcpPort.equals(other.tcpPort))
      return false;
    if (volume.equals(other.volume))
      return false;
    return true;
  }
}
