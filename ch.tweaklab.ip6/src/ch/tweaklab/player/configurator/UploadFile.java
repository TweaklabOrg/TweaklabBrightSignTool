package ch.tweaklab.player.configurator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import ch.tweaklab.player.gui.controller.MainApp;

public class UploadFile {
	private final String fileName;
	private final byte[] fileAsBytes;

	public UploadFile(String fileName, ByteArrayOutputStream byteArrayOutputStream) {
		super();
		this.fileName = fileName;
		this.fileAsBytes = byteArrayOutputStream.toByteArray();
	}
	public UploadFile(File file) {
			
		super();
		byte[] bytes = null;
		try {
		bytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.fileName = file.getName();
		this.fileAsBytes = bytes;
	}
	
	


	public String getFileName() {
		return fileName;
	}
	public byte[] getFileAsBytes() {
		return fileAsBytes;
	}




}
