package com.technology.jep.jepria.client.ui.wizard;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.technology.jep.jepria.client.history.place.BlockPlaceController;
import com.technology.jep.jepria.client.ui.ClientFactory;
import com.technology.jep.jepria.client.ui.ClientFactoryImpl;
import com.technology.jep.jepria.client.ui.eventbus.JepEventBus;
import com.technology.jep.jepria.shared.service.data.JepDataService;
import com.technology.jep.jepria.shared.service.data.JepDataServiceAsync;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BlockClientFactoryImpl<S extends JepDataServiceAsync> extends ClientFactoryImpl<JepEventBus> implements BlockClientFactory<S> {

  protected S service;
  protected BlockPlaceController blockPlaceController;
  
  public S getService() {
    if (service == null){
      service = (S) GWT.create(JepDataService.class);
    }
    return service;
  }
  
  @Override
  protected JepEventBus createEventBus() {
    return new JepEventBus();
  }
  
  @Override
  protected void initActivityMappers(ClientFactory<JepEventBus> clientFactory) {
    
    super.initActivityMappers(clientFactory);
    
    /*
     * Создадим ActivityMapper и ActivityManager для формы визарда.
     */
    ActivityManager activityManager = new ActivityManager(
      new BlockFormActivityMapper((BlockClientFactory<S>)clientFactory)
      , clientFactory.getEventBus()
    );

    // Необходимо для предотвращения де-регистрации в EventBus и сбором garbage collection (смотри описание метода в JavaDoc GWT).
    activityManager.setDisplay(new AcceptsOneWidget() {
      public void setWidget(IsWidget widget) {}
    });
  }
  
  public BlockPlaceController getPlaceController() {
    if(blockPlaceController == null) {
      blockPlaceController = new BlockPlaceController(getEventBus(), this);
    }
    return blockPlaceController;
  }
}
