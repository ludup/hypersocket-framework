package com.hypersocket.client.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.hypersocket.client.i18n.I18N;
import com.hypersocket.client.rmi.Launchable;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceProtocol;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceService;

/**
 * This class demonstrates TreeViewer. It shows the drives, directories, and
 * files on the system.
 */
public class ResourcesTree extends AbstractWindow {

	SWTGui gui;
	ResourceService resourceService;
	TreeViewer tv;
	
	/**
	 * FileTree constructor
	 */
	public ResourcesTree() {
		super(null);
	}

	/**
	 * FileTree constructor
	 */
	public ResourcesTree(SWTGui gui, ResourceService resourceService) {
		super(new Shell(gui.getShell()));
		this.gui = gui;
		this.resourceService = resourceService;
	}

	/**
	 * Runs the application
	 */
	public void run() {
		// Don't return from open() until window closes
		setBlockOnOpen(true);

		// Open the main window
		open();

		// Dispose the display
		Display.getCurrent().dispose();
	}

	/**
	 * Configures the shell
	 * 
	 * @param shell
	 *            the shell
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		// Set the title bar text and the size
		shell.setText(I18N.getResource("resources.text"));
		shell.setSize(getWindowWidth(400), getWindowHeight(400));
	}

	
	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Create the tree viewer to display the file tree
		tv = new TreeViewer(composite);
		tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.setContentProvider(new FileTreeContentProvider());
		tv.setLabelProvider(new FileTreeLabelProvider());
		tv.setInput("root"); // pass a non-null that will be ignored

		final Menu menu = new Menu(tv.getTree());
		
		final MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(I18N.getResource("launch.text"));
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent change) {
				TreeItem[] selected = tv.getTree().getSelection();
				if(selected!=null && selected.length > 0) {
					if(selected[0].getData() instanceof Launchable) {
						item.setEnabled(((Launchable)selected[0].getData()).isLaunchable());
					} else {
						item.setEnabled(false);
					}
				}
			}
		});
		item.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	TreeItem[] selected = tv.getTree().getSelection();
				if(selected!=null && selected.length > 0) {
					if(selected[0].getData() instanceof Launchable) {
						Launchable launchable = (Launchable)selected[0].getData();
						launchable.getResourceLauncher().launch();
					}
				}
		      }
		});
	    
		tv.getTree().setMenu(menu);
	    

		
		return composite;
	}

	/**
	 * The application entry point
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new ResourcesTree().run();
	}

	/**
	 * This class provides the content for the tree in FileTree
	 */

	class FileTreeContentProvider implements ITreeContentProvider {
		/**
		 * Gets the children of the specified object
		 * 
		 * @param arg0
		 *            the parent object
		 * @return Object[]
		 */
		public Object[] getChildren(Object resource) {
			if(resource instanceof ResourceRealm) {
				return ((ResourceRealm) resource).getResources().toArray();
			} else if(resource instanceof Resource) {
				return ((Resource) resource).getProtocols().toArray();
			} else {
				return new Object[] { };
			}
		}

		/**
		 * Gets the parent of the specified object
		 * 
		 * @param arg0
		 *            the object
		 * @return Object
		 */
		public Object getParent(Object resource) {
			if(resource instanceof ResourceRealm) {
				return null;
			} else if(resource instanceof Resource) {
				return ((Resource) resource).getRealm();
			} else {
				return ((ResourceProtocol) resource).getResource();
			}
		}

		/**
		 * Returns whether the passed object has children
		 * 
		 * @param arg0
		 *            the parent object
		 * @return boolean
		 */
		public boolean hasChildren(Object resource) {
			if (resource instanceof Resource) {
				return ((Resource) resource).getProtocols().size() > 0;
			} else if(resource instanceof ResourceRealm) {
				return ((ResourceRealm) resource).getResources().size() > 0;
			} else {
				return false;
			}
		}

		/**
		 * Gets the root element(s) of the tree
		 * 
		 * @param arg0
		 *            the input data
		 * @return Object[]
		 */
		public Object[] getElements(Object arg0) {
			try {
				return ResourcesTree.this.resourceService.getResourceRealms().toArray();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * Disposes any created resources
		 */
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Called when the input changes
		 * 
		 * @param arg0
		 *            the viewer
		 * @param arg1
		 *            the old input
		 * @param arg2
		 *            the new input
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			// Nothing to change
		}
	}

	/**
	 * This class provides the labels for the file tree
	 */

	class FileTreeLabelProvider implements ILabelProvider {
		// The listeners
		private List<ILabelProviderListener> listeners;

		// Images for tree nodes
		private Image file;

		private Image dir;

		// Label provider state: preserve case of file names/directories
		boolean preserveCase;

		/**
		 * Constructs a FileTreeLabelProvider
		 */
		public FileTreeLabelProvider() {
			// Create the list to hold the listeners
			listeners = new ArrayList<ILabelProviderListener>();

			// Create the images
			try {
				file = new Image(null, new FileInputStream("images/file.gif"));
				dir = new Image(null, new FileInputStream(
						"images/directory.gif"));
			} catch (FileNotFoundException e) {
				// Swallow it; we'll do without images
			}
		}

		/**
		 * Sets the preserve case attribute
		 * 
		 * @param preserveCase
		 *            the preserve case attribute
		 */
		public void setPreserveCase(boolean preserveCase) {
			this.preserveCase = preserveCase;

			// Since this attribute affects how the labels are computed,
			// notify all the listeners of the change.
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(
					this);
			for (int i = 0, n = listeners.size(); i < n; i++) {
				ILabelProviderListener ilpl = listeners.get(i);
				ilpl.labelProviderChanged(event);
			}
		}

		/**
		 * Gets the image to display for a node in the tree
		 * 
		 * @param arg0
		 *            the node
		 * @return Image
		 */
		public Image getImage(Object resource) {
			if(resource instanceof ResourceProtocol) {
				return file;
			} else {
				return dir;
			}
		}

		/**
		 * Gets the text to display for a node in the tree
		 * 
		 * @param arg0
		 *            the node
		 * @return String
		 */
		public String getText(Object resource) {
			if(resource instanceof ResourceProtocol) {
				return ((ResourceProtocol)resource).getProtocol();
			} else if(resource instanceof Resource) {
				return ((Resource)resource).getHostname();
			} else {
				return ((ResourceRealm) resource).getName();
			}
		}

		/**
		 * Adds a listener to this label provider
		 * 
		 * @param arg0
		 *            the listener
		 */
		public void addListener(ILabelProviderListener arg0) {
			listeners.add(arg0);
		}

		/**
		 * Called when this LabelProvider is being disposed
		 */
		public void dispose() {
			// Dispose the images
			if (dir != null)
				dir.dispose();
			if (file != null)
				file.dispose();
		}

		/**
		 * Returns whether changes to the specified property on the specified
		 * element would affect the label for the element
		 * 
		 * @param arg0
		 *            the element
		 * @param arg1
		 *            the property
		 * @return boolean
		 */
		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		/**
		 * Removes the listener
		 * 
		 * @param arg0
		 *            the listener to remove
		 */
		public void removeListener(ILabelProviderListener arg0) {
			listeners.remove(arg0);
		}
	}

	public void refresh() {
		tv.refresh();
	}
}