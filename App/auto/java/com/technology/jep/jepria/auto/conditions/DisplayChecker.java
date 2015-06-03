package com.technology.jep.jepria.auto.conditions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Проверка отображения Web-элемента, заданного id
 */
public class DisplayChecker implements ConditionChecker {
	
	private String id;
	private WebDriver wd;

	public DisplayChecker(WebDriver wd, String id) {
		this.wd = wd;
		this.id = id;
	}
	
	@Override
	public boolean isSatisfied() {
		WebElement el;
		try {
			el = wd.findElement(By.id(id));
		} catch (Exception r) {
			return false;
		}
		
		return el.isDisplayed();
	}

	public String getId() {
		return id;
	}
}
