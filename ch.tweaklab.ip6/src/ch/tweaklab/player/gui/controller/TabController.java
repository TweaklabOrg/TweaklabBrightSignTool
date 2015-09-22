package ch.tweaklab.player.gui.controller;

import ch.tweaklab.player.model.MediaUploadData;

/**
 * Abstract Class for all tab controllers 
 * Methods are called by RootPageController
 * @author Alain
 *
 */
public abstract class TabController {

 abstract public MediaUploadData getMediaUploadData();
}
