package com.technology.jep.jepria.client.widget.field.wysiwyg.toolbar;

import static com.technology.jep.jepria.client.widget.field.wysiwyg.toolbar.ColorPicker.BLACK;
import static com.technology.jep.jepria.client.JepRiaClientConstant.*;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.FontSize;
import com.google.gwt.user.client.ui.RichTextArea.Formatter;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.technology.jep.jepria.client.util.JepClientUtil;
import com.technology.jep.jepria.client.widget.event.JepEvent;
import com.technology.jep.jepria.client.widget.event.JepEventType;
import com.technology.jep.jepria.client.widget.event.JepListener;
import com.technology.jep.jepria.client.widget.field.ComboBox;
import com.technology.jep.jepria.client.widget.field.multistate.JepComboBoxField;
import com.technology.jep.jepria.client.widget.field.multistate.event.BeforeSelectEvent;
import com.technology.jep.jepria.client.widget.field.multistate.event.BeforeSelectEvent.BeforeSelectHandler;
import com.technology.jep.jepria.client.widget.field.wysiwyg.toolbar.images.RichTextToolbarImages;
import com.technology.jep.jepria.shared.field.option.JepOption;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

/**
 * A sample toolbar for use with {@link RichTextArea}. It provides a simple UI
 * for all rich text formatting, dynamically displayed only for the available
 * functionality.
 */
public class RichTextToolbar extends Composite {

	/**
	 * HTML Pattern for options of font choice.
	 */
	private static final String FONT_NAME_PATTERN_OPTION = "<div style=\"font-family:{0};\">{0}</div>";

	/**
	 * Default value of URL to insert object.
	 */
	private static final String PREFIX_URL = "http://";
	
	/**
	 * No label for choice of formatting.
	 */
	private static final int CHOICE_LABEL_WIDTH = 1; // zero caused exception in IE
	
	/**
	 * Choice width.
	 */
	private static final int CHOICE_WIDTH = 123;

	/**
	 * We use an inner EventHandler class to avoid exposing event methods on the
	 * RichTextToolbar itself.
	 */
	private class EventHandler implements ClickHandler, KeyUpHandler {

		public void onClick(ClickEvent event) {
			Widget sender = (Widget) event.getSource();

			if (sender == bold) {
				formatter.toggleBold();
			} else if (sender == italic) {
				formatter.toggleItalic();
			} else if (sender == underline) {
				formatter.toggleUnderline();
			} else if (sender == subscript) {
				formatter.toggleSubscript();
			} else if (sender == superscript) {
				formatter.toggleSuperscript();
			} else if (sender == strikethrough) {
				formatter.toggleStrikethrough();
			} else if (sender == indent) {
				formatter.rightIndent();
			} else if (sender == outdent) {
				formatter.leftIndent();
			} else if (sender == justifyLeft) {
				formatter.setJustification(RichTextArea.Justification.LEFT);
			} else if (sender == justifyCenter) {
				formatter.setJustification(RichTextArea.Justification.CENTER);
			} else if (sender == justifyRight) {
				formatter.setJustification(RichTextArea.Justification.RIGHT);
			} else if (sender == insertImage) {
				String url = Window.prompt(JepTexts.wysiwyg_toolbar_prompt_image(), PREFIX_URL);
				if (url != null) {
					formatter.insertImage(url);
				}
			} else if (sender == createLink) {
				String url = Window.prompt(JepTexts.wysiwyg_toolbar_prompt_link(), PREFIX_URL);
				if (url != null) {
					formatter.createLink(url);
				}
			} else if (sender == removeLink) {
				formatter.removeLink();
			} else if (sender == hr) {
				formatter.insertHorizontalRule();
			} else if (sender == ol) {
				formatter.insertOrderedList();
			} else if (sender == ul) {
				formatter.insertUnorderedList();
			} else if (sender == backColors || sender == foreColors) {
				PopupPanel p = createColorMenu(sender);
				p.showRelativeTo(sender);
			} else if (sender == removeFormat) {
				formatter.removeFormat();
			} else if (sender == richText) {
				// We use the RichTextArea's onKeyUp event to update the toolbar status.
				// This will catch any cases where the user moves the cursor using the
				// keyboard, or uses one of the browser's built-in keyboard shortcuts.
				updateStatus();
			}
		}

		public void onKeyUp(KeyUpEvent event) {
			Widget sender = (Widget) event.getSource();
			if (sender == richText) {
				// We use the RichTextArea's onKeyUp event to update the toolbar status.
				// This will catch any cases where the user moves the cursor using the
				// keyboard, or uses one of the browser's built-in keyboard shortcuts.
				updateStatus();
			}
		}
	}

	private static final FontSize[] fontSizesConstants = new FontSize[] {
			FontSize.XX_SMALL, 
			FontSize.X_SMALL, 
			FontSize.SMALL,
			FontSize.MEDIUM, 
			FontSize.LARGE, 
			FontSize.X_LARGE,
			FontSize.XX_LARGE 
	};

	private RichTextToolbarImages images = GWT.create(RichTextToolbarImages.class);
	private EventHandler handler = new EventHandler();

	private RichTextArea richText;
	private Formatter formatter;

	private VerticalPanel outer = new VerticalPanel();
	private HorizontalPanel topPanel = new HorizontalPanel();
	private HorizontalPanel bottomPanel = new HorizontalPanel();
	private ToggleButton bold;
	private ToggleButton italic;
	private ToggleButton underline;
	private ToggleButton subscript;
	private ToggleButton superscript;
	private ToggleButton strikethrough;
	private PushButton indent;
	private PushButton outdent;
	private PushButton justifyLeft;
	private PushButton justifyCenter;
	private PushButton justifyRight;
	private PushButton hr;
	private PushButton ol;
	private PushButton ul;
	private PushButton insertImage;
	private PushButton createLink;
	private PushButton removeLink;
	private PushButton removeFormat;
	private PushButton backColors;
	private PushButton foreColors;
	private JepComboBoxField fontLists;
	private JepComboBoxField fontSizes;

	/**
	 * Creates a new toolbar that drives the given rich text area.
	 * 
	 * @param richText the rich text area to be controlled
	 */
	public RichTextToolbar(RichTextArea richText) {
		this.richText = richText;
		this.formatter = richText.getFormatter();

		outer.add(topPanel);
		outer.add(bottomPanel);
		topPanel.setWidth("100%");
		bottomPanel.setWidth("100%");

		initWidget(outer);
		setStyleName("gwt-RichTextToolbar");
		richText.addStyleName("hasRichTextToolbar");

		topPanel.add(bold = createToggleButton(images.bold(), JepTexts.wysiwyg_toolbar_bold()));
		topPanel.add(italic = createToggleButton(images.italic(), JepTexts.wysiwyg_toolbar_italic()));
		topPanel.add(underline = createToggleButton(images.underline(), JepTexts.wysiwyg_toolbar_underline()));
		topPanel.add(subscript = createToggleButton(images.subscript(), JepTexts.wysiwyg_toolbar_subscript()));
		topPanel.add(superscript = createToggleButton(images.superscript(), JepTexts.wysiwyg_toolbar_superscript()));
		topPanel.add(justifyLeft = createPushButton(images.justifyLeft(), JepTexts.wysiwyg_toolbar_justifyLeft()));
		topPanel.add(justifyCenter = createPushButton(images.justifyCenter(), JepTexts.wysiwyg_toolbar_justifyCenter()));
		topPanel.add(justifyRight = createPushButton(images.justifyRight(), JepTexts.wysiwyg_toolbar_justifyRight()));

		topPanel.add(strikethrough = createToggleButton(images.strikeThrough(), JepTexts.wysiwyg_toolbar_strikeThrough()));
		topPanel.add(indent = createPushButton(images.indent(), JepTexts.wysiwyg_toolbar_indent()));
		topPanel.add(outdent = createPushButton(images.outdent(), JepTexts.wysiwyg_toolbar_outdent()));
		topPanel.add(hr = createPushButton(images.hr(), JepTexts.wysiwyg_toolbar_hr()));
		topPanel.add(ol = createPushButton(images.ol(), JepTexts.wysiwyg_toolbar_ol()));
		topPanel.add(ul = createPushButton(images.ul(), JepTexts.wysiwyg_toolbar_ul()));
		topPanel.add(insertImage = createPushButton(images.insertImage(), JepTexts.wysiwyg_toolbar_insertImage()));
		topPanel.add(createLink = createPushButton(images.createLink(), JepTexts.wysiwyg_toolbar_createLink()));
		topPanel.add(removeLink = createPushButton(images.removeLink(), JepTexts.wysiwyg_toolbar_removeLink()));
		topPanel.add(removeFormat = createPushButton(images.removeFormat(), JepTexts.wysiwyg_toolbar_removeFormat()));
	
		// background color choice
		bottomPanel.add(backColors = createColorPickerButton(JepTexts.wysiwyg_toolbar_backgroundcolor()));
		// font color
		bottomPanel.add(foreColors = createColorPickerButton(JepTexts.wysiwyg_toolbar_color()));
		bottomPanel.add(fontLists = createFontList()); // font family choice
		bottomPanel.add(fontSizes = createFontSizes()); // font size choice

		// We only use these handlers for updating status, so don't hook them up
		// unless at least basic editing is supported.
		richText.addKeyUpHandler(handler);
		richText.addClickHandler(handler);
		
	}

	private PushButton createColorPickerButton(String caption) {
		PushButton pb = new PushButton(caption);
		pb.getElement().getStyle().setTextAlign(TextAlign.CENTER);
		pb.addClickHandler(handler);
		pb.setHeight(FIELD_DEFAULT_HEIGHT + Unit.PX.getType());
		pb.setWidth(CHOICE_WIDTH + Unit.PX.getType());
		return pb;
	}

	private JepComboBoxField createFontList() {		
		return 
			createComboBox(JepTexts.wysiwyg_toolbar_font(), 
				new JepListener() {
					@Override
					public void handleEvent(JepEvent event) {
						JepComboBoxField comboBox = (JepComboBoxField) event.getSource();
						JepOption selectedOption = comboBox.getValue();
						String fontName = JepOption.<String> getValue(selectedOption);
						formatter.setFontName(fontName);
						
						comboBox.getEditableCard().setValue(selectedOption, false);
						comboBox.getEditableCard().getValueBoxBase().setText(selectedOption.getName().replaceAll("\\<.*?>", ""));
					}
				}, new JepOption[]{
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, JepTexts.wysiwyg_toolbar_normal()), 
								"inherit"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Times New Roman"), 
								"Times New Roman"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Arial"),
								"Arial"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Courier New"), 
								"Courier New"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Comic Sans Ms"), 
								"Comic Sans Ms"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Georgia"), 
								"Georgia"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Tahoma"), 
								"Tahoma"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Trebuchet"), 
								"Trebuchet"),
						new JepOption(
								JepClientUtil.substitute(FONT_NAME_PATTERN_OPTION, "Verdana"), 
								"Verdana")
				});
	}

	private JepComboBoxField createFontSizes() {
		return
			createComboBox(JepTexts.wysiwyg_toolbar_size(), 
				new JepListener() {
					@Override
					public void handleEvent(JepEvent event) {
						JepComboBoxField comboBox = (JepComboBoxField) event.getSource();
						JepOption selectedOption = comboBox.getValue();
						formatter.setFontSize(fontSizesConstants[JepOption.<Integer>getValue(selectedOption) - 1]);
						comboBox.getEditableCard().setValue(selectedOption, false);
						comboBox.getEditableCard().getValueBoxBase().setText(selectedOption.getName());
					}
				}, new JepOption[]{
						new JepOption(JepTexts.wysiwyg_toolbar_xxsmall(), 1),
						new JepOption(JepTexts.wysiwyg_toolbar_xsmall(), 2),
						new JepOption(JepTexts.wysiwyg_toolbar_small(), 3),
						new JepOption(JepTexts.wysiwyg_toolbar_medium(), 4),
						new JepOption(JepTexts.wysiwyg_toolbar_large(), 5),
						new JepOption(JepTexts.wysiwyg_toolbar_xlarge(), 6),
						new JepOption(JepTexts.wysiwyg_toolbar_xxlarge(), 7)
				}
			);
	}
	
	private JepComboBoxField createComboBox(String emptyText, JepListener changeSelectionListener, JepOption... options){
		final JepComboBoxField lb = new JepComboBoxField();

		// уберем занимаемое лейблом местом
		lb.setLabelWidth(CHOICE_LABEL_WIDTH);
		// выставим длину поля
		lb.setFieldWidth(CHOICE_WIDTH);
		lb.setEmptyText(emptyText);
		// уберем пустую опцию из списка значений
		lb.setOptions(new ArrayList<JepOption>(Arrays.asList(options)), false);
		// навесим слушателя смены опции
		lb.addListener(JepEventType.CHANGE_SELECTION_EVENT, changeSelectionListener);

		ComboBox<JepOption> comboBox = lb.getEditableCard();
		comboBox.addBeforeSelectHandler(
			new BeforeSelectHandler<JepOption>() {
				@Override
				public void onBeforeSelect(BeforeSelectEvent<JepOption> event) {
					// отменим выбор той же опции
					if (event.getSelectedItem().equals(lb.getValue())) {
						event.setCancelled(true);
					}
				}
			});
		// отключаем возможность фильтра в комбобоксе
		comboBox.getValueBoxBase().setReadOnly(true);

		return lb;
	}

	private PushButton createPushButton(ImageResource img, String tip) {
		PushButton pb = new PushButton(new Image(img));
		pb.addClickHandler(handler);
		pb.setTitle(tip);
		return pb;
	}

	private ToggleButton createToggleButton(ImageResource img, String tip) {
		ToggleButton tb = new ToggleButton(new Image(img));
		tb.addClickHandler(handler);
		tb.setTitle(tip);
		return tb;
	}

	/**
	 * Updates the status of all the stateful buttons.
	 */
	private void updateStatus() {
		bold.setDown(formatter.isBold());
		italic.setDown(formatter.isItalic());
		underline.setDown(formatter.isUnderlined());
		subscript.setDown(formatter.isSubscript());
		superscript.setDown(formatter.isSuperscript());
		strikethrough.setDown(formatter.isStrikethrough());
	}
	
	/**
	 * Restore default state of toolbar and its widgets
	 */
	public void reset(){
		updateStatus();
		
		fontLists.clear();
		fontSizes.clear();
		
		backColors.getElement().getStyle().setColor(BLACK);
		foreColors.getElement().getStyle().setColor(BLACK);
	}
	
	protected PopupPanel createColorMenu(final Widget button) {
		final PopupPanel panel = new DecoratedPopupPanel(true);
	    panel.setPreviewingAllNativeEvents(true);
	    panel.setAnimationType(AnimationType.ROLL_DOWN);
	    panel.getElement().getStyle().setZIndex(10);
	    final boolean isBackground = button == backColors;
	    
	    ColorPicker colorPicker = new ColorPicker(isBackground ? "#FFFFFF" : BLACK); // black and white
	    colorPicker.addSelectionHandler(new SelectionHandler<String>() {
			@Override
			public void onSelection(SelectionEvent<String> event) {
				String color = event.getSelectedItem();
				button.getElement().getStyle().setColor(color);
				if (isBackground) {
					formatter.setBackColor(color);
				}
				else if (button == foreColors) {
					formatter.setForeColor(color);
				}
				panel.hide();
			}
		});
		panel.setWidget(colorPicker);
	    return panel;
	}
	
}