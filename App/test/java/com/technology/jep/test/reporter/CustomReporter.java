package com.technology.jep.test.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class CustomReporter implements IReporter {
  private PrintWriter outputFile;
  /**
   * Generate a report for the given suites into the specified output directory.
   */
  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    
    new File(outputDirectory).mkdirs();
    try {
      outputFile = new PrintWriter(new BufferedWriter(new FileWriter(new File(
          outputDirectory, "custom-report.txt"))));
    } catch (IOException e) {
      System.out.println("Error in creating writer: " + e);
    }
    
    print("*************************************************************************");
    print("*************************************************************************");
    print("                                                                         ");
    print("                   BEGIN Custom Report                                   ");
    print("                                                                         ");
    print("             View this report in test-output/custom-report.txt           ");
    print("                     or full info in test-output/index.html              "); 
    print("                                                                         ");
    print("*************************************************************************");
    print("*************************************************************************");
   
    print("Suites run: " + suites.size());
    for (ISuite suite : suites) {
      print("Suite>" + suite.getName());
      Map<String, ISuiteResult> suiteResults = suite.getResults();
      for (String testName : suiteResults.keySet()) {
        print(""); //new line
        print(""); //new line
        print("**Test>" + testName);
        ISuiteResult suiteResult = suiteResults.get(testName);
        ITestContext testContext = suiteResult.getTestContext();

        //passed status
        IResultMap passResult = testContext.getPassedTests();
        Set<ITestResult> testsPassed = passResult.getAllResults();
        print("    OK|" + testsPassed.size());
        for (ITestResult testResult : testsPassed) {

          print("      "
              + this.getScenarioName(testResult)
              + " |took "
              + (testResult.getEndMillis() - testResult
                  .getStartMillis()) + "ms");
        }
        
        //skipped status
        IResultMap skippedResult = testContext.getSkippedTests();
        Set<ITestResult> testsSkipped = skippedResult.getAllResults();
        print("    Skipped|" + testsSkipped.size());
        for (ITestResult testResult : testsSkipped) {
          print("      " + this.getScenarioName(testResult));
        }
        
        //failed status with exceptions
        //TODO: sort test results??
        print("    Failed|" + testContext.getFailedTests().size());
        IResultMap failedResult = testContext.getFailedTests();
        Set<ITestResult> testsFailed = failedResult.getAllResults();
        for (ITestResult testResult : testsFailed) {
          
          String throwableMessage = testResult.getThrowable().toString();
          int newLineIndex = throwableMessage.indexOf("\n"); 
          if(newLineIndex != -1){
            throwableMessage = throwableMessage.substring(0, newLineIndex) +
                "... more info view in output files.";
          }
          
          print("       " + this.getScenarioName(testResult));
          print("          " + throwableMessage);

          Object[] parameters = testResult.getParameters();
          for(int i=0; i<parameters.length; i++){
            print("             Parameter: " + parameters[i]);
          }
          
          print(""); //new line
        }
      }
    }
    print("*************************************************************************");
    print("*************************************************************************");
    print("                                                                         ");
    print("                     END Custom Report                                   ");
    print("                                                                         ");
    print("*************************************************************************");
    print("*************************************************************************");
    outputFile.flush();
    outputFile.close();
  }

  private String getScenarioName(ITestResult testResult){
    String scenarioName = testResult.getMethod().getDescription();
    if(scenarioName == null) scenarioName = testResult.getName();
    return scenarioName;
  }
  
  private void print(String text) {
    System.out.println(text);
    outputFile.println(text);
  }
}