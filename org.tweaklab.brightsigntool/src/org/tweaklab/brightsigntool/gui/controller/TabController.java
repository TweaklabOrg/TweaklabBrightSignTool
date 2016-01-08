package org.tweaklab.brightsigntool.gui.controller;

import org.tweaklab.brightsigntool.model.Keys;
import org.tweaklab.brightsigntool.model.MediaUploadData;

import java.util.Map;

/**
 * Abstract Class for all tab controllers 
 * Methods are called by RootPageController
 * @author Alain
 *
 */
public abstract class TabController {

 abstract public MediaUploadData getMediaUploadData();

 abstract public void setContent(Map<String, String> content);

 protected Boolean validateFileFormat(String filename){
  String audioRegex = Keys.loadProperty(Keys.AUDIO_REGEX_PROPS_KEY);
  String videoRegex = Keys.loadProperty(Keys.VIDEO_REGEX_PROPS_KEY);
  //TODO: add image support here
  return (filename.matches(audioRegex) || filename.matches(videoRegex));
   
 }
}
