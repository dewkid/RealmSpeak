/* 
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.general.io;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class FileManager {
	
	private JFrame parent;
	private File currentDirectory;
	
	private String description = null;
	private String forcedExtension = null;
	
	protected FileFilter saveFileFilter = null;
	protected FileFilter loadFileFilter = null;
	protected FileFilter saveDirectoryFilter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory();
		}

		public String getDescription() {
			return "Directory";
			
		}
	};
	
	public FileManager(JFrame parent,String description,String forcedExtension) {
		this.parent = parent;
		this.forcedExtension = forcedExtension;
		this.description = description;
		if (forcedExtension!=null) {
			saveFileFilter = new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory() || (f.isFile() && f.getPath().endsWith(FileManager.this.forcedExtension));
				}
		
				public String getDescription() {
					return FileManager.this.description;
				}
			};
		}
	}
	public void setLoadFileFilter(FileFilter filter) {
		loadFileFilter = filter;
	}
	public void setCurrentDirectory(File file) {
		if (file.isDirectory()) {
			currentDirectory = file;
		}
	}
	public File getCurrentDirectory() {
		return currentDirectory;
	}
	private JFileChooser getChooser(String title) {
		JFileChooser chooser;
		if (currentDirectory!=null) {
			chooser = new JFileChooser(currentDirectory);
		}
		else {
			chooser = new JFileChooser();
		}
		if (title!=null) {
			chooser.setDialogTitle(title);
		}
		return chooser;
	}
	public File getLoadPath() {
		return getLoadPath(null);
	}
	public File getLoadPath(String title) {
		JFileChooser chooser = getChooser(title);
		if (loadFileFilter!=null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(loadFileFilter);
		}
		else if (saveFileFilter!=null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(saveFileFilter);
		}
		if (chooser.showOpenDialog(parent)==JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (selectedFile!=null) {
				currentDirectory = selectedFile.getParentFile();
				return selectedFile;
			}
		}
		return null;
	}
	public File getSavePath() {
		return getSavePath(null,null);
	}
	public File getSavePath(String suggestedFilename) {
		return getSavePath(suggestedFilename,null);
	}
	public File getSavePath(String suggestedFilename,String title) {
		JFileChooser chooser = getChooser(title);
		if (saveFileFilter!=null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(saveFileFilter);
		}
		if (suggestedFilename!=null) {
			StringBuffer sb = new StringBuffer();
			sb.append(currentDirectory==null?"":currentDirectory.getAbsolutePath());
			sb.append(File.separator);
			sb.append(suggestedFilename);
			File file = new File(sb.toString());
			File selectedFile = FileUtilities.fixFileExtension(file,forcedExtension);
			chooser.setSelectedFile(selectedFile);
		}
		if (chooser.showSaveDialog(parent)==JFileChooser.APPROVE_OPTION) {
			File selectedFile = FileUtilities.fixFileExtension(chooser.getSelectedFile(),forcedExtension);
			if (selectedFile!=null) {
				currentDirectory = selectedFile.getParentFile();
				return selectedFile;
			}
		}
		return null;
	}
	public File getSaveDirectory(String title) {
		JFileChooser chooser = getChooser(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showSaveDialog(parent)==JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			if (dir!=null) {
				currentDirectory = dir;
				return dir;
			}
		}
		return null;
	}
}