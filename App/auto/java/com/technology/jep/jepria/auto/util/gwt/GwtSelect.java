package com.technology.jep.jepria.auto.util.gwt;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.technology.jep.jepria.auto.pages.JepRiaApplicationPage;
import com.technology.jep.jepria.auto.pages.PageManagerBase;

public class GwtSelect<P extends PageManagerBase> {

    private JepRiaApplicationPage<P> page;

    /*private WebElement inputField;
    private String inputXpath = "/following-sibling::div//input";*/

    private WebElement arrowBtn;
    private String arrowXpath = "/following-sibling::div//img";

    private WebElement optionBtn;
    private String valueXpath = "//div[contains(@class,'x-combo-list')]/div[text()='";

    public GwtSelect(JepRiaApplicationPage<P> page, String xpath) {
        this.page = page;
        /*inputField = page.waitContentElementToLoad(By.xpath(xpath + inputXpath))
                .findElement(By.xpath(xpath + inputXpath));*/
        arrowBtn = page.waitContentElementToLoad(By.xpath(xpath + arrowXpath))
                .findElement(By.xpath(xpath + arrowXpath));
    }

    public JepRiaApplicationPage<P> selectOption(String option) {
        arrowBtn.click();
        optionBtn = page.waitContentElementToLoad(By.xpath(valueXpath + option + "']"));
        optionBtn.click();
        return this.page.ensurePageLoaded();
    }
}