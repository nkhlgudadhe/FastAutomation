package com.fastautomation.test;

import org.openqa.selenium.By;

import com.fastautomation.core.Browser;
import com.fastautomation.core.BrowserType;
import com.fastautomation.core.Element;

public class SerachGitHubProjectTest 
{
	public static void main(String[] args) throws InterruptedException 
	{	
		System.setProperty("webdriver.chrome.driver","D:\\Softwares\\chromedriver.exe");
		//System.setProperty("webdriver.gecko.driver","D:\\Softwares\\geckodriver.exe");;
		//System.setProperty("webdriver.edge.driver", "D:\\Softwares\\MicrosoftWebDriver.exe");

		Browser browser = new Browser(BrowserType.Chrome, "https://github.com");

		Element searchField =  browser.findElement(By.cssSelector(".form-control.header-search-input"));

		searchField.sendKeys("FastAutomation\n");

		Element syncedWebAutomationTitle = browser.findElement(By.xpath("//em[text()='FastAutomation']"));
		
		syncedWebAutomationTitle.click();

		Element title = browser.findElement(By.cssSelector("strong[itemprop='name']")).findElement(By.tagName("a"));
		

		if(title.getText().equals("FastAutomation"))
		{
			System.out.println("FastAutomation project opened!");
		}
		else
		{
			System.out.println("Unable to open FastAutomation project");
		}
		
		Thread.sleep(5000); //sleep is not necessary here, it is just to see what happened before closing browser tab.

		browser.close();

		System.out.println("browser is closed!");
	}
}
