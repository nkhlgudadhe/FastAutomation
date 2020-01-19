package com.fastautomation.steps.test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;

import com.fastautomation.components.WebComponent;
import com.fastautomation.core.Browser;
import com.fastautomation.core.Element;
import com.fastautomation.core.Frame;

public class ComplexComponent extends WebComponent{

	@Override
	public void waitUntilLoaded() {
		// TODO Auto-generated method stub
		
	}
	
	public void run() {
		Browser browser = getBrowser();
		Element btn = browser.findElement(By.xpath("//input[@type='button']"));
		System.out.println("4: "+new Date());
		btn.click();
		System.out.println("5: "+new Date());
		btn.click();
		btn.click();
		btn.click();
		btn.click();
		btn.click();
		
		System.out.println("btn.getParent().getText(): "+btn.getParent().getInnerHTML());
		
		Element element =  browser.findElement(By.cssSelector(".class1"));

		element.sendKeys("hello");

		Frame frame = browser.findFrame(By.id("frame1"));
		System.out.println("frame.getParent().getText(): "+frame.getParent().getInnerHTML());
		
		
		frame.getWebDriver().manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		
		frame.getWebDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		
		Element btn2 = frame.findElement(By.xpath("//input[@type='button']"));
		
		btn2.click();
		
		frame.findElement(By.id("id1")).sendKeys("XXXX");
		
		btn2.click();
		btn2.click();
		btn2.click();
		btn2.click();
		
		for(Element elem : frame.findElements(By.cssSelector(".class2")))
		{ 
			elem.sendKeys("Inside Frame TextBox");
		}
		
		
		frame.findElement(By.id("id1")).sendKeys("hello nikhil");

		element.sendKeys("YYYY");
		
		System.out.println("starting at "+new Date());
		
		long startTime = new Date().getTime();
		
		List<Element> elements = browser.findElements(By.cssSelector(".class1"));
		
		System.out.println("come out in "+(new Date().getTime() - startTime) + "millis");
		
		Element lastElement = null;
		
		for(Element elem : elements)
		{ 
			elem.sendKeys("YYYY");
			
			lastElement = elem;
		}
		
		Rectangle rect = lastElement.getRect();
		
		System.out.println("x: "+rect.x+", y : "+rect.y + ", width: "+rect.width+", height: "+rect.height);
	}

	

}
