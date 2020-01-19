package com.fastautomation.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Frame extends Element
{
	private Browser browser;
	
	private int frameId = -1;
	
	private static int frameIdCounter = 1;
	
	public Frame(Element parentElement, By by)
	{
		super(parentElement, by);
		
		this.browser = (parentElement instanceof Frame ? (Frame) parentElement : parentElement.getContainerFrame()).browser;
	}
	
	
	public Frame(Browser browser)
	{
		super(null);
		
		this.browser = browser;
	}
	
	public Browser getBrowser()
	{
		return browser;
	}
	
	public WebDriver getWebDriver()
	{
		switchDriverToFrame();
		return browser.getCurrentWebDriver();
	}

	public Object executeScript(String script, Object... arguments)
	{
		switchDriverToFrame();
		
		JavascriptExecutor executor = (JavascriptExecutor) browser.getCurrentWebDriver();
		
		Object[] newArgments = new Object[arguments.length];
		
		for(int i = 0; i < newArgments.length ; i++)
		{
			Object arg = arguments[i];
			
			arg = revalidScriptIO(arg, true);
			
			newArgments[i] = arg;
		}
		
		Object result = executor.executeScript(script, newArgments);
		
		result = revalidScriptIO(result, false);
		
		return result;
	}
	
	private Object revalidScriptIO(Object io, boolean input)
	{
		if(io == null)
		{
			return null;
		}
		
		if((io instanceof Element) && input)
		{
			return ((Element) io).getSeleniumWebElement();
		}
		else if((io instanceof WebElement) && !input )
		{			
			Element element = this.createRelatedElement(true, (WebElement) io);
			
			element.updateJavaScriptReference(element.getContainerFrame().getBrowser().getCurrentWebDriver(), (WebElement) io);
			
			return element;
		}
		else if(io instanceof List)
		{
			List<Object> list =  (List<Object>) io;
			
			List<Object> newList = new ArrayList<Object>();
			
			for(int i = 0; i <  list.size(); i++)
			{
				Object object = list.get(i);
				object = revalidScriptIO(object, input);
				newList.add(object);
			}
			
			return newList;
		}
		else if(io instanceof Map)
		{
			Map<String, Object> map =  (Map<String, Object>) io;
			
			Map<String, Object> newMap = new HashMap<String, Object>();
			
			for(Entry<String, Object> entry : map.entrySet())
			{
				newMap.put(entry.getKey(), revalidScriptIO(entry.getValue(), input));
			}
			
			return newMap;
		}
		else
		{
			return io;
		}
		
	}

	private int getCurrentFrameId()
	{
		browser.waitForComplete();

		JavascriptExecutor executor =  (JavascriptExecutor) browser.getCurrentWebDriver();
		try {
			return  (int) (long) (Long) executor.executeScript("if(typeof document.syncedAutomationFrameId === 'undefined') { return -1 } else { return document.syncedAutomationFrameId }");
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	private void setCurrentFrameId(int frameId)
	{
		browser.waitForComplete();
		
		JavascriptExecutor executor =  (JavascriptExecutor) browser.getCurrentWebDriver();
		
		Integer result = (int) (long) (Long) executor.executeScript("return document.syncedAutomationFrameId = arguments[0];", frameId);
		
		if(frameId != result)
		{
			throw new RuntimeException("Unable to set document.syncedAutomationFrameId = "+frameId+" in current frame");
		}
	}
	
	
	WebDriver switchDriverToFrame()
	{
		int currentFrameId = getCurrentFrameId();
		
		if(this.frameId != -1 && this.frameId == currentFrameId)
		{			
			return browser.getCurrentWebDriver();
		}
		
		List<Element> elements = new LinkedList<Element>();
		
		Element elementToAdd = this;
		
		while(elementToAdd != null)
		{
			elements.add(0, elementToAdd);
			
			elementToAdd = elementToAdd.getParentElement();
		}
		
		browser.resetToMainWindow();
		
		WebDriver webDriver = browser.getCurrentWebDriver();
		
		for(Element element : elements)
		{	
			if(!(element instanceof Frame))
			{
				continue;
			}
			
			Frame frameElement = (Frame) element;
			
			boolean isTopFrame = frameElement.getParentElement() == null && frameElement.getSelector() == null;
			
			if(!isTopFrame)
			{
				WebElement webElement = frameElement.getWebElement(webDriver);
				
				webDriver = webDriver.switchTo().frame(webElement);
			}
			
			
			if(frameElement.frameId == -1 || frameElement.frameId != getCurrentFrameId())
			{
				setCurrentFrameId( ++ frameIdCounter);
				this.frameId = frameIdCounter;
			}
		}
		
		return webDriver;
	}
	
}
