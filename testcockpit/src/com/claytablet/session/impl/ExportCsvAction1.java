/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.claytablet.session.impl;


import de.hybris.platform.cockpit.components.contentbrowser.MultiTypeListMainAreaComponentFactory;
import de.hybris.platform.cockpit.components.listview.AbstractMultiSelectOnlyAction;
import de.hybris.platform.cockpit.components.listview.ListViewAction;
import de.hybris.platform.cockpit.model.listview.ColumnDescriptor;
import de.hybris.platform.cockpit.model.listview.ColumnModel;
import de.hybris.platform.cockpit.model.listview.TableModel;
import de.hybris.platform.cockpit.model.meta.PropertyDescriptor;
import de.hybris.platform.cockpit.model.meta.TypedObject;
import de.hybris.platform.cockpit.services.config.ColumnConfiguration;
import de.hybris.platform.cockpit.services.config.impl.MultiTypeColumnConfiguration;
import de.hybris.platform.cockpit.services.label.LabelService;
import de.hybris.platform.cockpit.services.values.ValueHandlerException;
import de.hybris.platform.cockpit.session.BrowserModel;
import de.hybris.platform.cockpit.session.UISessionUtils;
import de.hybris.platform.cockpit.wizards.Wizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Menupopup;


public class ExportCsvAction1 extends AbstractMultiSelectOnlyAction
{
	protected static final String ICON_EXPORT_CSV_ACTION_AVAILABLE = "cockpit/images/icon_func_export_csv_22.png";
	protected static final String ICON_EXPORT_CSV_ACTION_UNAVAILABLE = "cockpit/images/icon_func_export_csv_unavailable_22.png";
	private boolean truncateCollections;
	private LabelService labelService;

	@Override
	public EventListener getMultiSelectEventListener(final ListViewAction.Context context)
	{
		EventListener ret = null;
		final List selectedItems = getSelectedItems(context);

		if ((context.getModel() != null) && (selectedItems != null) && (!selectedItems.isEmpty()) && (selectedItems.size() >= 1))
		{
			ret = new ExportCsvAction11(this, context, selectedItems);
		}

		return ret;
	}

	@Override
	public String getMultiSelectImageURI(final ListViewAction.Context context)
	{
		final List selectedItems = getSelectedItems(context);

		if ((context.getModel() != null) && (selectedItems != null) && (!selectedItems.isEmpty()) && (selectedItems.size() >= 1))
		{
			return "cockpit/images/icon_func_export_csv_22.png";
		}
		return "cockpit/images/icon_func_export_csv_unavailable_22.png";
	}

	@Override
	public Menupopup getMultiSelectPopup(final ListViewAction.Context context)
	{
		return null;
	}

	@Override
	public String getTooltip(final ListViewAction.Context context)
	{
		return Labels.getLabel("exportcsvaction.tooltip");
	}

	private void createAndSaveCSVForMultitype(
			final MultiTypeListMainAreaComponentFactory.MultiTypeListViewConfiguration multiTypeListViewConfiguration,
			final List<TypedObject> list) throws ValueHandlerException
	{
		final StringBuilder csvContent = new StringBuilder();

		final List<ColumnConfiguration> columnConfigurations = new ArrayList();

		Iterator localIterator1 = multiTypeListViewConfiguration.getRootColumnGroupConfiguration().getColumnConfigurations()
				.iterator();

		while (localIterator1.hasNext())
		{
			final ColumnConfiguration columnConfiguration = (ColumnConfiguration) localIterator1.next();

			if ((!(columnConfiguration instanceof MultiTypeColumnConfiguration))
					|| (!((MultiTypeColumnConfiguration) columnConfiguration).getColumnDescriptor().isVisible()))
			{
				continue;
			}
			columnConfigurations.add(columnConfiguration);
		}

		//		for (final MultiTypeColumnConfiguration columnConfiguration : columnConfigurations)
		//		{
		//			csvContent.append(columnConfiguration.getColumnDescriptor().getName()).append(";");
		//		}

		csvContent.append("\n");

		for (localIterator1 = list.iterator(); localIterator1.hasNext();)
		{
			final Object item = localIterator1.next();

			for (final ColumnConfiguration columnConfiguration : columnConfigurations)
			{
				final String columnValue = getEscapedCsvColumnValue(String
						.valueOf(((MultiTypeColumnConfiguration) columnConfiguration).getValueHandler().getValue((TypedObject) item)));
				csvContent.append(columnValue).append(";");
			}
			csvContent.append("\n");
		}

		Filedownload.save(csvContent.toString(), "text/comma-separated-values;charset=UTF-8", "list.csv");
	}

	private String getEscapedCsvColumnValue(final String value)
	{
		String ret = value;
		if ((value.contains(";")) || (value.contains("\"")))
		{
			ret = "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return ret.replace('\n', ' ');
	}

	private void createAndSaveCSV(final ListViewAction.Context context)
	{
		final BrowserModel browser = context.getBrowserModel();
		final TableModel tableModel = context.getModel();

		if ((browser != null) && (tableModel != null))
		{
			final List<TypedObject> allItems = browser.getSelectedItems();
			final List<ColumnDescriptor> visibleColumns = tableModel.getColumnComponentModel().getVisibleColumns();

			if (allItems != null)
			{
				//				final List localizedPropertyDescriptors = new ArrayList();
				final Set propertyDescriptors = new HashSet();

				final StringBuffer buffer = new StringBuffer();

				for (final ColumnDescriptor columnDescriptor : visibleColumns)
				{
					final ColumnModel columnComponentModel = tableModel.getColumnComponentModel();
					final PropertyDescriptor propertyDescriptor = columnComponentModel.getPropertyDescriptor(columnDescriptor);
					if (propertyDescriptor == null)
					{
						continue;
					}
					//					localizedPropertyDescriptors.add(new ExportCsvAction.LocalizedProperty(this, propertyDescriptor, columnDescriptor
					//							.getLanguage() == null ? null : columnDescriptor.getLanguage().getIsocode()));
					propertyDescriptors.add(propertyDescriptor);
					buffer.append(columnDescriptor.getName() + ";");
				}

				buffer.append('\n');

				for (final TypedObject item : allItems)
				{
					buffer.append("" + item.getType());
				}

				Filedownload.save(buffer.toString(), "text/comma-separated-values;charset=UTF-8", "list.csv");
			}
		}
	}

	//	private String getCSVRow(final TypedObject item, final List<ExportCsvAction.LocalizedProperty> localizedPropertyDescriptors,
	//			final Set<PropertyDescriptor> descriptors)
	//	{
	//		final StringBuffer buffer = new StringBuffer();
	//		final ObjectValueContainer valueContainer = TypeTools.createValueContainer(item, descriptors, UISessionUtils
	//				.getCurrentSession().getSystemService().getAvailableLanguageIsos());
	//
	//		for (final ExportCsvAction.LocalizedProperty localizedProperty : localizedPropertyDescriptors)
	//		{
	//			Object value = null;
	//
	//			if (valueContainer.hasProperty(localizedProperty.getPropDesc(), localizedProperty.getLang()))
	//			{
	//				final ObjectValueContainer.ObjectValueHolder valueHolder = valueContainer.getValue(localizedProperty.getPropDesc(),
	//						localizedProperty.getLang());
	//				value = valueHolder.getCurrentValue();
	//			}
	//
	//			if (value != null)
	//			{
	//				String valueAsString = "";
	//				if (this.truncateCollections)
	//				{
	//					valueAsString = TypeTools.getValueAsString(this.labelService, value);
	//				}
	//				else
	//				{
	//					valueAsString = TypeTools.getValueAsString(this.labelService, value, -1);
	//				}
	//
	//				if (valueAsString == null)
	//				{
	//					valueAsString = "";
	//				}
	//				else
	//				{
	//					valueAsString = getEscapedCsvColumnValue(valueAsString);
	//				}
	//				buffer.append(valueAsString);
	//			}
	//			buffer.append(';');
	//		}
	//
	//		buffer.append('\n');
	//
	//		return buffer.toString();
	//	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public LabelService getLabelService()
	{
		if (this.labelService == null)
		{
			this.labelService = UISessionUtils.getCurrentSession().getLabelService();
		}
		return this.labelService;
	}

	public boolean isTruncateCollections()
	{
		return this.truncateCollections;
	}

	public void setTruncateCollections(final boolean truncateCollections)
	{
		this.truncateCollections = truncateCollections;
	}


	class ExportCsvAction11 implements EventListener
	{
		/**
		 *
		 */
		public ExportCsvAction11(final ExportCsvAction1 exportCsvAction1, final Context context, final List selectedItems)
		{
			System.out.println("List of selected items retrieved.." + selectedItems.size());
		}

		public void onEvent(final Event event) throws Exception
		{
			System.out.println("onEvent called");
			Wizard.show("createCustomerWizard");
			//			final TableModel tableModel = this.val$context.getModel();
			//			if (tableModel != null)
			//			{
			//				final ColumnModel columnComponentModel = tableModel.getColumnComponentModel();
			//				if (((columnComponentModel instanceof DefaultColumnModel))
			//						&& ((((DefaultColumnModel) columnComponentModel).getConfiguration() instanceof MultiTypeListMainAreaComponentFactory.MultiTypeListViewConfiguration)))
			//				{
			//					ExportCsvAction
			//							.access$0(
			//									this.this$0,
			//									(MultiTypeListMainAreaComponentFactory.MultiTypeListViewConfiguration) ((DefaultColumnModel) columnComponentModel)
			//											.getConfiguration(), this.val$selectedItems);
			//				}
			//				else
			//				{
			//					ExportCsvAction.access$1(this.this$0, this.val$context);
			//				}
			//			}
		}
	}
}
