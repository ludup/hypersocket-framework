/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.i18n.I18N;

public class OptionsDialog extends Dialog {
	
	static Logger log = LoggerFactory.getLogger(OptionsDialog.class);
	
	private Combo comboLocale;
	private Map<String,String> textToLocale = new HashMap<String,String>();
	
	String locale;
	Label lblError;
	
	SWTGui swtGui;
	/**
	 * Create the dialog.
	 * @param swtGui
	 */
	public OptionsDialog(SWTGui swtGui) {
		super(swtGui.getShell());
		this.swtGui = swtGui;
		setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		TabFolder tabFolder = new TabFolder(container, SWT.NONE);
	

		TabItem tbtmPreferences = new TabItem(tabFolder, SWT.NONE);
		tbtmPreferences.setText(I18N.getResource("client.pref.label"));
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmPreferences.setControl(composite_1);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginLeft = 50;
		gl_composite_1.marginTop = 10;
		composite_1.setLayout(gl_composite_1);
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		lblNewLabel.setText(I18N.getResource("client.locale.label") + ":");
		
		comboLocale = new Combo(composite_1, SWT.NONE);
		GridData gd_comboLocale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboLocale.widthHint = 200;
		comboLocale.setLayoutData(gd_comboLocale);
		addLanguage("en");
		
		/**
		 * Other languages currently not supported.
		 */
//		addLanguage("da");
//		addLanguage("nl");
//		addLanguage("fi");
//		addLanguage("fr");
//		addLanguage("de");
//		addLanguage("it");
//		addLanguage("no");
//		addLanguage("pl");
//		addLanguage("ru");
//		addLanguage("sv");
//		addLanguage("es");
//		addLanguage("ja");

		return container;
	}

	private void addLanguage(String language) {
		textToLocale.put(I18N.getResource("lang." + language), language);
		comboLocale.add(I18N.getResource("lang." + language));
		
		String currentLocale;
		try {
			currentLocale = swtGui.getConfigurationService().getValue("ui.locale", "en");
			if(currentLocale.equals(language)) {
				comboLocale.setText(I18N.getResource("lang." + language));
			}
		} catch (RemoteException e) {
			log.error("Failed to get configuration value", e);
		}
		
	}
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, I18N.getResource("text.ok"),
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				I18N.getResource("text.cancel"), false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	
	public void saveOptions() throws IOException {
	
		try {
			swtGui.getConfigurationService().setValue("ui.locale", locale);
		} catch (RemoteException e) {
			throw new IOException("Failed to save values");
		}
	}
	
	protected void okPressed() {

		locale = textToLocale.get(comboLocale.getText());
		super.okPressed();
	}
}
