package com.technology.jep.jepria.client.ui.form.detail;

import static com.technology.jep.jepria.client.JepRiaClientConstant.JepTexts;
import static com.technology.jep.jepria.client.JepRiaClientConstant.UPLOAD_SUCCESS_SUBSTRING;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.CREATE;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.EDIT;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.SEARCH;
import static com.technology.jep.jepria.client.ui.WorkstateEnum.VIEW_DETAILS;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.technology.jep.jepria.client.JepScheduledCommand;
import com.technology.jep.jepria.client.async.JepAsyncCallback;
import com.technology.jep.jepria.client.history.place.JepSearchPlace;
import com.technology.jep.jepria.client.history.place.JepViewDetailPlace;
import com.technology.jep.jepria.client.history.place.JepViewListPlace;
import com.technology.jep.jepria.client.history.place.PlainPlaceController;
import com.technology.jep.jepria.client.history.scope.JepScope;
import com.technology.jep.jepria.client.history.scope.JepScopeStack;
import com.technology.jep.jepria.client.message.ConfirmCallback;
import com.technology.jep.jepria.client.ui.JepPresenter;
import com.technology.jep.jepria.client.ui.WorkstateEnum;
import com.technology.jep.jepria.client.ui.eventbus.event.UpdateScopeEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.PlainEventBus;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.DeleteEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.DoDeleteEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.DoGetRecordEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.DoSearchEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.ListEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.SaveEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.SearchEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.SetCurrentRecordEvent;
import com.technology.jep.jepria.client.ui.eventbus.plain.event.SetListUIDEvent;
import com.technology.jep.jepria.client.ui.plain.StandardClientFactory;
import com.technology.jep.jepria.client.util.JepClientUtil;
import com.technology.jep.jepria.client.widget.field.FieldManager;
import com.technology.jep.jepria.client.widget.field.multistate.JepMultiStateField;
import com.technology.jep.jepria.client.widget.field.multistate.large.JepLargeField;
import com.technology.jep.jepria.shared.exceptions.NotSingleRecordException;
import com.technology.jep.jepria.shared.history.JepHistoryToken;
import com.technology.jep.jepria.shared.load.FindConfig;
import com.technology.jep.jepria.shared.load.PagingConfig;
import com.technology.jep.jepria.shared.load.PagingResult;
import com.technology.jep.jepria.shared.record.JepRecord;
import com.technology.jep.jepria.shared.service.data.JepDataServiceAsync;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

public class DetailFormPresenter<V extends DetailFormView, E extends PlainEventBus, S extends JepDataServiceAsync, 
    F extends StandardClientFactory<E, S>>
  extends JepPresenter<E, F>
    implements 
      DeleteEvent.Handler,
      DoDeleteEvent.Handler,
      DoGetRecordEvent.Handler,
      DoSearchEvent.Handler,
      ListEvent.Handler,
      SearchEvent.Handler,
      SetCurrentRecordEvent.Handler,
      SetListUIDEvent.Handler,
      SaveEvent.Handler 
      {
  
  protected V view;
  protected S service;
  
  protected PlainPlaceController<?, ?, ?> placeController;
  
  /**
   * Управляющий полями класс по умолчанию. В общем случае, управляющих классов может быть произвольное количество.
   * При необходимости, дополнительные управляющие классы используются уже на прикладном уровне.
   */
  protected FieldManager fields;

  /**
   * Поле для хранения параметров поиска.<br/>
   * Используется для восстановления значения поисковых полей при возвращении в режим поиска.
   */
  protected JepRecord searchTemplate = null;
  
  /**
   * Массив идентификаторов модулей на текущем уровне иерархии модулей.<br/>
   * Первым элементом массива является идентификатор родительского, по отношению к остальным, модуля.<br/>
   * Определяется только для родительского модуля.
   */
  protected String[] scopes;

  /**
   * Текущая запись.
   */
  protected JepRecord currentRecord = null;

  /**
   * Идентификатор списка.
   */
  protected Integer listUID = null;

  /**
   * Создает презентер детальной формы в заданном режиме (Place).
   *
   * @param place режим, в котором необходимо создать презентер
   * @param clientFactory клиентская фабрика модуля
   */
  @SuppressWarnings("unchecked")
  public DetailFormPresenter(Place place, F clientFactory) {
    super(place, clientFactory);
    
    view = (V) clientFactory.getDetailFormView();
    service = (S)clientFactory.getService();

    placeController = clientFactory.getPlaceController();

    fields = view.getFieldManager();
  }
  
  /**
   * Создает презентер родительской детальной формы (для которой существуют дочерние) в заданном режиме (Place).<br/>
   * Обычно используется в наследниках вызовом вида <code>super(&lt;Form Name&gt;ClientConstant.scopeModuleIds, place, clientFactory);</code>.
   *
   * @param scopeModuleIds идентификаторы родительского и дочерних модулей (первым элементом массива является идентификатор родительского, 
   * по отношению к остальным, модуля)
   * @param place режим, в котором необходимо создать презентер
   * @param clientFactory клиентская фабрика модуля
   */
  public DetailFormPresenter(String[] scopeModuleIds, Place place, F clientFactory) {
    this(place, clientFactory);
    this.scopes = scopeModuleIds;
  }
  
  public void start(AcceptsOneWidget container, EventBus eventBus) {
    // Подписка activity-презентера на события EventBus.
    eventBus.addHandler(DeleteEvent.TYPE, this);
    eventBus.addHandler(DoDeleteEvent.TYPE, this);
    eventBus.addHandler(DoGetRecordEvent.TYPE, this);
    eventBus.addHandler(DoSearchEvent.TYPE, this);
    eventBus.addHandler(ListEvent.TYPE, this);
    eventBus.addHandler(SearchEvent.TYPE, this);
    eventBus.addHandler(SetCurrentRecordEvent.TYPE, this);
    eventBus.addHandler(SetListUIDEvent.TYPE, this);
    eventBus.addHandler(SaveEvent.TYPE, this);
    
    // "Привязка" элементов представления к функционалу презентера.
    bind();
    // Переведем презентер модуля в заданный режим.
    changeWorkstate(place);
  }
  
  /**
   * Метод используется для перекрытия потомками с целью "привязки" элементов представления к функционалу презентера.
   */
  protected void bind() {
    view.setPresenter(this);
  }
  
  protected void onChangeWorkstate(WorkstateEnum workstate) {
    fields.changeWorkstate(workstate);

    if(VIEW_DETAILS.equals(workstate)) {
      fields.setValues(currentRecord);
      updateScope(workstate);
    } else if(EDIT.equals(workstate)) {
      fields.setValues(currentRecord);
      updateScope(workstate);
    } else if(CREATE.equals(workstate)) {
      fields.clear();
      resetScope();
    } else if(SEARCH.equals(workstate)) {
      if(searchTemplate != null) {
        // Если есть сохраненные поисковые параметры, то восстановим их.
        fields.setValues(searchTemplate);
      } else {
        // Очистим поля, если сохраненные поисковые параметры отсутствуют.
        fields.clear();
      }

      resetScope();
    }
  }
  
  /**
   * Обработчик события отображения списка.<br/>
   * В данном методе происходит переход на списочную форму, если пользователь уже открывал ее, 
   * в противном случае - происходит переход на детальную форму в режиме поиска.
   *
   * @param event событие отображения списка
   */
  @Override
  public void onList(ListEvent event) {
    // Скроем закладки дочерних форм.
    resetScope();
    // Если есть сохраненные поисковые параметры, значит пользователь уже открывал списочную форму, в этом случае перейдем
    // на списочную форму, иначе - перейдем на детальную форму в режиме поиска.
    // Вызов перехода на новый Place происходит ОБЯЗАТЕЛЬНО ПОСЛЕ подготовки данных для записи в History (изменения Scope).
    placeController.goTo(searchTemplate != null ? new JepViewListPlace() : new JepSearchPlace());
  }
  
  /**
   * Обновление данных о состоянии модуля (в объекте <code>JepScopeStack</code>, отражающем состояние приложения).
   * 
   * @param workstate новое рабочее состояние
   */
  protected void updateScope(WorkstateEnum workstate) {
    JepScope scope = JepScopeStack.instance.peek();
    // Только в случае, если действительно изменяется текущая область видимости модулей
    // Для поэлементного сравнение массивов, а не по ссылкам в памяти, следует использовать метод equals утилитарного класса Arrays
    if(scopes != null && !Arrays.equals(scope.getModuleIds(), scopes)) { // и указаны родительский и дочерние модули ...
      if(scope.isMainActive()) { // Если текущий модуль является родительским ...
        scope.setModuleIds(scopes);
        scope.getModuleStates()[0] = workstate;  // Обновление рабочего состояния родительского модуля.
        
      // Если текущий модуль на данном уровне иерархии является дочерним, но сам содержит дочерние модули, то создадим новый уровень иерархии
      // модулей и добавим его в стек (в объект JepScopeStack, отражающий состояние приложения).
      } else {
        JepScope newScope = new JepScope(scopes);
        newScope.getModuleStates()[0] = workstate;
        JepScopeStack.instance.push(newScope);
        eventBus.adjustExitScope(); // Выполним действия по настройке перехода на вышележащий уровень иерархии (на родительский уровень).

        // Обработчик установки текущей записи устанавливает в JepScopeStack заначение первичного ключа текущего модуля.
        eventBus.setCurrentRecord(currentRecord);
      }
      // Уведомим приложение об изменении состояния модуля (например для скрытия/отображения вкладок перехода на родительские/дочерние модули).
      eventBus.updateScope(new UpdateScopeEvent(JepScopeStack.instance.peek()));
    }
  }

  /**
   * Установка данных о состоянии модуля в исходное положение (скрытие вкладок перехода на дочерние модули).
   */
  protected void resetScope() {
    JepScope scope = JepScopeStack.instance.peek();
    scope.collapseIfMain();
    eventBus.updateScope(new UpdateScopeEvent(scope));
  }

  @Override
  public void onSetCurrentRecord(SetCurrentRecordEvent event) {
    currentRecord = event.getCurrentRecord();
  }

  /**
   * Обработчик события поиска "doSearch".
   */
  @Override
  public void onDoSearch(DoSearchEvent event) {
    if(fields.isValid()) {
      // Получим значения с формы.
      JepRecord formProperties = fields.getValues();
      // Добавим foreignKey.
      JepClientUtil.addForeignKey(formProperties);
      PagingConfig pagingConfig = new PagingConfig(formProperties);
      eventBus.search(pagingConfig);
      // Вызов перехода на новый Place происходит ОБЯЗАТЕЛЬНО ПОСЛЕ подготовки данных для записи в History 
      // (изменения Scope в обработчиках шины событий).
      placeController.goTo(new JepViewListPlace());
    } else {
      messageBox.showError(JepTexts.errors_dialog_form_incorrectInputData());
    }
  }
  
  /**
   * Обработчик события поиска "search".<br/>
   * 
   * Особенности:<br/>
   * Сохранение поискового шаблона осуществляется здесь, поскольку переход возможен не только с формы поиска (типовой случай), 
   * но и во время перехода с главной формы на подчиненную.
   */
  @Override
  public void onSearch(SearchEvent event) {
    // Проинициализируем поисковый шаблон (независимо откуда был осуществлен переход: с формы поиска или с главной формы на подчиненную).
    searchTemplate = event.getPagingConfig().getTemplateRecord();
  }
  
  /**
   * Обработчик события сохранения 'save'
   * В зависимости от состояния выполняет сохранение существующего или создание нового объекта.
   * 
   * Метод используется в случае использования стандартного сервиса работы с данными JepStandardService.
   * При реализации в прикладном модуле собственного сервиса необходимо в презентере-наследнике прикладного
   * модуля реализовать перекрывающий метод onSave().
   */
  @Override
  public void onSave(SaveEvent event) {
    if(fields.isValid()) {
      JepRecord formProperties = fields.getValues();
      
      logger.debug(this.getClass() + ".onSave(): formProperties = " + formProperties);
      
      // Для подчинённых объектов добавляется foreignKey.
      if(JepScopeStack.instance.size() > 1 || !JepScopeStack.instance.peek().isMainActive()) {
        JepClientUtil.addForeignKey(formProperties);
      }
      
      // Для режима редактирования текущую запись
      // необходимо дополнить или переписать значениями с формы.
      if(EDIT.equals(_workstate)) {
        JepRecord updatedRecord = new JepRecord(currentRecord);
        updatedRecord.update(formProperties);
        formProperties = updatedRecord;
      }
      
      // Если не выполнены первоначальные условия проверки, то выходим из сохранения.
      if (!beforeSave(formProperties)) return;
      
      eventBus.setSaveButtonEnabled(false);
      
      if(CREATE.equals(_workstate)) {
        saveOnCreate(formProperties);
      } else if(EDIT.equals(_workstate)) {
        saveOnEdit(formProperties);
      }
      
    } else {
      messageBox.showError(clientFactory.getTexts().errors_dialog_form_incorrectInputData());
    }
  }
  
  protected void saveOnCreate(JepRecord currentRecord) {
    FindConfig createConfig = new FindConfig(currentRecord);
    createConfig.setListUID(listUID);
    clientFactory.getService().create(createConfig, new JepAsyncCallback<JepRecord>() {
      public void onFailure(final Throwable th) {
        onCreateFailure(th);
      }
      public void onSuccess(final JepRecord resultRecord) {
        onCreateSuccess(resultRecord);
      }
    });
  }

  protected void saveOnEdit(JepRecord currentRecord) {
    FindConfig updateConfig = new FindConfig(currentRecord);
    updateConfig.setListUID(listUID);
    clientFactory.getService().update(updateConfig, new JepAsyncCallback<JepRecord>() {
      public void onFailure(final Throwable th) {
        onUpdateFailure(th);
      }
      public void onSuccess(final JepRecord resultRecord) {
        onUpdateSuccess(resultRecord);
      }
    });
  }
  
  /**
   * Счетчик количества сабмитов
   */
  protected int submitCounter;
  
  /**
   * Метод, вызываемый после успешного сохранения информации
   * 
   * @param resultRecord    ссылка на созданную запись
   */
  public void onSaveSuccess(JepRecord resultRecord) {
    submitCounter = 0;
    
    for (JepMultiStateField<?, ?> field: fields.values()) {  
      JepLargeField<?> largeField;
      if((field instanceof JepLargeField) && (largeField = (JepLargeField<?>)field).isFileSelected()) {
        prepareLOBField(largeField, resultRecord);
        largeField.getFormPanel().submit();        
      }      
    }
    if (submitCounter == 0) {
      afterSave(resultRecord);
    }
  }
  
  /**
   * Подготовка LOB-полей перед сабмитом формы
   * 
   * @param field          LOB-поле
   * @param resultRecord      текущий рекорд, по которому готовим поле
   */
  @SuppressWarnings("unchecked")
  protected void prepareLOBField(final JepLargeField field, final JepRecord resultRecord){
    
    field.getHiddenPrimaryKeyField().setValue(JepHistoryToken.getMapAsToken(clientFactory.getRecordDefinition().buildPrimaryKeyMap(resultRecord)));
    
    field.setBeforeSubmitCommand(new ScheduledCommand() {
      @Override
      public void execute() {
        if (submitCounter++ == 0){
          JepClientUtil.showLoadingPanel(null, clientFactory.getTexts().loadingPanel_fileLoading());
        }
      }
    });
    
    field.setAfterSubmitCommand(new JepScheduledCommand<String>(){
      public void execute() {
        submitCounter--;
        String resultHtml = getData();
        if (submitSuccessful(resultHtml)) {
          onSubmitSuccess(resultRecord);
        }
        else {
          onSubmitFailure(resultRecord, resultHtml);
        }
      }
    });
  }

  /**
   * Проверка, что сабмит был успешен.<br/>
   * По умолчанию возвращает true, если получен пустой ответ или ответ содержит подстроку 
   * &quot;success&quot;. В противном случае возвращает false. Может быть переопределён 
   * в классе-наследнике.
   * @param resultHtml полученный от сервера результат сабмита в виде строки
   * @return true в случае успешного сабмита, false в противном случае
   */
  protected boolean submitSuccessful(String resultHtml) {
    return JepRiaUtil.isEmpty(resultHtml) || resultHtml.contains(UPLOAD_SUCCESS_SUBSTRING);
  }

  /**
   * Действие в случае успешного сабмита.<br/>
   * По умолчанию проверяет, остались ли ещё незавершённые сабмиты, и если нет, 
   * то скрывает индикатор загрузки и вызывает afterSave(). 
   * Может быть переопределён в классе-наследнике.
   * @param resultRecord запись
   */
  protected void onSubmitSuccess(JepRecord resultRecord) {
    if (submitCounter == 0) {
      JepClientUtil.hideLoadingPanel();
      afterSave(resultRecord);  
    }
  }
  
  /**
   * Действие в случае ошибки при сабмите.<br/>
   * По умолчанию выдаёт сообщение об ошибке, и далее проверяет, остались ли 
   * ещё незавершённые сабмиты. Если нет, то скрывает индикатор загрузки и вызывает 
   * afterSave(). Может быть переопределён в классе-наследнике.
   * @param resultRecord запись
   * @param resultHtml полученный от сервера результат сабмита в виде строки
   */
  protected void onSubmitFailure(JepRecord resultRecord, String resultHtml) {
    messageBox.showError(
        clientFactory.getTexts().errors_file_uploadError() + "\n" + resultHtml);
    if (submitCounter == 0) {
      JepClientUtil.hideLoadingPanel();
      afterSave(resultRecord);  
    }
  }
  
  /**
   * Проверка, что перед сохранением текущая запись готова к сохранению. По умолчанию, возврашает true.<br/>
   * Здесь же при необходимости можно выполнить какие-либо действия перед сохранением (например, скорректировать запись).<br/>
   * Может быть переопределён в классе-наследнике.
   * 
   * @param currentRecord текущая запись
   * @return true в случае успешной проверки, false в противном случае
   */
  protected boolean beforeSave(JepRecord currentRecord) {
    return true;
  }

  /**
   * При необходимости выполнить какие-либо дополнительные действия после сохранения
   * метод перекрывается наследниками.
   * 
   * @param resultRecord результирующая запись
   */
  protected void afterSave(JepRecord resultRecord) {
    // Поскольку БД-модуль не обязан возвращать родительский ключ.
    if(JepScopeStack.instance.size() > 1 || !JepScopeStack.instance.peek().isMainActive()) {
      JepClientUtil.addForeignKey(resultRecord);
    }
    // Текущую запись обновляем ТОЛЬКО после успешного сохранения.
    eventBus.setCurrentRecord(resultRecord);
    // Вызов перехода на новый Place происходит ОБЯЗАТЕЛЬНО ПОСЛЕ подготовки данных для записи в History 
    // (изменения Scope в обработчиках шины событий).
    placeController.goTo(new JepViewDetailPlace());
  }

  /**
   * Обработчик события получения записи по первичному ключу.<br/>
   * Используется для восстановления состояния детальной формы из History.<br/>
   * 
   * Особенности:<br/>
   * После получения записи происходит иннициализация полей формы полученными значениями вызовом метода {@link #adjustToRecord(JepRecord record)}.
   *
   * @param event событие получения записи
   */
  @Override
  public void onDoGetRecord(DoGetRecordEvent event) {
  
    final PagingConfig pagingConfig = event.getPagingConfig();
    
    clientFactory.getService().find(pagingConfig, new JepAsyncCallback<PagingResult<JepRecord>>() {
      public void onSuccess(PagingResult<JepRecord> pagingResult) {
        List<JepRecord> result = pagingResult.getData();
        if(result.size() == 1) {
          adjustToRecord(result.get(0));
        } else {
          String message = this.getClass() 
            + ": onDoGetRecord: pagingConfig.getTemplateRecord() = " + pagingConfig.getTemplateRecord() + " , find result size != 1";
          logger.error(message);
          throw new NotSingleRecordException(message);
        }
      }
    });

  }
  
  /**
   * Иннициализация полей формы значением записи.<br/>
   * Используется для восстановления состояния детальной формы из History.<br/>
   * 
   * Особенности:<br/>
   * Вызывается в обработчике получения записи по первичному ключу {@link #onDoGetRecord(DoGetRecordEvent event)}.
   *
   * @param record запись для иннициализации полей формы
   */
  protected void adjustToRecord(JepRecord record) {
    eventBus.setCurrentRecord(record);
    onChangeWorkstate(_workstate);
    adjustToWorkstate(_workstate);
  }
  
  /** 
   * Обработчик события Удалить.<br/>
   * Метод вызывает диалог подтверждения удаления и, в обработчике ConfirmCallback, взывает обработчик непосредственно удаления 
   * {@link #onDeleteConfirmation(Boolean, JepRecord) onDeleteConfirmation}.
   * 
   * @param event событие Удалить
   */
  @Override
  public void onDoDelete(DoDeleteEvent event) {
    // Проверим состояние, чтобы обеспечить срабатывание данного обработчика только при активной детальной форме.
    if(VIEW_DETAILS.equals(_workstate) || EDIT.equals(_workstate)) {
      messageBox.confirmDeletion(false, new ConfirmCallback() {
        public void onConfirm(Boolean yes) {
          onDeleteConfirmation(yes, currentRecord);
        }
      });
    }
  }  

  /**
   * Обработчик удаления, вызывающий непосредственно сервис удаления.
   *
   * @param yes вызывать ли сервис удаления: true - вызывать, иначе - не вызывать
   * @param record запись, которую необходимо удалить
   */
  protected void onDeleteConfirmation(Boolean yes, final JepRecord record) {
    if(yes) {
      JepClientUtil.showLoadingPanel(null, JepTexts.loadingPanel_deletingRecords());
      FindConfig deleteConfig = new FindConfig(record);
      deleteConfig.setListUID(listUID);
      clientFactory.getService().delete(deleteConfig, new JepAsyncCallback<Void>() {
        public void onFailure(final Throwable th) {
          onDeleteFailure(th);
          JepClientUtil.hideLoadingPanel();
        }
        public void onSuccess(final Void result) {
          onDeleteSuccess(record);
          JepClientUtil.hideLoadingPanel();
        }
      });
    }
  }
  
  /**
   * Обработчик события удаления.
   *
   * @param event событие удаления
   */
  @Override
  public void onDelete(DeleteEvent event) {
    eventBus.list();
  }
  
  /**
   * Обработчик успешного завершения сервиса создания записи.
   *
   * @param resultRecord созданная запись
   */
  public void onCreateSuccess(JepRecord resultRecord) {
    onSaveSuccess(resultRecord);
  }

  /**
   * Обработчик НЕуспешного завершения сервиса создания записи.
   *
   * @param th возникшее исключение
   */
  public void onCreateFailure(Throwable th) {
    eventBus.setSaveButtonEnabled(true);
    clientFactory.getExceptionManager().handleException(th, JepTexts.form_detail_createError());
  }

  /**
   * Обработчик успешного завершения сервиса обновления записи.
   *
   * @param resultRecord обновленная запись
   */
  public void onUpdateSuccess(JepRecord resultRecord) {
    onSaveSuccess(resultRecord);
  }

  /**
   * Обработчик НЕуспешного завершения сервиса обновления записи.
   *
   * @param th возникшее исключение
   */
  public void onUpdateFailure(Throwable th) {
    eventBus.setSaveButtonEnabled(true);
    clientFactory.getExceptionManager().handleException(th, JepTexts.form_detail_updateError());
  }
  
  /**
   * Обработчик успешного завершения сервиса удаления записи.
   *
   * @param record удаленная запись
   */
  public void onDeleteSuccess(JepRecord record) {
    eventBus.delete(record);
  }

  /**
   * Обработчик НЕуспешного завершения сервиса удаления записи.
   *
   * @param th возникшее исключение
   */
  public void onDeleteFailure(Throwable th) {
    clientFactory.getExceptionManager().handleException(th, JepTexts.form_deleteError());
  }
  
  @Override
  public void onSetListUID(SetListUIDEvent event) {
    listUID = event.getListUID();
  }
  
  /**
   * {@inheritDoc}<br>
   * Обрабатываем лишь специфичные для детальной формы состояния. 
   */
  @Override
  protected boolean isAcceptableWorkstate(WorkstateEnum workstate) {
    return SEARCH.equals(workstate) || CREATE.equals(workstate) || VIEW_DETAILS.equals(workstate) || EDIT.equals(workstate);
  }
}
