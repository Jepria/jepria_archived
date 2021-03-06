package com.technology.jep.jepria.client.ui.toolbar;

import static com.technology.jep.jepria.client.JepRiaClientConstant.TOOLBAR_DEFAULT_STYLE;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.technology.jep.jepria.client.ui.toolbar.ToolBarViewImpl;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

/**
 * Реализация инструментальной панели с заголовкой. 
 * Простая надстройка над стандартной инструментальной панелью. <br/>
 * TODO: подумать над объединением ToolBarWithHeaderViewImpl и ToolBarViewImpl
 */
public class ToolBarWithHeaderViewImpl extends ToolBarViewImpl
    implements ToolBarWithHeaderView {

  private static final String TOOLBAR_WITH_HEADER_HEADER_CLASS = "jepRia-toolBarWithHeader__header";
  
  /**
   * Виджет заголовка.
   */
  private SimplePanel header;
  
  /**
   * Виджет инструментальной панели с заголовком.
   */
  private VerticalPanel toolBarWithHeader;
  
  /**
   * Конструктор.
   */
  public ToolBarWithHeaderViewImpl() {
    super();
    
    header = new SimplePanel();
    header.setWidget(new HTML());
    header.setStyleName(TOOLBAR_DEFAULT_STYLE);
    header.addStyleName(TOOLBAR_WITH_HEADER_HEADER_CLASS);
    
    toolBarWithHeader = new VerticalPanel();
    toolBarWithHeader.setWidth("100%");
    toolBarWithHeader.add(toolBar);
    toolBarWithHeader.add(header);
  }
  
  @Override
  public VerticalPanel asWidget() {
    return toolBarWithHeader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setHeaderHTML(String text) {
    ((HTML) header.getWidget()).setHTML(text);
    header.setVisible(!JepRiaUtil.isEmpty(text));
  }
}
