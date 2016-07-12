package com.technology.jep.jepria.client.widget.list;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.technology.jep.jepria.client.widget.list.cell.JepCheckBoxCell;
import com.technology.jep.jepria.shared.record.JepRecord;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

@SuppressWarnings("unchecked")
public class JepColumn<T, C> extends Column<T, C> {

  /**
   * Наименование селектора (класса стилей), общего для всех ячеек данных грида.
   */
  protected static String COMMON_CELL_STYLE = "jepRia-Grid-Cell";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с выравниванием текста по левому краю.
   */
  protected static String ALIGN_LEFT_CELL_STYLE = "jepRia-Grid-Cell-AlignLeft";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с выравниванием текста по правому краю.
   */
  protected static String ALIGN_RIGHT_CELL_STYLE = "jepRia-Grid-Cell-AlignRight";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с выравниванием текста по центру.
   */
  protected static String ALIGN_CENTER_CELL_STYLE = "jepRia-Grid-Cell-AlignCenter";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с вертикальным выравниванием текста по верхнему краю.
   */
  protected static String VERTICAL_ALIGN_TOP_CELL_STYLE = "jepRia-Grid-Cell-VAlignTop";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с вертикальным выравниванием текста по нижнему краю.
   */
  protected static String VERTICAL_ALIGN_BOTTOM_CELL_STYLE = "jepRia-Grid-Cell-VAlignBottom";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с вертикальным выравниванием текста по середине.
   */
  protected static String VERTICAL_ALIGN_MIDDLE_CELL_STYLE = "jepRia-Grid-Cell-VAlignMiddle";
  
  /**
   * Наименование селектора (класса стилей) для ячеек данных с переносом текста.
   */
  protected static String NORMAL_WRAP_STYLE = "jepRia-Grid-normalWrap";

  /**
   * Наименование поля в {@link com.technology.jep.jepria.shared.record.JepRecord}, соответствующее данной колонке грида.
   */
  private String fieldName;
  
  /**
   * Наименование заголовка данной колонки грида.
   */
  private String headerText;
  
  /**
   * Ширина колонки грида.
   */
  private double width;
  
  /**
   * Признак переноса текста как в колонке, так и в его заголовке.
   */
  private Boolean wrapText;

  public JepColumn(Cell<C> cell) {
    super(cell);
  }

  public JepColumn(String fieldName, String headerText, double width) {
    this(fieldName, headerText, width, (Cell<C>) new TextCell(), false);
  }

  public JepColumn(String fieldName, String headerText, double width, Cell<C> cell) {
    this(fieldName, headerText, width, cell, false);
  }

  public JepColumn(String fieldName, String headerText, double width, Cell<C> cell, Boolean wrapText) {
    super(cell);
    
    this.fieldName = fieldName;
    this.headerText = headerText;
    this.width = width;
    this.wrapText = wrapText;

    setDataStoreName(fieldName);

    HorizontalAlignmentConstant align = HasHorizontalAlignment.ALIGN_LEFT;

    if (cell instanceof JepCheckBoxCell)
      align = HasHorizontalAlignment.ALIGN_CENTER;
    else if (cell instanceof NumberCell)
      align = HasHorizontalAlignment.ALIGN_RIGHT;

    setHorizontalAlignment(align);

    addClassName(COMMON_CELL_STYLE);
    if (wrapText) {
      addClassName(NORMAL_WRAP_STYLE);
    }
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getHeaderText() {
    return headerText;
  }

  public Double getWidth() {
    return width;
  }
  
  public Boolean isWrapText() {
    return wrapText;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public C getValue(T object) {
    return (C) (object instanceof JepRecord ? ((JepRecord) object).get(fieldName) : object.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * Особенности:<br/>
   * В GWT горизонтальное выравнивание осуществляется посредством использования аттрибута align в соответствующем
   * теге td колонки таблицы, что не всегда достаточно, если присутствует перекрывающий css-стиль. Поэтому для корректного
   * выравнивания также добавлена поддержка с использованием css-стилей. 
   */
  @Override
  public void setHorizontalAlignment(HorizontalAlignmentConstant hAlign) {
    super.setHorizontalAlignment(hAlign);
    // remove old align styles if exists
    removeClassName(ALIGN_CENTER_CELL_STYLE);
    removeClassName(ALIGN_RIGHT_CELL_STYLE);
    removeClassName(ALIGN_LEFT_CELL_STYLE);
    
    if (HasHorizontalAlignment.ALIGN_CENTER.equals(hAlign)) {
      addClassName(ALIGN_CENTER_CELL_STYLE);
    } else if (HasHorizontalAlignment.ALIGN_RIGHT.equals(hAlign)) {
      addClassName(ALIGN_RIGHT_CELL_STYLE);
    } else if (HasHorizontalAlignment.ALIGN_LEFT.equals(hAlign)) {
      addClassName(ALIGN_LEFT_CELL_STYLE);
    }
  }
  
  /**
   * {@inheritDoc}
   * 
   * Особенности:<br/>
   * В GWT вертикальное выравнивание осуществляется посредством использования аттрибута valign в соответствующем
   * теге td колонки таблицы, что не всегда достаточно, если присутствует перекрывающий css-стиль. Поэтому для корректного
   * выравнивания также добавлена поддержка с использованием css-стилей.
   */
  @Override
  public void setVerticalAlignment(VerticalAlignmentConstant align) {
    super.setVerticalAlignment(align);
    // remove old align styles if exists
    removeClassName(VERTICAL_ALIGN_MIDDLE_CELL_STYLE);
    removeClassName(VERTICAL_ALIGN_TOP_CELL_STYLE);
    removeClassName(VERTICAL_ALIGN_BOTTOM_CELL_STYLE);
    
    if (HasVerticalAlignment.ALIGN_MIDDLE.equals(align)) {
      addClassName(VERTICAL_ALIGN_MIDDLE_CELL_STYLE);
    } else if (HasVerticalAlignment.ALIGN_TOP.equals(align)) {
      addClassName(VERTICAL_ALIGN_TOP_CELL_STYLE);
    } else if (HasVerticalAlignment.ALIGN_BOTTOM.equals(align)) {
      addClassName(VERTICAL_ALIGN_BOTTOM_CELL_STYLE);
    }
  }

  /**
   * Adds a name to this element's class property. If the name is already
   * present, this method has no effect.
   * 
   * @param className the class name to be added
   * @return <code>true</code> if this element did not already have the specified class name
   */
  public final boolean addClassName(String className) {
    String oldStyle = getCellStyleNames(null, null);
    String appendedStyle = JepRiaUtil.appendStrIfNotPresent(oldStyle, className);
    if (!JepRiaUtil.equalWithNull(oldStyle, appendedStyle)){
      setCellStyleNames(appendedStyle);
      return true;
    }
    return false;
  }
  
  /**
   * Removes a name from this element's class property. If the name is not
   * present, this method has no effect.
   * 
   * @param className the class name to be removed
   * @return <code>true</code> if this element had the specified class name
   */
  public final boolean removeClassName(String className) {
    String oldStyle = getCellStyleNames(null, null);
    String removedStyle = JepRiaUtil.removeStrIfPresent(oldStyle, className);
    if (!JepRiaUtil.equalWithNull(oldStyle, removedStyle)){
      setCellStyleNames(removedStyle);
      return true;
    }
    return false;
  }
}
