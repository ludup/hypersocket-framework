/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.hypersocket.client.Option;
import com.hypersocket.client.Prompt;
import com.hypersocket.client.PromptType;
import com.hypersocket.client.i18n.I18N;

public class LogonDialog extends Dialog {

	List<Prompt> prompts;
	Map<String, Control> fields = new HashMap<String, Control>();
	Map<String, String> hidden = new HashMap<String, String>();
	Map<String, String> results = null;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public LogonDialog(Shell parentShell, List<Prompt> prompts) {
		super(parentShell);
		this.prompts = prompts;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_container = new GridLayout(3, false);
		gl_container.marginLeft = 10;
		container.setLayout(gl_container);

		final Image image = new Image(parent.getDisplay(),
				LogonDialog.class.getResourceAsStream("/logo.png"));

		Canvas canvas = new Canvas(container, SWT.NONE);
		GridData gd_canvas = new GridData(SWT.LEFT, SWT.TOP, true, true, 2, 1);
		gd_canvas.heightHint = 220;
		gd_canvas.widthHint = 100;
		canvas.setLayoutData(gd_canvas);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0);
			}
		});

		Composite composite = new Composite(container, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_composite.heightHint = 185;
		gd_composite.widthHint = 334;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		for (Prompt prompt : prompts) {

			switch (prompt.getType()) {
			case TEXT:
			case PASSWORD: {
				Label label = new Label(composite, SWT.NONE);
				label.setText(prompt.getLabel());
				label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
						false));

				Text text = new Text(composite, SWT.BORDER | SWT.FILL);
				text.setText(prompt.getDefaultValue());
				text.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
						false));

				if (prompt.getType() == PromptType.PASSWORD) {
					text.setEchoChar('*');
				}

				fields.put(prompt.getResourceKey(), text);
				break;
			}
			case SELECT: {
				Label label = new Label(composite, SWT.NONE);
				label.setText(prompt.getLabel());
				label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
						false));

				Combo combo = new Combo(composite, SWT.READ_ONLY);
				boolean selected = false;
				for (Option o : prompt.getOptions()) {
					combo.add(o.getName());
					if (o.isSelected()) {
						selected = true;
						combo.setText(o.getName());
					}
				}
				if (!selected && combo.getItemCount() > 0) {
					combo.setText(combo.getItem(0));
				}

				fields.put(prompt.getResourceKey(), combo);
				break;
			}
			case P: {
				Label label = new Label(composite, SWT.NONE);
				label.setText(prompt.getDefaultValue());
				label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
						false));
				break;
			}
			case HIDDEN: {
				hidden.put(prompt.getResourceKey(), prompt.getDefaultValue());
				break;
			}
			}
		}

		getShell().forceActive();
		return container;
	}

	protected void okPressed() {

		results = new HashMap<String, String>();

		for (Prompt prompt : prompts) {
			if (prompt.getType() != PromptType.P) {

				Control c = fields.get(prompt.getResourceKey());
				if (c instanceof Text) {
					results.put(prompt.getResourceKey(), ((Text) c).getText());
				} else if (c instanceof Combo) {
					results.put(prompt.getResourceKey(), ((Combo) c).getText());
				} else {
					throw new IllegalStateException("We don't support "
							+ c.getClass().getCanonicalName());
				}
			}
		}

		results.putAll(hidden);

		super.okPressed();

	}

	public Map<String, String> getResults() {
		return results;
	}

	/**
	 * Create contents of the button bar.
	 * 
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
		return new Point(459, 275);
	}
}
