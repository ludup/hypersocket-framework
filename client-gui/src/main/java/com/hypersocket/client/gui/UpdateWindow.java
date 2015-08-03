package com.hypersocket.client.gui;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.hypersocket.client.i18n.I18N;

public class UpdateWindow extends Window {

	private CLabel message;
	private Composite progressBarComposite;
	private Button cancelButton;
	private ProgressBar progressBar;
	private Label processMessageLabel;
	private boolean cancelled;
	private boolean cancelWillClose;

	public UpdateWindow(Shell parentShell) {
		super(parentShell);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void show() {
		setShellStyle(SWT.SHELL_TRIM & (~SWT.RESIZE));
		open();
	}

	@Override
	protected void constrainShellSize() {
		Shell shell = getShell();
		shell.pack();

		Rectangle screenSize = shell.getDisplay().getPrimaryMonitor()
				.getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
				(screenSize.height - shell.getBounds().height) / 2);

		super.constrainShellSize();
	}

	public void awaitingNewService() {
		message.setText(I18N.getResource("client.update.awaitingServiceStart"));
		progressBar.setSelection(progressBar.getMaximum());
	}

	public void done() {
		message.setText(I18N.getResource("client.update.completed"));
		progressBar.setSelection(progressBar.getMaximum());
		cancelButton.setEnabled(false);
	}

	public void complete(String app) {
		message.setText(I18N.getResource("client.update.completedAppUpdate",
				app));
		progressBar.setSelection(progressBar.getMaximum());
	}

	public void failure(String app, String failureMessage) {
		message.setImage(new Image(getShell().getDisplay(), Main.class
				.getClassLoader().getResourceAsStream("red-led.png")));
		if (app == null) {
			message.setText(I18N.getResource("client.update.failedUpdates",
					failureMessage));
		} else {
			message.setText(I18N.getResource("client.update.failedAppUpdate",
					app, failureMessage));
		}
		progressBar.setSelection(progressBar.getMaximum());
		cancelButton.setText(I18N.getResource("client.update.close"));
		cancelButton.setEnabled(true);
		cancelWillClose = true;
	}

	public void start(String app, long totalBytesExpected) {
		message.setText(I18N
				.getResource("client.update.startingAppUpdate", app));
		progressBar.setMaximum((int) totalBytesExpected);
		progressBar.setSelection(0);
	}

	public void progress(String app, long sincelastProgress, long totalSoFar) {
		progressBar.setSelection((int) totalSoFar);
	}

	@Override
	protected Control createContents(Composite container) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;
		gridLayout.marginLeft = 8;
		gridLayout.marginRight = 8;

		Composite composite = new Composite(container, SWT.NONE);

		composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		composite.setLayout(new GridLayout());

		message = new CLabel(composite, SWT.NONE);
		message.setImage(new Image(getShell().getDisplay(), Main.class
				.getClassLoader().getResourceAsStream("green-led.png")));
		message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, false));
		message.setText("");

		progressBarComposite = new Composite(container, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false,
				false);
		gridData.widthHint = 400;
		progressBarComposite.setLayoutData(gridData);
		progressBarComposite.setLayout(new FillLayout());

		progressBar = new ProgressBar(progressBarComposite, SWT.SMOOTH);
		progressBar.setMaximum(100);

		processMessageLabel = new Label(container, SWT.NONE);
		processMessageLabel.setLayoutData(gridData);
		Label lineLabel = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		lineLabel.setLayoutData(gridData);

		// Button bar

		Composite buttons = new Composite(container, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		buttons.setLayout(gridLayout_1);

		cancelButton = new Button(buttons, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cancelWillClose) {
					close();
				} else {
					cancelled = true;
					cancelButton.setEnabled(false);
				}
			}
		});
		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText(I18N.getResource("client.update.cancel"));

		return container;
	}
}
