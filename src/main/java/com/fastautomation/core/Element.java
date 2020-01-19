package com.fastautomation.core;

import java.awt.AWTException;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.fastautomation.components.Logger;

public class Element 
{
	private By elementSelector;
	private Element parentElement;
	private static long nextElementIdx ;
	protected String javaScriptRef;
	private String jsRefDescription;
	
	private static final Logger logger = new Logger(Element.class);
	
	protected Element(Element parentElement)
	{
		this.parentElement = parentElement;
	}
	
	Element(Element parentElement, By elementSelector)
	{
		this.parentElement = parentElement;
		this.elementSelector = elementSelector;
	}
	
	@Override
	public String toString() 
	{
		return (this instanceof Frame ? "Frame" : "Element") + " ( "+ elementSelector + " )";
	}
	
	Element createRelatedElement(boolean useSelfAsContainer, WebElement webElement)
	{
		Frame containerFrame = null;
		
		if(useSelfAsContainer && this instanceof Frame)
		{
			containerFrame = (Frame) this;
		}
		else
		{
			containerFrame = getContainerFrame();
		}
		
		String tagName = webElement.getTagName();
		
		By selector = null;
		
		Element element = null;
		
		if(tagName.equalsIgnoreCase("frame") || tagName.equalsIgnoreCase("iframe"))
		{
			element = new Frame(containerFrame, selector);
		}
		else
		{
			element = new Element(containerFrame, selector);
		}
		
		return element;
	}

	public By getSelector()
	{
		return elementSelector;
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
	
	public Element findElement(By by)
	{
		return new Element(this, by);
	}
	
	public List<Element> findElements(By by)
	{
		Element element = new Element(this, by);
		
		return element.getElements();
	}
	
	
	
	public Frame findFrame(By by)
	{
		return new Frame(this, by);
	}
	
	public List<Frame> findFrames(By by)
	{
		Frame frame = new Frame(this, by);
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
	
	
	public WebElement getSeleniumWebElement()
	{
		Frame frame = getContainerFrame();

		WebDriver webDriver = frame.switchDriverToFrame();
		
		WebElement webElement = getWebElement(webDriver);
		
		return webElement;
	}
	
	public void waitUntilExists()
	{
		printStartLog();
		getSeleniumWebElement();
		printEndLog();
	}
	
	public void waitUntilRemoved()
	{
		printStartLog();
		
		getSeleniumWebElement();
		
		JavascriptExecutor executor = (JavascriptExecutor) getBrowser().getCurrentWebDriver();
		
		for(int i=0 ; i < Browser.getTimeoutInSeconds(); i++)
		{
			Boolean isExists = (Boolean) executor.executeScript("var jsElement = document."+javaScriptRef+"; var result = document.body == jsElement || document.body.contains(jsElement); return result;");
			
			if(!isExists)
			{
				printEndLog();
				return;
			}

			sleep(1000);
		}
		
		throw new RuntimeException("element '" + this.toString() + "' still exists after " + Browser.getTimeoutInSeconds() + " seconds");
	}
	
	
	/* ********************************** General selenium useful functions ****************************** */
	
	public void click()
	{
		printStartLog();
		getSeleniumWebElement().click();
		printEndLog();
	}
	
	public Element getParent()
	{
		printStartLog();
		
		Frame frame  = getContainerFrame();
		
		Element parent = (Element) frame.executeScript("return arguments[0].parentElement;", getSeleniumWebElement());
		
		parent.jsRefDescription = getElementTrack() + ".parent()";
		
		//parent.parentElement = this;
		
		printEndLog();
		
		return parent;
	}
	
	public String getInnerHTML() {
		Frame frame  = getContainerFrame();
		return (String) frame.executeScript("return arguments[0].innerHTML;", getSeleniumWebElement());
	}
	
	public void clear()
	{
		printStartLog();
		getSeleniumWebElement().clear();
		printEndLog();
	}
	
	public void sendKeys(CharSequence ... keysToSend)
	{
		printStartLog();
		getSeleniumWebElement().sendKeys(keysToSend);
		printEndLog();
	}
	
	public String getAttribute(String name)
	{
		printStartLog();
		String attrValue = getSeleniumWebElement().getAttribute(name);
		printEndLog();
		return attrValue;
	}
	
	public String getCssValue(String propertyName)
	{
		printStartLog();
		String cssValue = getSeleniumWebElement().getCssValue(propertyName);
		printEndLog();
		return cssValue;
	}
	
	public String getText()
	{
		printStartLog();
		String text = getSeleniumWebElement().getText();
		printEndLog();
		return text;
	}
	
	public String getTagName()
	{
		printStartLog();
		String name = getSeleniumWebElement().getTagName();
		printEndLog();
		return name;
	}
	
	public void submit()
	{
		printStartLog();
		getSeleniumWebElement().submit();
		printEndLog();
	}
	
	public boolean isDisplayed()
	{
		printStartLog();
		boolean displayed = getSeleniumWebElement().isDisplayed();
		printEndLog();
		return displayed;
	}
	
	public boolean isEnabled()
	{
		printStartLog();
		boolean enabled = getSeleniumWebElement().isEnabled();
		printEndLog();
		return enabled;
	}
	
	public boolean isSelected()
	{
		printStartLog();
		boolean selected = getSeleniumWebElement().isSelected();
		printEndLog();
		return selected;
	}
	
	public Dimension getSize()
	{
		printStartLog();
		Dimension dimension = getSeleniumWebElement().getSize();
		printEndLog();
		return dimension;
	}
	
	
	public Point getLocation()
	{
		printStartLog();
		Point point = getSeleniumWebElement().getLocation();
		printEndLog();
		return point;
	}
	
	public <X> X getScreenshotAs(OutputType<X> target)
	{
		printStartLog();
		X x = getSeleniumWebElement().getScreenshotAs(target);
		printEndLog();
		return x;
	}
	
	public Rectangle getRect()
	{	
		printStartLog();
		WebElement webElement = getSeleniumWebElement();
		
		List<Object> list = (List<Object>) getContainerFrame().executeScript("var rect = arguments[0].getBoundingClientRect(); var arr = [rect.left, rect.top, rect.width, rect.height ]; return arr;", webElement);
		
		for(int i=0; i < list.size(); i++)
		{
			if(list.get(i) instanceof Long)
			{
				list.set(i, (int)(long)(Long)list.get(i));
			}
			else if(list.get(i) instanceof Double)
			{
				list.set(i, (int)(double)(Double)list.get(i));
			}
		}
		
		Rectangle rectangle = new Rectangle((int)(Integer) list.get(0), (int)(Integer) list.get(1), (int)(Integer) list.get(3), (int) (Integer)list.get(2));
		printEndLog();
		return rectangle;
	}
	
	/* *************************** Drag And Drop methods *************************************** */
	
	
	public void dragAndDropTo(Element tgtElement) throws AWTException, InterruptedException
	{
		dragAndDropTo(this, tgtElement, null, null);
	}
	
	public void dragAndDropTo(Element tgtElement, int xOffset, int yOffset) throws AWTException, InterruptedException
	{
		dragAndDropTo(this, tgtElement, xOffset, yOffset);
	}
	
	public void dragAndDropTo(int xOffset, int yOffset) throws AWTException, InterruptedException
	{
		dragAndDropTo(this, null, xOffset, yOffset);
	}
	
	private void dragAndDropTo(Element srcElement, Element tgtElement, Integer xOffset, Integer yOffset)
	{
		printStartLog();
		
		WebElement sourceWebElement = srcElement.getSeleniumWebElement();
		
        Actions builder = new Actions(srcElement.getBrowser().getCurrentWebDriver());
        
        builder.clickAndHold(sourceWebElement).perform();
        
        if(tgtElement != null)
        {
        	builder.moveByOffset(20, 20);
        	
        	WebElement targetWebElement = tgtElement.getSeleniumWebElement();
        	
        	if(xOffset == null || yOffset == null)
        	{
        		builder.moveToElement(targetWebElement);
        	}
        	else
        	{
        		builder.moveToElement(targetWebElement, xOffset, yOffset);
        	}
        	
        }
        else if (xOffset != null && yOffset != null)
        {
        	builder.moveByOffset(xOffset, yOffset); 
        }
        
        builder.release().perform();
        
        printEndLog();
	}
	
	
	/* ************************************* private required core methods ********************************************** */
	
	private Browser getBrowser()
	{
		Frame frame = this instanceof Frame ? (Frame) this : getContainerFrame();
		return frame.getBrowser();
	}
	
	Frame getContainerFrame()
	{
		Element element = this.parentElement;
		
		while(element != null && !(element instanceof Frame))
		{
			element = element.parentElement;
		}
		
		return (Frame) element;
	}

	Element getParentElement()
	{
		return parentElement;
	}

	private List<WebElement> getWebElements(WebDriver currentWebDriver, WebElement perentWebElement, boolean throwException)
	{
		try
		{
			List<WebElement> webElements = null;
			
			By selector = getSelector();
			
			if(perentWebElement != null)
			{
				webElements = perentWebElement.findElements(selector);
			}
			else
			{
				webElements = currentWebDriver.findElements(selector);
			}
			
			if(webElements != null && !webElements.isEmpty())
			{
				return webElements;
			}
		}
		catch(InvalidSelectorException ex)
		{
			throw new RuntimeException("Invalid selector " + this.toString());
		}

		if(throwException)
		{			
			throw new RuntimeException("unable to find " + this.toString() + " in " +  Browser.getTimeoutInSeconds()  + " seconds");
		}
		else
		{
			return new LinkedList<>();
		}
	}	
	
	List<Element> getElementPathInFrame()
	{
		List<Element> elements = new LinkedList<Element>();
		
		Element elementToAdd = this;

		while(elementToAdd != null)
		{
			elements.add(0, elementToAdd);

			elementToAdd = elementToAdd.getParentElement();

			if(elementToAdd instanceof Frame)
			{
				break;
			}
		}
		
		return elements;
	}
	
	protected WebElement getWebElement(WebDriver webDriver)
	{		
		WebElement webElement = getWebElementFromJavaScriptRef(webDriver);
		
		if(webElement != null)
		{
			return webElement;
		}
		
		if(getSelector() == null)
		{
			throw new RuntimeException("Element is removed from DOM");
		}
		
		List<Element> elements = getElementPathInFrame();
		
		WebElement parentWebElement = null;
		
		for(int i= elements.size() - 2 ; i >= 0 ; i--)
		{
			Element element = elements.get(i);
			
			parentWebElement = element.getWebElement(webDriver);
		}
		
		webElement = elements.get(elements.size() - 1).getWebElements(webDriver, parentWebElement, true).get(0);
		
		updateJavaScriptReference(webDriver, webElement);
		
		return webElement;
	}
	
	
	
	private WebElement getWebElementFromJavaScriptRef(WebDriver webDriver)
	{
		WebElement webElement = null;
		
		if(javaScriptRef != null)
		{
			JavascriptExecutor executor =  (JavascriptExecutor) webDriver;
			
			webElement = (WebElement) executor.executeScript("var jsElement = document."+javaScriptRef+"; if( document.body == jsElement || document.body.contains(jsElement) ) { return jsElement; }");
		}
		
		return webElement;
	}
	
	void updateJavaScriptReference(WebDriver webDriver, WebElement webElement)
	{
		JavascriptExecutor executor =  (JavascriptExecutor) webDriver;
		
		if(this.javaScriptRef == null)
		{
			String javaScriptRef = "fastAutomationElement_"+(++nextElementIdx);
			
			executor.executeScript("document."+javaScriptRef+"=arguments[0]", webElement);
			
			this.javaScriptRef = javaScriptRef;
		}
		else
		{
			executor.executeScript("if(document."+this.javaScriptRef+" != arguments[0]){ document."+this.javaScriptRef+" = arguments[0]; }", webElement);
		}
	}

	List<Element> getElements()
	{
		Frame frame = getContainerFrame();
		
		WebDriver webDriver = frame.switchDriverToFrame();
		
		List<Element> elements = getElementPathInFrame();
		
		WebElement parentWebElement = null;
		
		if(elements.size() > 1)
		{
			parentWebElement = elements.get(elements.size() - 2).getWebElement(webDriver);
		}
		
		List<WebElement> webElements = elements.get(elements.size() - 1).getWebElements(webDriver, parentWebElement, false);
		
		List<Element> newElements = new LinkedList<Element>();

		int childCount = 0;
		String elemTrack = getElementTrack();
		for(WebElement webElement : webElements)
		{
			Element element = createRelatedElement(false, webElement);
			
			element.updateJavaScriptReference(webDriver, webElement);
			element.jsRefDescription =  elemTrack + "["+(childCount++)+"]";
			
			newElements.add(element);
		}
		
		return newElements;
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
	
	private void printStartLog() {
		
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		String methodPath = getElementTrack()+"."+methodName+"()";
		logger.debug("Calling {}", methodPath);
	}
	
	private void printEndLog() {
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		String methodPath = getElementTrack()+"."+methodName+"()";
		logger.debug("Finished {}", methodPath);
	}
	
	private String getElementTrack() {
		
		StringBuilder strBuilder = new StringBuilder();
		if(parentElement != null) {
			String desc = parentElement.getElementTrack();
			if(!desc.isEmpty()) {
				strBuilder.append(desc).append(".");
			}			
		}
		
		String desc = getElementDesc();
		if(desc != null) {
			strBuilder.append(getElementDesc());
		}
		
		return strBuilder.toString();
	}
	
	private String getElementDesc() {
		String objName = this instanceof Frame ? "frame" : "element" ;
		String desc = getSelector() == null ? 
				javaScriptRef == null ?
						null :  jsRefDescription != null ? 
								jsRefDescription : objName + "(jsRef: " + javaScriptRef + ")" : objName + "("+getSelector().toString() +")";
		return desc == null ? null : desc;
	}
}
