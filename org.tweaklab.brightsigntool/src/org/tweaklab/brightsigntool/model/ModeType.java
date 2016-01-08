package org.tweaklab.brightsigntool.model;

public enum ModeType {
  GPIO, PLAYLIST;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
