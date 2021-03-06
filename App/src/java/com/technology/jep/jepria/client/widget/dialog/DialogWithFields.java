package com.technology.jep.jepria.client.widget.dialog;

import static com.technology.jep.jepria.client.JepRiaClientConstant.JepTexts;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.technology.jep.jepria.client.message.MessageBox;
import com.technology.jep.jepria.client.message.PredefinedButton;
import com.technology.jep.jepria.client.widget.field.multistate.JepMultiStateField;

/**
 * Модальное окно с поддержкой добавления полей. 
 * На кнопку "Ок" можно добавить листенер, который сработает, если валидация полей успешна. <br/>
 * Пример:
 * <pre>
 *   final DialogWithFields setLostDialog = new DialogWithFields(contractDetailText.dialog_lost_header());
 *
 *   JepComboBoxField eventTypeField = new JepComboBoxField(contractDetailText.dialog_lost_field_lostEventType());
 *   eventTypeField.setAllowBlank(false);
 *   eventTypeField.setOptions(lostEventTypeList);
 *   setLostDialog.addField(LOST_EVENT_TYPE_CODE, eventTypeField);
 *   
 *   JepDateField dateField = new JepDateField(contractDetailText.dialog_lost_field_lostDate());
 *   dateField.setAllowBlank(false);
 *   setLostDialog.addField(LOST_EVENT_DATE, dateField);
 *
 *   setLostDialog.setOnSave(new Command() {
 *     {@literal @}Override
 *     public void execute() {
 *       list.mask(JepTexts.loadingPanel_dataLoading());
 *       service.setEventContractLost(
 *          currentRecord.get(CONTRACT_NUMBER),
 *           JepOption.&lt;String&gt;getValue(setLostDialog.getField(LOST_EVENT_TYPE_CODE).getValue()),
 *           setLostDialog.getField(LOST_EVENT_DATE).getValue(),
 *           voidRefreshCallback);
 *     }
 *   });
 *   
 *   setLostDialog.show(); 
 * </pre>
 * TODO: Подумать еще над названием класса.
 * <br/> TODO: имеет смысл использовать FieldManager вместо fields?
 * <br/> TODO: продумать гибкую кастомизацию кнопок.
 */
public class DialogWithFields extends MessageBox {
  
   /**
   * Поля модального окна. 
   */
  private Map<String, JepMultiStateField<?, ?>> fields = new HashMap<String, JepMultiStateField<?,?>>();
  
  /**
   * Панель модального окна.
   */
  private VerticalPanel panel = new VerticalPanel();
  
  /**
   * Добавляет виджет на панель модального окна.
   */
  public void add(Widget w) {
    panel.add(w);
  }
  
  /**
   * Добавить поле на панель.
   * @param fieldId Идентификатор поля.
   * @param field Поле.
   */
  public void addField(String fieldId, JepMultiStateField<?, ?> field) {
    fields.put(fieldId, field);
    add(field);
  }
  
  /**
   * Получить поле.
   * @param fieldId Идентификатор поля.
   * @return Поле.
   */
  public JepMultiStateField<?, ?> getField(String fieldId) {
    return fields.get(fieldId);
  }

  
  /**
   * Конструктор
   * @param headerText Заголовок.
   */
  public DialogWithFields(String headerText) {
    super(headerText, null);
    
    //основная панель
    mainPanel.setWidget(0, 0, panel);
    mainPanel.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    
    //кнопка сохранить
    Button saveButton = new Button(JepTexts.button_save_alt());
    saveButton.addClickHandler(event -> onSave());
    addButton(PredefinedButton.OK, saveButton);
    
    //кнопка выйти
    Button cancelButton = new Button(JepTexts.button_exit_alt());
    cancelButton.addClickHandler(event -> onCancel());
    addButton(PredefinedButton.CANCEL, cancelButton);
  }

  /**
   * Обработчик на кнопку PredefinedButton.CANCEL
   */
  protected void onCancel() {
    hide();
  }

  /**
   * Обработчик на кнопку PredefinedButton.OK
   */
  protected void onSave() {
    if (isValid()) {
      if (onSave != null) onSave.execute();
      hide();
    }
  }

  /**
   * Проверка валидации полей диалогового окна.
   * 
   * @return флаг успешности валидации
   */
  protected boolean isValid() {
    return fields.entrySet().parallelStream().allMatch(map -> map.getValue().isValid() == true);
  }
  
  /**
   * Обработчик сохранения после успешной валидации.
   */
  protected Command onSave;
  
  public void setOnSave(Command onSave) {
    this.onSave = onSave;
  }
}
