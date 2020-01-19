package com.fastautomation.test;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.fastautomation.core.Browser;
import com.fastautomation.core.BrowserType;
import com.fastautomation.core.Element;

public class AccessFrameElement 
{
	public static void main(String[] args) throws InterruptedException 
	{

		System.setProperty("webdriver.chrome.driver","C:\\Softwares\\chromedriver.exe");
		//System.setProperty("webdriver.gecko.driver","D:\\Softwares\\geckodriver.exe");;
		//System.setProperty("webdriver.edge.driver", "D:\\Softwares\\MicrosoftWebDriver.exe");

		//open w3school test example page.
		Browser browser = new Browser(BrowserType.Chrome, "https://www.w3schools.com/tags/tryit.asp?filename=tryhtml_frame_cols");

		Element codeDiv =  browser.findElement(By.cssSelector("div.CodeMirror"));
		
		//click on code area to focus it.
		codeDiv.click();
		
		
		
		Actions builder = new Actions(browser.getWebDriver());
		
		builder.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform(); // ctrl+a on code area  
		
		String text = "<html><body><a href=\"http://www.google.com\" target=\"_blank\">click here</a></body></html>";
		
		builder.sendKeys(text).perform(); //type new html text into code area
		
		
		browser.findElement(By.cssSelector("button.w3-button.w3-bar-item.w3-green")).click(); // click on 'Run' button
		
		/* ** easy way to access frame element, no need to switch driver anymore **** */
		Element link = browser.findFrame(By.id("iframeResult")).findElement(By.tagName("a"));
		
		link.click(); // click on the hyperlink 'click here' appear in next frame area.
		
		/* ** easy way to get handle of newly opend browser **** */
		Browser googleTab = browser.attachAndGetNewWindow(); // get handle of newly open google tab after clicking on hyperlink 'click here'.
		
		googleTab.findElement(By.cssSelector("input[type='text']")).sendKeys("Hello\n"); //type hello on google search box.		

		
		Thread.sleep(5000); //sleep is not necessary here, it is just to see what happened before closing browser tabs.
		
		
		googleTab.close(); // close google tab.
		
		browser.close(); // close w3school test example tab.
	}
}
