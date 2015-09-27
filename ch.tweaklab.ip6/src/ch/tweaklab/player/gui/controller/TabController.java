package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.model.Keys;
import ch.tweaklab.player.model.MediaUploadData;

/**
 * Abstract Class for all tab controllers 
 * Methods are called by RootPageController
 * @author Alain
 *
 */
public abstract class TabController {

 abstract public MediaUploadData getMediaUploadData();
 
 
 protected Boolean validateFileFormat(String filename){
  String audioRegex = Keys.loadProperty(Keys.AUDIO_REGEX_PROPS_KEY);
  String imageRegex = Keys.loadProperty(Keys.IMAGE_REGEX_PROPS_KEY);
  String videoRegex = Keys.loadProperty(Keys.VIDEO_REGEX_PROPS_KEY);
  
  return (filename.matches(audioRegex) || filename.matches(imageRegex) || filename.matches(videoRegex));
   
 }
}
