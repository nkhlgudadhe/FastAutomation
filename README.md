# FastAutomation
Fast Automation is selenium based webautomation for easy creation of webautomation scripts.

# Features
1) The important feature of framework, it handles automatic switching of webdriver based on scope of the element to be actioned .
2) Stepwise execution of scenario .
3) Easy creation of Automation Scenario and Scenario step by using simple annotations and without writting any code for any class or object bindings .
4) Easy creation of webcomponent objection for automation.
4) Defining execution control flow scenario.js which simplifies the logic and hide complexity for the user .
5) Automatic handle of log management for every action on webpage . which helps in proper debuging and find root cause of the issue in case any future failures .
6) Html report creation . Prepare full logs as well as stepwise log and status. Also automatically captures screenshot during any execution failure .

# How to use
Please refer FastAutomationTest to check sample code for use cases .
1) Create WebComponents
		Create  Custom WebComponents for Page or for logical sections in the web page .
		e.g. Page Component -> MathOperationPage
		     Sectional component in page -> AddiationPanel, SubstarctionPanel, OperationPanel etc.
2) Create Scenario Steps
		Create scenario steps to test web page and different sections in it .
		e.g. MathScenarioSteps annotated with @ScenarioConetxt and provided scenario name as "MathScenario"
		     Above class has multiple methods annotated with @Step in nothing but scenario steps to test each components in the page.
		      
3) Create Test Scenario
		To call above created steps in proper flow by passing custom inputs , create Scenario.js 

4) Run Scenario 
		Run above scenario by running com.fastautomation.components.WebStarter by passing following arguments
		-browser browser -scenario scenario -url url  [-max_wait max_wait]
		where
		 -browser <arg>    target browser among (Chrome, FireFox, Edge)
		 -max_wait <arg>   maximum waiting time in sec for element to appear
		 -output <arg>     output folder for html logs
		 -scenario <arg>   Scenario.js file path
		 -url <arg>        starting url
		
		Also need to pass following jvm argument for target browser type :
		For Chrome : -Dwebdriver.chrome.driver=<chromedriver.exe path>
		For Firefox : -Dwebdriver.gecko.driver=<geckodriver.exe path>
		For Edge :    -Dwebdriver.edge.driver=<MicrosoftWebDriver.exe path>

