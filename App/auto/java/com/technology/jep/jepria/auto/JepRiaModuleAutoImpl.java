package com.technology.jep.jepria.auto;

import static com.technology.jep.jepria.auto.util.WebDriverFactory.getWait;
import static com.technology.jep.jepria.client.AutomationConstant.ALERT_MESSAGEBOX_ID;
import static com.technology.jep.jepria.client.AutomationConstant.CONFIRM_MESSAGEBOX_ID;
import static com.technology.jep.jepria.client.AutomationConstant.CONFIRM_MESSAGE_BOX_YES_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.ERROR_MESSAGEBOX_ID;
import static com.technology.jep.jepria.client.AutomationConstant.GRID_BODY_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.GRID_HEADER_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_CARD_TYPE_HTML_ATTR;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_CARD_TYPE_VALUE_EDTB;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_CARD_TYPE_VALUE_VIEW;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_COMBO_BOX_FIELD_DROPDOWN_BTN_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_COMBO_BOX_FIELD_MENU_ITEM_INFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_COMBO_BOX_FIELD_POPUP_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_DUAL_LIST_FIELD_LEFTPART_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_DUAL_LIST_FIELD_MENU_ITEM_INFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_DUAL_LIST_FIELD_MOVEALLLEFT_BTN_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_DUAL_LIST_FIELD_MOVERIGHT_BTN_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_FIELD_ALLOW_BLANK_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_FIELD_INPUT_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_LIST_FIELD_CHECKALL_POSTFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_LIST_FIELD_ITEM_CHECKBOX_INFIX;
import static com.technology.jep.jepria.client.AutomationConstant.JEP_OPTION_VALUE_HTML_ATTR;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_ADD_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_DELETE_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_EDIT_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_FIND_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_LIST_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_SAVE_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_SEARCH_BUTTON_ID;
import static com.technology.jep.jepria.client.AutomationConstant.TOOLBAR_VIEW_DETAILS_BUTTON_ID;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.EDIT;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.SEARCH;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.SELECTED;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.VIEW_DETAILS;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.VIEW_LIST;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.technology.jep.jepria.auto.conditions.ConditionChecker;
import com.technology.jep.jepria.auto.conditions.DisplayChecker;
import com.technology.jep.jepria.auto.conditions.ExpectedConditions;
import com.technology.jep.jepria.auto.conditions.TextChangeChecker;
import com.technology.jep.jepria.auto.entrance.ApplicationEntranceAuto;
import com.technology.jep.jepria.auto.entrance.EntranceAppAuto;
import com.technology.jep.jepria.auto.entrance.pages.JepRiaApplicationPageManager;
import com.technology.jep.jepria.auto.exceptions.AutomationException;
import com.technology.jep.jepria.auto.exceptions.NotExpectedException;
import com.technology.jep.jepria.auto.exceptions.WrongOptionException;
import com.technology.jep.jepria.auto.widget.field.Field;
import com.technology.jep.jepria.auto.widget.statusbar.StatusBar;
import com.technology.jep.jepria.auto.widget.statusbar.StatusBarImpl;
import com.technology.jep.jepria.client.ui.WorkstateEnum;
import com.technology.jep.jepria.shared.exceptions.NotImplementedYetException;
import com.technology.jep.jepria.shared.exceptions.UnsupportedException;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

/**
 * Реализация JepRiaModuleAuto
 */
public class JepRiaModuleAutoImpl<A extends EntranceAppAuto, P extends JepRiaApplicationPageManager> extends ApplicationEntranceAuto<A, P>
		implements JepRiaModuleAuto {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(JepRiaModuleAutoImpl.class.getName());
	private WorkstateEnum currentWorkstate;
	private StatusBar statusBar;
	
	public JepRiaModuleAutoImpl(A app, P pageManager) {
		super(app, pageManager);
	}
	
	public JepRiaModuleAutoImpl(A app, P pageManager, WorkstateEnum initialWorkstate) {
		this(app, pageManager);
		currentWorkstate = initialWorkstate;
	}

	@Override
	public StatusBar getStatusBar() {
		if(statusBar == null) {
			statusBar = new StatusBarImpl<P>(pages);
		}
		
		return statusBar;
	}
	
	public void clickButton(String buttonId) {
		getWait().until(elementToBeClickable(By.id(buttonId)));
		this.pages.getApplicationPage().getWebDriver().findElement(By.id(buttonId)).click();
	}
	
	@Override
	public void find(Map<String, String> template) {
		if(getCurrentWorkstate() != VIEW_LIST) {
			setWorkstate(SEARCH);
			fillFields(template);
			
			String statusBarTextBefore = getStatusBar().getText();
			clickButton(getToolbarButtonId(VIEW_LIST));
	        waitTextToBeChanged(getStatusBar(), statusBarTextBefore);
	        
	        setCurrentWorkstate(VIEW_LIST);
		}
	}
	
	@Override
	public void find() {
		pages.getApplicationPage().ensurePageLoaded();
		
		getWait().until(elementToBeClickable(By.id(TOOLBAR_FIND_BUTTON_ID)));
		
		pages.getApplicationPage().findButton.click();
	}

	@Override
	public void setWorkstate(WorkstateEnum workstateTo) {
		pages.getApplicationPage().ensurePageLoaded();
		
		if(!getCurrentWorkstate().equals(workstateTo))  {
			String toolbarButtonId = getToolbarButtonId(workstateTo);
			if(toolbarButtonId != null) {
				
				String statusBarTextBefore = getStatusBar().getText();
				clickButton(toolbarButtonId);
		        waitTextToBeChanged(getStatusBar(), statusBarTextBefore);
		        
		        setCurrentWorkstate(workstateTo);
			} else {
				throw new UnsupportedException("Wrong transition: " + this.getCurrentWorkstate() + "->" + workstateTo);
			}
		}
	}

	@Override
	public void edit(Map<String, String> template) {
		find(template);
		
        selectItem(0);
        
		String toolbarButtonId = getToolbarButtonId(EDIT);

		String statusBarTextBefore = getStatusBar().getText();
		clickButton(toolbarButtonId);
        waitTextToBeChanged(getStatusBar(), statusBarTextBefore);
        
        setCurrentWorkstate(EDIT);
	}

	@Override
	public void delete(Map<String, String> key) {
		try {
	        selectItem(key);
	        
			String statusBarTextBefore = getStatusBar().getText();
			clickButton(TOOLBAR_DELETE_BUTTON_ID);
			
			assert checkMessageBox(CONFIRM_MESSAGEBOX_ID);
			
			clickButton(CONFIRM_MESSAGE_BOX_YES_BUTTON_ID);
			
	        waitTextToBeChanged(getStatusBar(), statusBarTextBefore);
	        
	        setCurrentWorkstate(VIEW_LIST);
		} catch(IndexOutOfBoundsException ex) {
			// Нормально для случая отсутствия записи с ключом key
		}
	}

	
	public void selectItem(Map<String, String> key) {
		find(key);
        selectItem(0);
	}

	public void selectItem(int index) {
		assert getCurrentWorkstate() == VIEW_LIST;

		WebDriver wd = pages.getApplicationPage().getWebDriver();
		int size = 0;
		int i = 0;
//		List<WebElement> elements = wd.findElements(By.id(LIST_FORM_GRID_ROW_ID)); От этого пока отказались 
		List<WebElement> elements = wd.findElements(By.className("GC2PVC0CPI")); // На GWT 2.6.1 работает
		
		size = elements.size();
		while(true) { // TODO Избавиться от sleep
			if(size > 0 || i++ == 20 ) break;
			sleep(100);
//			elements = wd.findElements(By.id(LIST_FORM_GRID_ROW_ID));  От этого пока отказались
			elements = wd.findElements(By.className("GC2PVC0CPI"));  // На GWT 2.6.1 работает 
			size = elements.size();
		}
		if(index  < size) {
			WebElement item = elements.get(index);
			getWait().until(elementToBeClickable(item));
			new Actions(wd).clickAndHold(item).release().perform();
	        setCurrentWorkstate(SELECTED);
		} else {
			throw new IndexOutOfBoundsException("index = " + index + ", size = " + size);
		}
	}

	private void sleep(int msc) {
		try {
			Thread.sleep(msc);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void fillFields(Map<String, String> fieldMap) {
		for(String fieldName: fieldMap.keySet()) {
			setFieldValue(fieldName, fieldMap.get(fieldName));
		}
	}

	@Override
	public void setFieldValue(String fieldId, String value) {
		pages.getApplicationPage().ensurePageLoaded();
		
		WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(fieldId + JEP_FIELD_INPUT_POSTFIX)); 
		getWait().until(elementToBeClickable(fieldInput));
		
		String del = Keys.chord(Keys.CONTROL, "a") + Keys.DELETE; 
		fieldInput.sendKeys(del + value);
	}

	@Override
	public String getFieldValue(String fieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(fieldId + JEP_FIELD_INPUT_POSTFIX));
		return fieldInput.getAttribute("value");
	}
	
	@Override
	public String[] getDualListFieldValues(String jepDualListFieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// Получаем список всех опций внутри INPUT'а заданного поля (INPUT это правый список)
	    List<WebElement> options = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
	    		String.format("//*[@id='%s']//option",
	    				jepDualListFieldId + JEP_FIELD_INPUT_POSTFIX)));
	    
	    List<String> ret = new ArrayList<String>();
	    
	    // Проходим по полученному списку, и, для каждой опции, получаем ее option-value
	    for (WebElement option: options) { 
	        ret.add(option.getAttribute(JEP_OPTION_VALUE_HTML_ATTR));
	    }
	    return ret.toArray(new String[ret.size()]);
	}
	
	@Override
	public void selectComboBoxMenuItem(String comboBoxFieldId, String menuItem) {
		// FIXME А что если опции комбо-бокса загружаются не лениво, а не лениво? То есть возможно, тестирование комбо-бокса начнется
		// до того (например, если оно стоит первым в сценарии), как успеют загрузиться опции... Ведь в этом методе завязка идет на то,
		// что опции грузятся лениво.
		selectComboBoxMenuItem(comboBoxFieldId, menuItem, false, menuItem.length());
	}
	
	@Override
	public void selectComboBoxMenuItemWithCharByCharReloadingOptions(String comboBoxFieldId, String menuItem, int minInputLength) {
		// 1 is the minimal possible value
		selectComboBoxMenuItem(comboBoxFieldId, menuItem, true, Math.max(1, minInputLength));
	}
	
	//TODO унифицировать простые и сложные поля через get/set-FieldValue() - Нужна иерархия полей ! 
	private void selectComboBoxMenuItem(String comboBoxFieldId, String menuItem,
			final boolean charByCharReloadingOptions, final int minInputLength) { // TODO Поддержать локализацию
		pages.getApplicationPage().ensurePageLoaded();
		
		// Подготовка: в случае, если на данный JepComboBoxField навешена загрузка опций по первому использованию,
		// то нажимаем на кнопку 'развернуть' для того, чтобы загрузка произошла, и ждём окончания загрузки опций.
		final WebElement dropDownButton = pages.getApplicationPage().getWebDriver().findElement(By.id(comboBoxFieldId + JEP_COMBO_BOX_FIELD_DROPDOWN_BTN_POSTFIX)); 
		getWait().until(elementToBeClickable(dropDownButton));
		dropDownButton.click();
		
		// Отслеживаем появление suggestBoxPopup, то есть когда список опций загрузится.
		final By comboBoxPopupPanelBy = By.id(comboBoxFieldId + JEP_COMBO_BOX_FIELD_POPUP_POSTFIX);
		getWait().until(presenceOfElementLocated(comboBoxPopupPanelBy));
		
		
		// Непосредственно поиск и выбор элемента в списке.
		// Очистим поле ввода.
		final WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(comboBoxFieldId + JEP_FIELD_INPUT_POSTFIX)); 
		getWait().until(elementToBeClickable(fieldInput));
		String del = Keys.chord(Keys.CONTROL, "a") + Keys.DELETE; 
		fieldInput.sendKeys(del);
		
		// Если menuItemText короче firstInputLength, сообщаем об ошибке
		if (minInputLength > menuItem.length()) {
			// Закроем открытый список опций.
			dropDownButton.click();
			
			throw new WrongOptionException("Wrong combobox option: [" + menuItem + "], "
					+ "the input must be at least " + minInputLength + " chars.");
		}
		// Пишем в поле Комбо-бокса начальные символы (в случае с простой однократной загрузкой списка опций - весь текст сразу).
		fieldInput.sendKeys(menuItem.substring(0, minInputLength));
		
		if (charByCharReloadingOptions) {
			// Закроем открытый список опций.
			dropDownButton.click();
			
			// Отслеживаем появление suggestBoxPopup, то есть когда список опций загрузится.
			getWait().until(presenceOfElementLocated(comboBoxPopupPanelBy));
		}
		
		int ind = minInputLength;
		WebElement comboBoxMenuItem;
		 
		// Продолжаем набирать оставшийся текст по одной букве, ожидая подгрузки списка, до тех пор пока либо не закончатся буквы,
		// либо опция не будет найдена в списке (в случае с простой однократной загрузкой списка опций - выход из цикла происходит сразу).
		while (true) {
			try {
				// Важно! лоцировать comboBoxPopupPanel нужно именно в каждой итерации цикла, несмотря на то, что элемент вроде не изменяется внутри цикла.
				comboBoxMenuItem = pages.getApplicationPage().getWebDriver().findElement(By.xpath(
						String.format("//*[@id='%s']//*[starts-with(@id, '%s') and @%s='%s']",
								comboBoxFieldId + JEP_COMBO_BOX_FIELD_POPUP_POSTFIX,
								comboBoxFieldId + JEP_COMBO_BOX_FIELD_MENU_ITEM_INFIX,
								JEP_OPTION_VALUE_HTML_ATTR,
								menuItem)));
				// Исключения не было - значит, опция найдена в списке на данном шаге.
				break;
			} catch (NoSuchElementException e) {
				
				// Опции не найдено в списке на данном шаге - значит, вводим следующую букву.
				if (ind < menuItem.length()) {
					fieldInput.sendKeys(Character.toString(menuItem.charAt(ind++)));
					
					// Закроем открытый список опций.
					dropDownButton.click();
					
					// Отслеживаем появление suggestBoxPopup, то есть когда список опций загрузится.
					getWait().until(presenceOfElementLocated(comboBoxPopupPanelBy));
				} else {
					// Закроем открытый список опций.
					dropDownButton.click();
					
					// Букв не осталось - значит, опции в списке нет.
					throw new WrongOptionException("Wrong combobox option: [" + menuItem + "]");
				}
			}
		}
		
		// Выбираем найденную опцию.
		getWait().until(elementToBeClickable(comboBoxMenuItem));
		comboBoxMenuItem.click();
	}
	
	@Override
	public void selectDualListMenuItems(String dualListFieldId, String[] menuItems) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// Очищаем правый список (переносим все влево)
		final WebElement moveAllLeftButton = pages.getApplicationPage().getWebDriver().
				findElement(By.id(dualListFieldId + JEP_DUAL_LIST_FIELD_MOVEALLLEFT_BTN_POSTFIX));
		getWait().until(elementToBeClickable(moveAllLeftButton));
		moveAllLeftButton.click();
		
		// Определяем кнопку "выбрать опцию" (переместить вправо)
		final WebElement moveRightButton = pages.getApplicationPage().getWebDriver().
				findElement(By.id(dualListFieldId + JEP_DUAL_LIST_FIELD_MOVERIGHT_BTN_POSTFIX));
		
		WebElement option;
		// Последовательно выбираем все опции из необходимых и перемещаем в правую часть
		for (String menuItem: menuItems) {
			if (!JepRiaUtil.isEmpty(menuItem)) {
				try {
					option = pages.getApplicationPage().getWebDriver().findElement(By.xpath(
							String.format("//*[@id='%s']//*[starts-with(@id, '%s') and @%s='%s']",
									dualListFieldId + JEP_DUAL_LIST_FIELD_LEFTPART_POSTFIX,
									dualListFieldId + JEP_DUAL_LIST_FIELD_MENU_ITEM_INFIX,
									JEP_OPTION_VALUE_HTML_ATTR,
									menuItem)));
					getWait().until(elementToBeClickable(option));
					option.click();
					
					getWait().until(elementToBeClickable(moveRightButton));
					moveRightButton.click();
				} catch (NoSuchElementException e) {
					throw new WrongOptionException("Wrong dualList option: [" + menuItem + "]");
				}
			}
		}
	}
	
	@Override
	public SaveResultEnum save() {
		SaveResultEnum result = null;
		pages.getApplicationPage().ensurePageLoaded();
		
		getWait().until(elementToBeClickable(By.id(TOOLBAR_SAVE_BUTTON_ID)));
		
		pages.getApplicationPage().saveButton.click();

		WebDriver wd = pages.getApplicationPage().getWebDriver();
		
		ConditionChecker conditionChecker = new WebDriverWait(wd, 5000).until(
				ExpectedConditions.atLeastOneOfConditionIsSatisfied(
						new DisplayChecker(wd, ALERT_MESSAGEBOX_ID),
						new DisplayChecker(wd, ERROR_MESSAGEBOX_ID),
						new TextChangeChecker(getStatusBar())
		        )
		);		

		if(conditionChecker instanceof TextChangeChecker) {
			result = SaveResultEnum.STATUS_HAS_CHANGED;
			this.setCurrentWorkstate(VIEW_DETAILS);
		} else if(ALERT_MESSAGEBOX_ID.equals(((DisplayChecker)conditionChecker).getId())) {
			result = SaveResultEnum.ALERT_MESSAGE_BOX;
		} else if(ERROR_MESSAGEBOX_ID.equals(((DisplayChecker)conditionChecker).getId())) {
			result = SaveResultEnum.ERROR_MESSAGE_BOX;
		} else {
			throw new NotExpectedException("Save error");
		}
		
		return result;
	}
	
	public boolean checkMessageBox(String messageBoxId) {
		return isDisplayed(messageBoxId);
	}

	public WorkstateEnum getCurrentWorkstate() {
		if(currentWorkstate == null) {
			currentWorkstate = SEARCH;
		}
		return currentWorkstate;
	}

	public void setCurrentWorkstate(WorkstateEnum currentWorkstate) {
		this.currentWorkstate = currentWorkstate;
	}

	/**
	 * Получение кнопки toolbar для перехода в заданное состояние
	 * @param workstate
	 * @return id кнопки Toolbar
	 */
	protected String getToolbarButtonId(WorkstateEnum workstate) {
		String toolbarButtonId = null;
		if(Util.isWorkstateTransitionAcceptable(this.getCurrentWorkstate(), workstate)) { // Проверка возможности перехода (во избежание "бесконечного ожидания")
			switch(workstate) {
			case CREATE:
				toolbarButtonId = TOOLBAR_ADD_BUTTON_ID;
				break;
			case EDIT:
				toolbarButtonId = TOOLBAR_EDIT_BUTTON_ID;
				break;
			case SEARCH:
				toolbarButtonId = TOOLBAR_SEARCH_BUTTON_ID;
				break;
			case SELECTED:
				// TODO Что здесь делать ?
				toolbarButtonId = TOOLBAR_SEARCH_BUTTON_ID;
				break;
			case VIEW_DETAILS:
				toolbarButtonId = TOOLBAR_VIEW_DETAILS_BUTTON_ID;
				break;
			case VIEW_LIST:
				toolbarButtonId = currentWorkstate == SEARCH ? TOOLBAR_FIND_BUTTON_ID : TOOLBAR_LIST_BUTTON_ID;
				break;
			}
		}
		
		return toolbarButtonId;
	}
	
	
	private boolean isDisplayed(String id) {
		try {
			pages.getApplicationPage().getWebDriver().findElement(By.id(id));
			return true;
		} catch(NoSuchElementException ex) {
			return false;
		}
	}

	@Override
	public Field getField(String fieldId) {
		throw new NotImplementedYetException();
	}

	@Override
	public void edit(Map<String, String> key, RecordSelector recordSelector) {
		throw new NotImplementedYetException();
	}

	@Override
	public void edit(String idFieldName, String id) {
		throw new NotImplementedYetException();
	}

	@Override
	public void setCheckBoxFieldValue(String checkBoxFieldId, boolean checked) {
		pages.getApplicationPage().ensurePageLoaded();
		
		WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(checkBoxFieldId + JEP_FIELD_INPUT_POSTFIX)); 
		
		if (fieldInput.isSelected() != checked) {
			getWait().until(elementToBeClickable(fieldInput));
			fieldInput.click();
		}
	}

	@Override
	public void changeCheckBoxFieldValue(String checkBoxFieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(checkBoxFieldId + JEP_FIELD_INPUT_POSTFIX)); 
		
		getWait().until(elementToBeClickable(fieldInput));
		fieldInput.click();
	}

	@Override
	public boolean getCheckBoxFieldValue(String checkBoxFieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		WebElement fieldInput = pages.getApplicationPage().getWebDriver().findElement(By.id(checkBoxFieldId + JEP_FIELD_INPUT_POSTFIX));
		return fieldInput.isSelected();
	}

	@Override
	public void selectAllListMenuItems(String listFieldId, boolean selectAll) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// Cначала пробуем сделать это кнопкой "выделить все"
		WebElement selectAllCheckBox = pages.getApplicationPage().getWebDriver().
				findElement(By.id(listFieldId + JEP_LIST_FIELD_CHECKALL_POSTFIX));
		
		if ("true".equals(selectAllCheckBox.getAttribute("aria-hidden"))) {
			// Кнопка "выделить все" скрыта, значит, нужно снимать выделение с каждого элемента.
			
			// Получаем список всех чекбоксов внутри INPUT'а заданного поля 
			List<WebElement> allCheckBoxes = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
					String.format("//*[@id='%s']//*[starts-with(@id, '%s')]",
							listFieldId + JEP_FIELD_INPUT_POSTFIX,
							listFieldId + JEP_LIST_FIELD_ITEM_CHECKBOX_INFIX)));
			// Кликаем на необходимые
			for (WebElement option: allCheckBoxes ) {
				if (option.isSelected() && !selectAll || !option.isSelected() && selectAll) {
					((JavascriptExecutor) pages.getApplicationPage().getWebDriver()).executeScript("arguments[0].click();", option);
				}
			}
		} else {
			// Кнопка "выделить все" найдена.
			
			if (selectAllCheckBox.isSelected()) {
				if (!selectAll) {
					getWait().until(elementToBeClickable(selectAllCheckBox));
					selectAllCheckBox.click();
				}
			} else {
				// устанавливаем флажок
				getWait().until(elementToBeClickable(selectAllCheckBox));
				selectAllCheckBox.click();
				
				if (!selectAll) {
					// далее при необходимости его снимаем
					getWait().until(elementToBeClickable(selectAllCheckBox));
					selectAllCheckBox.click();
				}
			}
		}
	}
	
	@Override
	public void selectListMenuItems(String listFieldId, String[] menuItems) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// Снимаем все флажки
		selectAllListMenuItems(listFieldId, false);
		
		
		WebElement option;
		// Последовательно отмечаем все опции из необходимых
		for (String menuItem: menuItems) {
			if (!JepRiaUtil.isEmpty(menuItem)) {
				try {
					// Ищем чекбокс с соответствующим искомому значению option-value.
					option = pages.getApplicationPage().getWebDriver().findElement(By.xpath(
							String.format("//*[@id='%s']//*[starts-with(@id, '%s') and @%s='%s']",
									listFieldId + JEP_FIELD_INPUT_POSTFIX,
									listFieldId + JEP_LIST_FIELD_ITEM_CHECKBOX_INFIX,
									JEP_OPTION_VALUE_HTML_ATTR,
									menuItem)));
					((JavascriptExecutor) pages.getApplicationPage().getWebDriver()).executeScript("arguments[0].click();", option);
				
				} catch (NoSuchElementException e) {
					throw new WrongOptionException("Wrong list option: [" + menuItem + "]");
				}
			}
		}
	}

	@Override
	public String[] getListFieldValues(String jepListFieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// Получаем список всех чекбоксов внутри INPUT'а заданного поля 
	    List<WebElement> allCheckBoxes = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
	    		String.format("//*[@id='%s']//*[starts-with(@id, '%s')]",
	    				jepListFieldId + JEP_FIELD_INPUT_POSTFIX,
	    				jepListFieldId + JEP_LIST_FIELD_ITEM_CHECKBOX_INFIX)));
	    
	    List<String> ret = new ArrayList<String>();
	    
	    // Проходим по полученному списку, и, для отмеченных чекбоксов, получаем их option-value
	    for (WebElement checkbox: allCheckBoxes) {
	    	if (checkbox.isSelected()) {
	    		ret.add(checkbox.getAttribute(JEP_OPTION_VALUE_HTML_ATTR));
	    	}
	    }
	    return ret.toArray(new String[ret.size()]);
	}

	
	@Override
	public boolean isFieldVisible(String fieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// visiblity can be determined by "aria-hidden" attribute of the entire field element
		WebElement element = pages.getApplicationPage().getWebDriver().findElement(By.id(fieldId));
		return !"true".equals(element.getAttribute("aria-hidden"));
	}

	
	@Override
	public boolean isFieldEnabled(String fieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// enability can be determined by "disabled" attribute of the field's INPUT element
		WebElement element = pages.getApplicationPage().getWebDriver().findElement(By.id(fieldId + JEP_FIELD_INPUT_POSTFIX));
		return !"true".equals(element.getAttribute("disabled"));
	}

	@Override
	public boolean isFieldEditable(String fieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// get all div children (but not grandchildren) of the field
		List<WebElement> potentialCards = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
				String.format("//*[@id='%s']/div",
						fieldId)));
		WebElement editableCard = null, viewCard = null;
		
		for (WebElement potentialCard: potentialCards) {
			if (editableCard == null) {
				try { 
					potentialCard.findElement(By.xpath(
							String.format(".//*[@%s='%s']",
									JEP_CARD_TYPE_HTML_ATTR,
									JEP_CARD_TYPE_VALUE_EDTB)));
					// if no excpetion has been thrown, then the 'jep-card-type=editable' was found in a child
					// of the current div, that means it itself is the editable card.
					editableCard = potentialCard;
				} catch (NoSuchElementException e) {/*continue finding*/}
			}
			if (viewCard == null) {
				try { 
					potentialCard.findElement(By.xpath(
							String.format(".//*[@%s='%s']",
									JEP_CARD_TYPE_HTML_ATTR,
									JEP_CARD_TYPE_VALUE_VIEW)));
					// if no excpetion has been thrown, then the 'jep-card-type=view' was found in a child
					// of the current div, that means it itself is the view card.
					viewCard = potentialCard;
				} catch (NoSuchElementException e) {/*continue finding*/}
			}
		}
		
		if (editableCard == null) {
			throw new AutomationException("No editable card found for the field " + fieldId + ". Check the presence by xpath.");
		}
		if (viewCard == null) {
			throw new AutomationException("No view card found for the field " + fieldId + ". Check the presence by xpath.");
		}
		
		return editableCard.getAttribute("aria-hidden") == null &&
				"true".equals(viewCard.getAttribute("aria-hidden"));
	}

	@Override
	public boolean isFieldAllowBlank(String fieldId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		try {
			pages.getApplicationPage().getWebDriver().findElement(By.id(fieldId + JEP_FIELD_ALLOW_BLANK_POSTFIX));
			return false;
		} catch (NoSuchElementException e) {
			return true; 
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public List<String> getGridHeaders(String gridId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		List<String> ret = new ArrayList<String>();
		
		List<WebElement> headers = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
				String.format("//thead[@id='%s']//th",
						gridId + GRID_HEADER_POSTFIX)));
		
		for (int i = 0; i < headers.size() - 1; i++) {// One extra column of a zero width to stretch the rightmost column
			ret.add(headers.get(i).getAttribute("innerHTML"));
		}
		return ret;
	}

	@Override
	public List<List<Object>> getGridDataRowwise(String gridId) {
		pages.getApplicationPage().ensurePageLoaded();
		
		// TODO get rid of this sleeping, but somehow wait until the grid is loaded!
		sleep(500);
		
		List<List<Object>> ret = new ArrayList<List<Object>>();
		
		List<WebElement> rows = pages.getApplicationPage().getWebDriver().findElements(By.xpath(
				String.format("//tbody[@id='%s']/tr",
						gridId + GRID_BODY_POSTFIX)));
		
		for (WebElement row: rows) {
			List<Object> rowObjects = new ArrayList<Object>();
			
			for (WebElement cellDiv: row.findElements(By.xpath("./td/div"))) {
				
				try {
					WebElement cellElement = cellDiv.findElement(By.xpath("./*"));
					rowObjects.add(cellElement);
				} catch (NoSuchElementException e) {
					rowObjects.add(cellDiv.getAttribute("innerHTML"));
				}
			}
			
			ret.add(rowObjects);
		}
		
		return ret;
	}

}