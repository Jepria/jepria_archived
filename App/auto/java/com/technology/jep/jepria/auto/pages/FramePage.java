package com.technology.jep.jepria.auto.pages;

import static com.technology.jep.jepria.auto.util.WebDriverFactory.getWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.technology.jep.jepria.auto.util.WebDriverFactory;

public abstract class FramePage extends AbstractPage {

  @FindBy(name = "navigation")
  public WebElement navigation;

  @FindBy(name = "content")
  public WebElement content;

  @FindBy(xpath = "//a[contains(@href,'switchLanguage')]")
  private WebElement locale;

  public enum Locale {
      RU, ENG;
  }

  @Override
  public FramePage ensurePageLoaded() {
    WebDriverFactory.getDriver().switchTo().defaultContent();
    getWait().until(presenceOfElementLocated(By.xpath("//frameset")));

    if (verifyLocale(Locale.ENG)) {
      return switchLocale().ensurePageLoaded();
    }

    WebDriverFactory.getDriver().switchTo().defaultContent();
    return this;
  }

  public FramePage getNavigation() {
    WebDriverFactory.getDriver()
        .switchTo()
        .defaultContent()
        .switchTo()
        .frame(navigation);
    return this;
  }

  public FramePage getContent() {
    WebDriverFactory.getDriver()
        .switchTo()
        .defaultContent()
        .switchTo()
        .frame(content);
    return this;
  }
    
  public WebElement waitContentElementToLoad(By by) {
    return getWait().until(presenceOfElementLocated(by));
  }

  public boolean verifyLocale(Locale locale) {
    this.getNavigation();

    if (locale.equals(Locale.RU) && this.locale.getText().equals("English")) {
      return true;
    }

    if (locale.equals(Locale.ENG) && this.locale.getText().equals("Русский")) {
      return true;
    }

    return false;
  }

  public FramePage switchLocale() {
    this.ensurePageLoaded().getNavigation().locale.click();
    return this.ensurePageLoaded().getNavigation();
  }
}
