package com.fastautomation.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Represent Browser tab
 */
public class Browser
{
	private WebDriver webDriver;
	private BrowserType browserType;
	private String mainWindowHandle;
	
	private static int defaultWaitingTimeInSeconds = 60;
	
	private static Map<String, Browser> attachedBrowsers = new HashMap<String, Browser>();
	
	private Frame topFrame = new Frame(this);
	
	/**
	 * Create Returns browser tab object with respective current state of provided selenium WebDriver object
	 */
	public Browser(WebDriver webDriver)
	{
		setWebDriver(webDriver);
	}
	
	public static int getTimeoutInSeconds()
	{
		return defaultWaitingTimeInSeconds;
	}
	
	public static void setTimeoutInSeconds(int seconds)
	{
		defaultWaitingTimeInSeconds = seconds;
		
		if(!attachedBrowsers.isEmpty())
		{
			attachedBrowsers.values().stream().forEach(d -> d.getCurrentWebDriver().manage().timeouts().implicitlyWait(defaultWaitingTimeInSeconds, TimeUnit.SECONDS));
			//attachedBrowsers.values().iterator().next().getCurrentWebDriver().manage().timeouts().implicitlyWait(defaultWaitingTimeInSeconds, TimeUnit.SECONDS);
		}
	}
	
	/**
	 * Create and return Returns browser tab object with respective provied BrowserType {FireFox, Chrome, IE, EDGE} and url.
	 */
	public Browser(BrowserType browserType, String url)
	{
		this.browserType = browserType;
		
		WebDriver webDriver = null;
		
		switch(browserType)
		{
			case FireFox : webDriver = new FirefoxDriver(); break;
			case Chrome : webDriver = new ChromeDriver(); break;
			case IE : webDriver = new InternetExplorerDriver(); break;
			case EDGE : webDriver = new EdgeDriver(); break;
		}
		
		webDriver.get(url);
		
		setWebDriver(webDriver);
	}

	/**
	 * Load url current browser tab. This method is similar to selenium 'webdriver.get(url)'
	 */
	public void get(String url)
	{
		resetToMainWindow();
		
		webDriver.get(url);
		
		resetHandle();
	}
	
	
	/**
	 * Navigate page to provided url. This method is similar to selenium 'webDriver.navigate().to(url)'
	 */
	public void to(String url)
	{
		resetToMainWindow();
		
		webDriver.navigate().to(url);
		
		resetHandle();
	}
	
	/**
	 * Maximize Browser Window
	 */
	public void maximize()
	{
		resetToMainWindow();
		webDriver.manage().window().maximize();
	}
	
	
	/**
	 * Create new or get existing Browser object with respective provided selenium WebDriver object'
	 */
	public static Browser getBrowser(WebDriver webDriver)
	{		
		String windowHandle = webDriver.getWindowHandle();
		
		for(Entry<String, Browser>  entry : attachedBrowsers.entrySet())
		{
			if(entry.getKey().equals(windowHandle))
			{
				return entry.getValue();
			}
		}
		
		return new Browser(webDriver);
	}
	
	/**
	 * Return BrowserType object among FireFox, Chrome, IE, EDGE'
	 */
	
	public BrowserType getBrowserType()
	{
		return browserType;
	}
	
	public Element findElement(String cssSelector)
	{
		return findElement(By.cssSelector(cssSelector));
	}
	
	public List<Element> findElements(String cssSelector)
	{
		return findElements(By.cssSelector(cssSelector));
	}
	
	public Frame findFrame(String cssSelector)
	{
		return findFrame(By.cssSelector(cssSelector));
	}
	
	public List<Frame> findFrames(String cssSelector)
	{
		return findFrames(By.cssSelector(cssSelector));
	}	
	
	
	/**
	 * find and return Frame in current document with respective provided 'selenium By selector'
	 */
	public Frame findFrame(By by)
	{
		return new Frame(topFrame, by);
	}
	
	
	/**
	 * find and return Frames in current document with respective provided 'selenium By selector'
	 */
	
	List<Frame> findFrames(By by)
	{
		Frame frame = new Frame(topFrame, by);
		
		List<Element> elements = frame.getElements();
		
		List<Frame> frames = new LinkedList<Frame>();
		
		for(Element element : elements)
		{
			if(element instanceof Frame)
			{
				frames.add((Frame) element);
			}
		}
		
		return frames;
	}
	
	
	/**
	 * find and return Element in current document with respective provided 'selenium By selector'
	 */
	
	public Element findElement(By by)
	{
		return new Element(topFrame, by);
	}
	
	
	/**
	 * find and return Elements in current document with respective provided 'selenium By selector'
	 */
	
	public List<Element> findElements(By by)
	{
		Element element = new Element(topFrame, by);
		return element.getElements();
	}
	
	
	
	/**
	 * Wait until document to load completely in browser
	 */
	
	void waitForComplete()
	{	
		String windowHandle = null;
		try
		{
			windowHandle = webDriver.getWindowHandle();	
		}
		catch(NoSuchWindowException ex)
		{
			
		}
		
		if(!mainWindowHandle.equalsIgnoreCase(windowHandle))
		{
			resetToMainWindow();
		}

		JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
		
		String script = "return (typeof document === 'undefined' ? false : document.readyState === 'complete');";
		
		for(int i =0 ; i < defaultWaitingTimeInSeconds; i++)
		{
			boolean result = (Boolean) jsExecutor.executeScript(script);
			
			if(result)
			{
				return;
			}
			
			sleep(1000);
		}
		
		throw new RuntimeException("browser is not loaded completely in "+defaultWaitingTimeInSeconds + " seconds");
	}
	
	/**
	 * Close browser tab, quit selenium web driver if all browser tabs are closed.
	 */	
	public void close()
	{
		resetToMainWindow();
		
		attachedBrowsers.remove(mainWindowHandle);
		
		refreshAttachedBrowserMap(webDriver);
		
		webDriver.close();
	}
	
	///////////////////////////////////////////
	
	/**
	 * Attach and get newly opened Browser Tab object
	 */	
	public Browser attachAndGetNewWindow()
	{
		Set<String> handles = webDriver.getWindowHandles();
		
		if(handles == null || handles.isEmpty())
		{
			return null;
		}
		
		for(String handle : handles)
		{			
			if(!attachedBrowsers.containsKey(handle))
			{
				webDriver.switchTo().window(handle);
				return new Browser(webDriver);
			}
		}
		
		return null;
	}
	

	/** 
	 * Get current state of Selenium WebDriver
	 */
	public WebDriver getWebDriver()
	{
		topFrame.switchDriverToFrame();
		
		return webDriver;
	}
	
	public Object executeScript(String script, Object... arguments)
	{		
		JavascriptExecutor executor = (JavascriptExecutor) getWebDriver();
		
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
			Element element = topFrame.createRelatedElement(true, (WebElement) io);
			
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
	
	
	
	WebDriver getCurrentWebDriver()
	{
		return webDriver;
	}
	
	
	///////////////////////////////////////////


	private void setWebDriver(WebDriver webDriver)
	{
		this.webDriver = webDriver;
		
		Capabilities cap = ((RemoteWebDriver)webDriver).getCapabilities();

		String browserName = cap.getBrowserName();
		
		if(browserName != null && !(browserName = browserName.toLowerCase().trim()).isEmpty())
		{
			if(browserName.contains("mozilla") || browserName.contains("firefox") )
			{
				this.browserType = BrowserType.FireFox;
			}
			else if(browserName.contains("chrome"))
			{
				this.browserType = BrowserType.Chrome;
			}
			else if(browserName.contains("edge"))
			{
				this.browserType = BrowserType.EDGE;
			}
			else if(browserName.contains("internet explorer") )
			{
				this.browserType = BrowserType.IE;
			}
		}

		this.mainWindowHandle = webDriver.getWindowHandle();

		attachedBrowsers.put(mainWindowHandle, this);
		
		setTimeoutInSeconds(defaultWaitingTimeInSeconds);
		
		String quitOnExit = System.getProperty("webdriver.quitOnExit");
		
		if(quitOnExit == null || !quitOnExit.equalsIgnoreCase("false"))
		{
			DriverCloserOnExit.getDriverCloserOnExit().add(webDriver);
		}
		
	}

	void resetToMainWindow()
	{
		webDriver.switchTo().window(mainWindowHandle);
		
		while(true != (Boolean) ((JavascriptExecutor) webDriver).executeScript("return self == window.top;"))
		{
			webDriver.switchTo().parentFrame();
		}
	}
	
	
	private void resetHandle()
	{
		attachedBrowsers.remove(mainWindowHandle);
		
		mainWindowHandle = webDriver.getWindowHandle();
		
		attachedBrowsers.put(mainWindowHandle, this);
	}
	
	String getWindowHandle()
	{
		return mainWindowHandle;
	}
	
	private static void refreshAttachedBrowserMap(WebDriver webDriver)
	{
		Set<String> handles = webDriver.getWindowHandles();
		
		if(handles == null || handles.isEmpty())
		{
			attachedBrowsers.clear();
			
			return;
		}
		
		List<String> handlesToRemove = new ArrayList<String>();
		
		for(String attachedHandle : attachedBrowsers.keySet())
		{
			if(!handles.contains(attachedHandle))
			{
				handlesToRemove.add(attachedHandle);
			}
		}
		
		for(String handleToRemove : handlesToRemove)
		{
			attachedBrowsers.remove(handleToRemove);
		}
	}
	
	private static class DriverCloserOnExit extends Thread
	{
		private Set<WebDriver> webDrivers = new HashSet<WebDriver>();
		
		private static DriverCloserOnExit driverCloserOnExit;
		
		private DriverCloserOnExit()
		{
		}
		
		public static synchronized DriverCloserOnExit getDriverCloserOnExit()
		{
			if(driverCloserOnExit == null)
			{
				driverCloserOnExit = new DriverCloserOnExit();
				
				Runtime.getRuntime().addShutdownHook(driverCloserOnExit);
			}
			
			return driverCloserOnExit;
		}
		
		
		public synchronized void add(WebDriver webDriver)
		{
			webDrivers.add(webDriver);
		}

		@Override
		public void run()
		{
			for(WebDriver webDriver : webDrivers)
			{
				try
				{
					webDriver.quit();
				}
				catch(Exception ex)
				{

				}
			}
		}

	}
	
	@SuppressWarnings("unused")
	private void sleep(int millis)
	{
		try 
		{
			Thread.sleep(millis);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}


