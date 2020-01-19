package com.fastautomation.components;

import java.io.File;

public class StepInfo {
	private boolean passed;
	private Object data;
	private String description;
	private String name;
	private File imageFile;
	private File logFile;
	
	public boolean isPassed() {
		return passed;
	}
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getLogFile() {
		return logFile;
	}
	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}
	public File getImageFile() {
		return imageFile;
	}
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

}
