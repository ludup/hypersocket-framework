package com.hypersocket.client.gui;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionStatus;

public class ConnectionsWindow extends AbstractWindow {

	static Logger log = LoggerFactory.getLogger(ConnectionsWindow.class);

	boolean updatesRequired = false;
	Image disconnectedImage;
	Image connectingImage;
	Image connectedImage;
	Image[] statusImages;

	Button btnEdit;
	Button btnConnect;

	ClientService clientService;
	SWTGui gui;
	private Table table;
	private TableViewer tableViewer;
	private ScrolledComposite scrolledComposite;
	private Action newAction;
	private Action editAction;
	private Action deleteAction;
	private Action connectAction;
	private Action disconnectAction;

	/**
	 */
	public ConnectionsWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public ConnectionsWindow(SWTGui gui, ClientService clientService) {
		super(new Shell(gui.getShell()));
		this.gui = gui;
		this.clientService = clientService;
		
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public void refresh() {
		synchronized (tableViewer) {
			tableViewer.refresh();
		}
	}

	
	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		scrolledComposite = new ScrolledComposite(container, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		tableViewer = new TableViewer(scrolledComposite, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();

		statusImages = new Image[3];
		statusImages[ConnectionStatus.CONNECTED] = connectedImage = new Image(
				gui.getShell().getDisplay(), getClass().getResourceAsStream(
						"/green-led.png"));
		statusImages[ConnectionStatus.CONNECTING] = connectingImage = new Image(
				gui.getShell().getDisplay(), getClass().getResourceAsStream(
						"/amber-led.png"));
		statusImages[ConnectionStatus.DISCONNECTED] = disconnectedImage = new Image(
				gui.getShell().getDisplay(), getClass().getResourceAsStream(
						"/red-led.png"));

		DelegatingStyledCellLabelProvider styledCellLP1 = new DelegatingStyledCellLabelProvider(
				new ConnectionLabelProvider());

		table.setLinesVisible(true);
		table.setBounds(0, 0, getWindowWidth(300), getWindowHeight(525));
		table.setHeaderVisible(false);
		tableViewer.setContentProvider(new ClientListContentProvider());

		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = 48;
			}
		});

		newAction.setEnabled(true);
		editAction.setEnabled(false);
		deleteAction.setEnabled(false);
		connectAction.setEnabled(false);
		disconnectAction.setEnabled(false);

		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				log.info("Table item selected");
				updateActions();
			}
		});

		TableViewerColumn tvc1 = new TableViewerColumn(tableViewer, SWT.NONE);
		tvc1.getColumn().setWidth(table.getBounds().width);
		tvc1.setLabelProvider(styledCellLP1);

		scrolledComposite.setContent(table);
		scrolledComposite.setMinSize(table
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		loadConnections();
		return container;
	}

	public void updateActions() {
		if(table.isDisposed()) {
			return;
		}
		table.setEnabled(!gui.isUpdating());
		TableItem[] items = table.getSelection();
		if (items == null || items.length == 0 || gui.isUpdating()) {
			newAction.setEnabled(!gui.isUpdating());
			editAction.setEnabled(false);
			deleteAction.setEnabled(false);
			connectAction.setEnabled(false);
			disconnectAction.setEnabled(false);
		} else {
			newAction.setEnabled(true);
			editAction.setEnabled(true);
			
			Connection c = (Connection) items[0].getData();
			try {
				if (clientService.getStatus(c) == ConnectionStatus.DISCONNECTED) {
					connectAction.setEnabled(true);
					disconnectAction.setEnabled(false);
					deleteAction.setEnabled(true);
				} else {
					connectAction.setEnabled(false);
					disconnectAction.setEnabled(true);
					deleteAction.setEnabled(false);
				}
			} catch (RemoteException e) {
				log.error("Could not get connection status", e);
			}
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
		{
			newAction = new Action(I18N.getResource("new.text")) {
				public void run() {
					try {
						ConnectionDialog dialog = new ConnectionDialog(gui,
								null, clientService);
						if (dialog.open() == ConnectionDialog.OK) {
							loadConnections();
						}
					} catch (Throwable e) {
						log.error("Adding connection dialog failed", e);
					}
				}
			};

			newAction.setImageDescriptor(ImageDescriptor.createFromImage(SWTResourceManager.getImage(ConnectionsWindow.class,
					 "/new.png")));
		}
		{
			editAction = new Action(I18N.getResource("edit.text")) {
				public void run() {
					try {
						TableItem[] items = table.getSelection();
						if(items==null || items.length==0) {
							btnEdit.setEnabled(false);
							btnConnect.setEnabled(false);
						} else {
							Connection c = (Connection) items[0].getData();
							ConnectionDialog dialog = new ConnectionDialog(gui,
									c, clientService);
							if (dialog.open() == ConnectionDialog.OK) {
								loadConnections();
							}
						}
					} catch (Throwable e) {
						log.error("Adding connection dialog failed", e);
					}
				}
			};
			editAction.setImageDescriptor(ImageDescriptor.createFromImage(SWTResourceManager.getImage(ConnectionsWindow.class,
					 "/edit.png")));
		}
		{
			deleteAction = new Action(I18N.getResource("delete.text")) {
				public void run() {
					try {
						Connection con = getSelectedConnection();
						if(con!=null) {
							MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION
									| SWT.YES | SWT.NO);
							messageBox.setText(I18N.getResource("delete.connection.label"));
							messageBox.setMessage(I18N.getResource(
									"delete.connection.desc", con.getHostname()));
							if(messageBox.open()==SWT.YES) {
								clientService.getConnectionService().delete(con);
								loadConnections();
							}
						}
					} catch (Throwable e) {
						log.error("Adding connection dialog failed", e);
					}
				}
			};
			deleteAction.setImageDescriptor(ImageDescriptor.createFromImage(SWTResourceManager.getImage(ConnectionsWindow.class,
					 "/delete.png")));
		}
		{
			connectAction = new Action(I18N.getResource("connect.text")) {
				public void run() {
					connectOrDisconnect();
				}
			};
			connectAction.setImageDescriptor(ImageDescriptor.createFromImage(SWTResourceManager.getImage(ConnectionsWindow.class,
			 "/network_open.png")));
		}
		{
			disconnectAction = new Action(I18N.getResource("disconnect.text")) {
				public void run() {
					connectOrDisconnect();
				}
			};
			disconnectAction.setImageDescriptor(ImageDescriptor.createFromImage(SWTResourceManager.getImage(ConnectionsWindow.class,
					 "/network_close.png")));
		}
	}

	private void connectOrDisconnect() {
		try {
			if (isConnectionSelected()) {
				final Connection c = getSelectedConnection();
				if (!clientService.isConnected(c)) {
					if (log.isInfoEnabled()) {
						log.info("Connecting client with id "
								+ c.getId() + "/" + c.getHostname());
					}
					Thread t = new Thread() {
						public void run() {
							try {
								clientService.connect(c);
							} catch (RemoteException e) {
								log.error("Failed to connect", e);
							}
						}
					};
					t.start();
					
				} else {
					if (log.isInfoEnabled()) {
						log.info("Disconnecting client with id "
								+ c.getId() + "/" + c.getHostname());
					}
					Thread t = new Thread() {
						public void run() {
							try {
								clientService.disconnect(c);
							} catch (RemoteException e) {
								log.error("Failed to disconnect", e);
							}
						}
					};
					t.start();
					
				}
			}
		} catch (Throwable e) {
			log.error("Connect/disconnect failed", e);
		} finally {
			updateActions();
		}
	}
	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");

		MenuManager menuManager_1 = new MenuManager(
				I18N.getResource("connection.text"));
		menuManager_1.add(newAction);
		menuManager_1.add(new Separator());
		menuManager_1.add(editAction);
		menuManager_1.add(deleteAction);
		menuManager_1.add(new Separator());
		menuManager_1.add(connectAction);
		menuManager_1.add(disconnectAction);
		menuManager.add(menuManager_1);
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		toolBarManager.add(newAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(editAction);
		toolBarManager.add(deleteAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(connectAction);
		toolBarManager.add(disconnectAction);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(I18N.getResource("connections.title"));
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	private boolean isConnectionSelected() {
		return table.getSelectionIndex() > -1;
	}

	private Connection getSelectedConnection() {
		if (!isConnectionSelected())
			return null;
		TableItem i = table.getItem(table.getSelectionIndex());
		Connection c = (Connection) i.getData();
		return c;
	}

	private void loadConnections() {

		try {
			synchronized (tableViewer) {
				tableViewer.setInput(clientService.getConnectionService()
						.getConnections());
			}
			updateActions();
		} catch (RemoteException e) {
			log.error("Failed to load connections", e);
		}
	}

	/**
	 * A simple label provider
	 */
	private class ConnectionLabelProvider extends ColumnLabelProvider implements
			IStyledLabelProvider {

		public Image getImage(Object element) {
			Connection con = (Connection) element;
			try {
				return statusImages[clientService.getStatus(con)];
			} catch (RemoteException e) {
				return statusImages[ConnectionStatus.DISCONNECTED];
			}
		}

		public String getText(Object element) {
			return getStyledText(element).toString();
		}

		private String getStatusText(Connection con) {
			try {
				switch (clientService.getStatus(con)) {
				case ConnectionStatus.CONNECTED:
					return I18N.getResource("connected.text");
				case ConnectionStatus.CONNECTING:
					return I18N.getResource("connecting.text");
				default:
					return I18N.getResource("disconnected.text");
				}
			} catch (RemoteException e) {
				log.error("Failed to get connection status", e);
				return "Cannot determine status";
			}
		}

		public StyledString getStyledText(Object element) {
			Connection status = (Connection) element;
			StyledString styledString = new StyledString();
			styledString.append(status.getHostname());
			Styler styler = new Styler() {
				public void applyStyles(TextStyle textStyle) {
					Font font = new Font(null, new FontData("consolas", 9,
							SWT.BOLD));
					textStyle.font = font;
					textStyle.foreground = new Color(null, new RGB(0xdd, 0xdd,
							0xdd));
				}
			};
			styledString.append("\r\n" + getStatusText(status), styler);
			return styledString;
		}
	}

	private static class ClientListContentProvider implements
			ITreeContentProvider {

		List<Connection> connections;

		public Object getParent(Object element) {
			log.info("Get parent");
			if (element instanceof File) {
				File file = (File) element;
				return file.getParentFile();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			log.info("has Children");
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return connections.toArray(new Object[0]);
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			log.info("Input changed");
			this.connections = (List<Connection>) newInput;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}
	}
}
