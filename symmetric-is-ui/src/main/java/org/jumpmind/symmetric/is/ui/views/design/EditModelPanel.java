package org.jumpmind.symmetric.is.ui.views.design;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jumpmind.symmetric.is.core.model.DataType;
import org.jumpmind.symmetric.is.core.model.Model;
import org.jumpmind.symmetric.is.core.model.ModelAttribute;
import org.jumpmind.symmetric.is.core.model.ModelEntity;
import org.jumpmind.symmetric.is.ui.common.ApplicationContext;
import org.jumpmind.symmetric.is.ui.common.ButtonBar;
import org.jumpmind.symmetric.ui.common.IUiPanel;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditModelPanel extends VerticalLayout implements IUiPanel {

	ApplicationContext context;
	
	TreeTable treeTable = new TreeTable();
	
	Model model;

	Set<Object> lastEditItemIds = Collections.emptySet();
	
	TableColumnSelectWindow tableColumnSelectWindow;
	
	Button addEntityButton;
    
    Button addAttributeButton;

    Button editButton;
    
    Button removeButton;

    Button importButton;
    
    TextField filterField;
	
	public EditModelPanel(ApplicationContext context, Model model) {
		this.context = context;
		this.model = model;

		ButtonBar buttonBar = new ButtonBar();
		addComponent(buttonBar);

		addEntityButton = buttonBar.addButton("Add Entity", FontAwesome.TABLE);
		addEntityButton.addClickListener(new AddEntityClickListener());
		
		addAttributeButton = buttonBar.addButton("Add Attribute", FontAwesome.COLUMNS);
		addAttributeButton.addClickListener(new AddAttributeClickListener());

		editButton = buttonBar.addButton("Edit", FontAwesome.EDIT);
		editButton.addClickListener(new EditClickListener());
		
		removeButton = buttonBar.addButton("Remove", FontAwesome.TRASH_O);
		removeButton.addClickListener(new RemoveClickListener());

		importButton = buttonBar.addButton("Import ...", FontAwesome.DOWNLOAD);
		importButton.addClickListener(new ImportClickListener());
		
		filterField = buttonBar.addFilter();
		filterField.addTextChangeListener(new TextChangeListener() {
            
            @Override
            public void textChange(TextChangeEvent event) {
                setButtonsEnabled(event.getText());
                treeTable.removeAllItems();
                addAll(event.getText(), EditModelPanel.this.model.getModelEntities());
            }
        });

		treeTable.setSizeFull();
		treeTable.setCacheRate(100);
		treeTable.setPageLength(100);
		treeTable.setImmediate(true);
        treeTable.setSelectable(true);
        treeTable.setMultiSelect(true);
        
        treeTable.addContainerProperty("name", Label.class, "", "Name", null, null);
        treeTable.addContainerProperty("type", String.class, "", "Type", null, null);
        treeTable.setVisibleColumns(new Object[] { "name", "type" });
        treeTable.addItemClickListener(new TreeTableItemClickListener());
        treeTable.addValueChangeListener(new TreeTableValueChangeListener());
        
		addComponent(treeTable);
		setExpandRatio(treeTable, 1.0f);
		addAll("", model.getModelEntities());
		setButtonsEnabled("");
	}
	
	public void setButtonsEnabled(String filter) {
	    boolean noFilter = isBlank(filter);	    
	    Set<Object> selected = getSelectedItems();	    
	    addEntityButton.setEnabled(noFilter);
	    importButton.setEnabled(noFilter);
	    addAttributeButton.setEnabled(noFilter);	    
	    removeButton.setEnabled(selected.size() > 0);	    
	    editButton.setEnabled(selected.size() > 0);	    	    
	}

	@Override
	public boolean closing() {
		return true;
	}

	@Override
	public void selected() {
	}
	
    @Override
    public void deselected() {
    }

	@SuppressWarnings("unchecked")
	protected Set<Object> getSelectedItems() {
		return (Set<Object>) treeTable.getValue();
	}
	
	protected void selectOnly(Object itemId) {
        for (Object id: getSelectedItems()) {
        	treeTable.unselect(id);
        }
        treeTable.select(itemId);
	}

	protected void add(String filter, ModelEntity modelEntity) {
		addModelEntity(filter, modelEntity);
		for (ModelAttribute modelAttribute : modelEntity.getModelAttributes()) {
			modelAttribute.setEntity(modelEntity);
			addModelAttribute(filter, modelAttribute);
		}
		treeTable.setCollapsed(modelEntity, false);
	}

    protected void addAll(String filter, Collection<ModelEntity> modelEntityList) {
        for (ModelEntity modelEntity : modelEntityList) {
            boolean add = isBlank(filter) || modelEntity.getName().contains(filter);
            if (!add) {
                for (ModelAttribute modelAttribute : modelEntity.getModelAttributes()) {
                    add |= modelAttribute.getName().contains(filter);
                }
            }

            if (add) {
                add(filter, modelEntity);
            }
        }
    }
    
    protected Label getName(String filter, String name) {        
        if (isNotBlank(filter) && name.contains(filter)) {
            int start = name.indexOf(filter);
            String pre = start < name.length() ? name.substring(0, start) : "";
            String highlighted = name.substring(start, start + filter.length());
            String post = start + filter.length() < name.length() ? name.substring(start
                    + filter.length()) : "";
            name = pre + "<span class='highlight'>" + highlighted + "</span>" + post;
        }
        Label label = new Label(name, ContentMode.HTML);
        return label;
    }

	@SuppressWarnings("unchecked")
	protected void addModelEntity(String filter, ModelEntity modelEntity) {
		treeTable.addItem(modelEntity);
		treeTable.getContainerProperty(modelEntity, "name").setValue(getName(filter, modelEntity.getName()));
		treeTable.setItemIcon(modelEntity, FontAwesome.TABLE);
		treeTable.setChildrenAllowed(modelEntity, true);
	}

	@SuppressWarnings("unchecked")
	protected void addModelAttribute(String filter, ModelAttribute modelAttribute) {
		treeTable.addItem(modelAttribute);
		treeTable.getContainerProperty(modelAttribute, "name").setValue(getName(filter, modelAttribute.getName()));
		treeTable.getContainerProperty(modelAttribute, "type").setValue(modelAttribute.getType().toString());
		treeTable.setItemIcon(modelAttribute, FontAwesome.COLUMNS);
		treeTable.setParent(modelAttribute, modelAttribute.getEntity());
		treeTable.setChildrenAllowed(modelAttribute, false);
	}

	protected void editSelectedItem() {
		lastEditItemIds = getSelectedItems();
        treeTable.setTableFieldFactory(new TableFieldFactory() {
			public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
				if (lastEditItemIds.contains(itemId)) {
					if (propertyId.equals("name")) {
						TextField t = new TextField();
						t.focus();
						t.selectAll();
						return t;
					} else if (propertyId.equals("type") && itemId instanceof ModelAttribute) {
						ComboBox cbox = new ComboBox();
						cbox.setNullSelectionAllowed(false);
						for (DataType dataType : DataType.values()) {
							cbox.addItem(dataType.toString());	
						}
						return cbox;
					}
				}
				return null;
			}
        });
        treeTable.setEditable(true);
	}

	class AddEntityClickListener implements ClickListener {
		public void buttonClick(ClickEvent event) {
	        ModelEntity e = new ModelEntity();
	        e.setName("New Entity");
	        e.setModelId(model.getId());
	        context.getConfigurationService().save(e);
	        addModelEntity("", e);
	        selectOnly(e);
	        editSelectedItem();
		}
	}

	class AddAttributeClickListener implements ClickListener {
		public void buttonClick(ClickEvent event) {
			Set<Object> itemIds = getSelectedItems();
			if (itemIds.size() > 0) {
		        ModelAttribute a = new ModelAttribute();
		        a.setName("New Attribute");
		        a.setDataType(DataType.VARCHAR);
				Object itemId = itemIds.iterator().next();
				if (itemId instanceof ModelEntity) {
					a.setEntity((ModelEntity) itemId);
				} else if (itemId instanceof ModelAttribute) {
					a.setEntity(((ModelAttribute) itemId).getEntity());					
				}
				context.getConfigurationService().save(a);
		        addModelAttribute("", a);
		        treeTable.setCollapsed(a.getEntity(), false);
		        selectOnly(a);
		        editSelectedItem();		        
			}
		}
	}

	class EditClickListener implements ClickListener {
		public void buttonClick(ClickEvent event) {
			editSelectedItem();
		}
	}

	class RemoveClickListener implements ClickListener {
		@SuppressWarnings("unchecked")
		public void buttonClick(ClickEvent event) {
			Set<Object> itemIds = new HashSet<Object>();
			Set<Object> selectedIds = getSelectedItems();
			
			for (Object itemId : selectedIds) {
				Collection<Object> children = (Collection<Object>) treeTable.getContainerDataSource().getChildren(itemId);
				if (children != null) {
					itemIds.addAll(children);
				}
				itemIds.add(itemId);
			}

			for (Object itemId : itemIds) {
				if (itemId instanceof ModelAttribute) {
					ModelAttribute a = (ModelAttribute) itemId;
					context.getConfigurationService().delete((ModelAttribute) itemId);
					a.getEntity().removeModelAttribute(a);
					treeTable.removeItem(itemId);
				}
			}
			for (Object itemId : itemIds) {
				if (itemId instanceof ModelEntity) {
					context.getConfigurationService().delete((ModelEntity) itemId);
					treeTable.removeItem(itemId);
				}
			}
		}
	}

	class ImportClickListener implements ClickListener, TableColumnSelectListener {
		public void buttonClick(ClickEvent event) {
			if (tableColumnSelectWindow == null) {
				tableColumnSelectWindow = new TableColumnSelectWindow(context, model);
				tableColumnSelectWindow.setTableColumnSelectListener(this);
			}
			UI.getCurrent().addWindow(tableColumnSelectWindow);
		}
		
		public void selected(Collection<ModelEntity> modelEntityCollection) {
			HashMap<String, ModelEntity> existingModelEntities = new HashMap<String, ModelEntity>();
			for (Object itemId : treeTable.getItemIds()) {
				if (itemId instanceof ModelEntity) {
					ModelEntity modelEntity = (ModelEntity) itemId;
					existingModelEntities.put(modelEntity.getName().toUpperCase(), modelEntity);
				}
			}

			for (ModelEntity e : modelEntityCollection) {
				ModelEntity modelEntity = existingModelEntities.get(e.getName().toUpperCase());
				if (modelEntity == null) {
					context.getConfigurationService().save(e);
					existingModelEntities.put(e.getName().toUpperCase(), e);
					add("", e);
				} else {
					for (ModelAttribute a : e.getModelAttributes()) {
						if (modelEntity.getModelAttributeByName(a.getName()) == null) {
							a.setEntity(modelEntity);
							context.getConfigurationService().save(a);							
							modelEntity.addModelAttribute(a);
							addModelAttribute("", a);
						}
					}
				}
			}
		}
	}

	class TreeTableItemClickListener implements ItemClickListener {
		public void itemClick(ItemClickEvent event) {
			if (event.isDoubleClick()) {
				editSelectedItem();				
			}
		}		
	}

	class TreeTableValueChangeListener implements ValueChangeListener {
		public void valueChange(ValueChangeEvent event) {
			for (Object itemId : lastEditItemIds) {
				if (itemId instanceof ModelEntity) {
					ModelEntity e = (ModelEntity) itemId;
					e.setName((String) treeTable.getContainerProperty(itemId, "name").getValue());
					context.getConfigurationService().save(e);
				} else if (itemId instanceof ModelAttribute) {
					ModelAttribute a = (ModelAttribute) itemId;
					a.setName((String) treeTable.getContainerProperty(itemId, "name").getValue());
					a.setType((String) treeTable.getContainerProperty(itemId, "type").getValue());
					context.getConfigurationService().save(a);
				}
			}
	        treeTable.setEditable(false);
	        setButtonsEnabled(filterField.getValue());
		}		
	}

}
