package com.hypersocket.client.gui;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.Connection;

public class ConnectionDialog extends Dialog {

	protected Object result;
	//protected Shell shell;
	private Text txtHostname;
	private Text txtPort;
	private Text txtPath;
	private Text txtRealm;
	private Text txtUsername;
	private Text txtPassword;
	private Button btnConnectAtStartup;
	private Button btnStayConnected;
	
	SWTGui swtGui;
	Connection connection;
	ClientService clientService;
	/**
	 * Create the dialog.
	 * @param swtGui
	 * @param connection
	 * @param clientService
	 */
	public ConnectionDialog(SWTGui swtGui, Connection connection, ClientService clientService) {
		super(swtGui.getShell());
		this.swtGui = swtGui;
		this.connection = connection;
		this.clientService = clientService;
		setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL);
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
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FormLayout());
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.left = new FormAttachment(0, 21);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Host:");
		
		txtHostname = new Text(composite, SWT.BORDER);
		fd_lblNewLabel.right = new FormAttachment(txtHostname, -24);
		fd_lblNewLabel.top = new FormAttachment(txtHostname, 3, SWT.TOP);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 106);
		fd_text.right = new FormAttachment(100, -53);
		txtHostname.setLayoutData(fd_text);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText(I18N.getResource("text.port") + ":");
		
		txtPort = new Text(composite, SWT.BORDER);
		fd_lblNewLabel_1.top = new FormAttachment(txtPort, 3, SWT.TOP);
		txtPort.setText("443");
		FormData fd_text_1 = new FormData();
		fd_text_1.top = new FormAttachment(txtHostname, 6);
		fd_text_1.left = new FormAttachment(txtHostname, 0, SWT.LEFT);
		fd_text_1.right = new FormAttachment(100, -300);
		txtPort.setLayoutData(fd_text_1);
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		fd_lblNewLabel_1.left = new FormAttachment(lblNewLabel_2, 2, SWT.LEFT);
		FormData fd_lblNewLabel_2 = new FormData();
		fd_lblNewLabel_2.left = new FormAttachment(0, 54);
		lblNewLabel_2.setLayoutData(fd_lblNewLabel_2);
		lblNewLabel_2.setText("Path:");
		
		txtPath = new Text(composite, SWT.BORDER);
		fd_lblNewLabel_2.right = new FormAttachment(txtPath, -24);
		fd_lblNewLabel_2.top = new FormAttachment(txtPath, 3, SWT.TOP);
		txtPath.setText("/hypersocket");
		FormData fd_txthypersocket = new FormData();
		fd_txthypersocket.top = new FormAttachment(txtPort, 6);
		fd_txthypersocket.left = new FormAttachment(txtHostname, 0, SWT.LEFT);
		fd_txthypersocket.right = new FormAttachment(100, -159);
		txtPath.setLayoutData(fd_txthypersocket);
		
		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.top = new FormAttachment(lblNewLabel_2, 59);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("Realm:");
		
		txtRealm = new Text(composite, SWT.BORDER);
		fd_lblNewLabel_3.right = new FormAttachment(txtRealm, -21);
		FormData fd_text_2 = new FormData();
		fd_text_2.top = new FormAttachment(lblNewLabel_3, -3, SWT.TOP);
		fd_text_2.left = new FormAttachment(0, 106);
		fd_text_2.right = new FormAttachment(txtPath, 0, SWT.RIGHT);
		txtRealm.setLayoutData(fd_text_2);
		
		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.top = new FormAttachment(lblNewLabel_3, 11);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Username:");
		
		txtUsername = new Text(composite, SWT.BORDER);
		fd_lblNewLabel_4.right = new FormAttachment(txtUsername, -21);
		FormData fd_text_3 = new FormData();
		fd_text_3.top = new FormAttachment(lblNewLabel_4, -3, SWT.TOP);
		fd_text_3.left = new FormAttachment(0, 106);
		fd_text_3.right = new FormAttachment(txtPath, 0, SWT.RIGHT);
		txtUsername.setLayoutData(fd_text_3);
		
		Label lblNewLabel_5 = new Label(composite, SWT.NONE);
		FormData fd_lblNewLabel_5 = new FormData();
		fd_lblNewLabel_5.top = new FormAttachment(lblNewLabel_4, 11);
		lblNewLabel_5.setLayoutData(fd_lblNewLabel_5);
		lblNewLabel_5.setText("Password:");
		
		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		fd_lblNewLabel_5.right = new FormAttachment(txtPassword, -21);
		FormData fd_text_4 = new FormData();
		fd_text_4.top = new FormAttachment(lblNewLabel_5, -3, SWT.TOP);
		fd_text_4.left = new FormAttachment(0, 106);
		fd_text_4.right = new FormAttachment(txtPath, 0, SWT.RIGHT);
		txtPassword.setLayoutData(fd_text_4);
		
		btnConnectAtStartup = new Button(composite, SWT.CHECK);
		FormData fd_btnCheckButton = new FormData();
		fd_btnCheckButton.left = new FormAttachment(txtHostname, 0, SWT.LEFT);
		btnConnectAtStartup.setLayoutData(fd_btnCheckButton);
		btnConnectAtStartup.setText(I18N.getResource("text.connectAtStartup"));
		
		btnStayConnected = new Button(composite, SWT.CHECK);
		fd_btnCheckButton.bottom = new FormAttachment(btnStayConnected, -6);
		FormData fd_btnCheckButton_1 = new FormData();
		fd_btnCheckButton_1.bottom = new FormAttachment(txtRealm, -6);
		fd_btnCheckButton_1.left = new FormAttachment(txtHostname, 0, SWT.LEFT);
		btnStayConnected.setLayoutData(fd_btnCheckButton_1);
		btnStayConnected.setText(I18N.getResource("text.stayConnected"));

		loadConnection();
		
		return composite;
	}
	
	protected void loadConnection() {
		if(connection!=null) {
			txtHostname.setText(connection.getHostname());
			txtPort.setText(Integer.toString(connection.getPort()));
			txtPath.setText(connection.getPath());
			txtRealm.setText(connection.getRealm());
			txtUsername.setText(connection.getUsername());
			txtPassword.setText(connection.getHashedPassword());
			btnConnectAtStartup.setSelection(connection.isConnectAtStartup());
			btnStayConnected.setSelection(connection.isStayConnected());
		}
	}
	protected void okPressed() {

		boolean invalid = StringUtils.isEmpty(txtHostname.getText());
		invalid |= StringUtils.isEmpty(txtPort.getText());
		invalid |= StringUtils.isEmpty(txtPath.getText());
		
		if(invalid) {
			// Error
			MessageBox mb = new MessageBox(swtGui.getShell(), SWT.OK | SWT.ICON_ERROR);
	        mb.setText(I18N.getResource("error.addConnection.title"));
	        mb.setMessage(I18N.getResource("error.minimumDetails"));
	        mb.open();
			return;
		}
		
		if(btnConnectAtStartup.getSelection() || btnStayConnected.getSelection()) {
			invalid = StringUtils.isEmpty(txtRealm.getText());
			invalid |= StringUtils.isEmpty(txtUsername.getText());
			invalid |= StringUtils.isEmpty(txtPassword.getText());
			
			if(invalid) {
				
				// Error we need a password in order to connect at startup
				MessageBox mb = new MessageBox(swtGui.getShell(), SWT.OK | SWT.ICON_ERROR);
		        mb.setText(I18N.getResource("error.addConnection.title"));
		        mb.setMessage(I18N.getResource("error.credentialsRequired"));
		        mb.open();
				return;
			}
		}
		
		if(connection==null) {
			try {
				connection = clientService.getConnectionService().createNew();
			} catch (RemoteException e) {
				MessageBox mb = new MessageBox(swtGui.getShell(), SWT.OK | SWT.ICON_ERROR);
		        mb.setText(I18N.getResource("error.addConnection.title"));
		        mb.setMessage(I18N.getResource("error.rmiError", e.getMessage()));
		        mb.open();
		        return;
			}
		}
		connection.setHostname(txtHostname.getText());
		connection.setPort(Integer.parseInt(txtPort.getText()));
		connection.setPath(txtPath.getText());
		connection.setConnectAtStartup(btnConnectAtStartup.getSelection());
		connection.setStayConnected(btnStayConnected.getSelection());
		connection.setRealm(txtRealm.getText());
		connection.setUsername(txtUsername.getText());
		connection.setHashedPassword(txtPassword.getText());
		
		Thread t = new Thread() {
			public void run() {
				try {
					connection = clientService.getConnectionService().save(connection);
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							ConnectionDialog.super.okPressed();
						}
					});
					
				} catch (RemoteException e) {
					// Error
					MessageBox mb = new MessageBox(swtGui.getShell(), SWT.OK | SWT.ICON_ERROR);
			        mb.setText(I18N.getResource("error.addConnection.title"));
			        mb.setMessage(I18N.getResource("error.rmiError", e.getMessage()));
			        mb.open();
					return;
				}
			}
		};
		
		t.start();
		
	}
	
	public Connection getConnection() {
		return connection;
	}
}
