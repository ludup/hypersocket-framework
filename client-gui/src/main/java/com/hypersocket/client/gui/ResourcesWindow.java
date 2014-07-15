package com.hypersocket.client.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class ResourcesWindow extends ApplicationWindow {

	/**
	 * Create the application window.
	 */
	public ResourcesWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		TreeViewer tv = new TreeViewer(container, SWT.BORDER);
		Tree tree = tv.getTree();
		tree.setBounds(0, 0, 173, 220);

		  //  tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		    tv.setContentProvider(new FileTreeContentProvider());
		    tv.setLabelProvider(new FileTreeLabelProvider());
		    tv.setInput("."); // pass a non-null that will be ignored
		
		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			ResourcesWindow window = new ResourcesWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("New Application");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
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
	  public Object[] getChildren(Object arg0) {
	    // Return the files and subdirectories in this directory
	    return ((File) arg0).listFiles();
	  }

	  /**
	   * Gets the parent of the specified object
	   * 
	   * @param arg0
	   *            the object
	   * @return Object
	   */
	  public Object getParent(Object arg0) {
	    // Return this file's parent file
	    return ((File) arg0).getParentFile();
	  }

	  /**
	   * Returns whether the passed object has children
	   * 
	   * @param arg0
	   *            the parent object
	   * @return boolean
	   */
	  public boolean hasChildren(Object arg0) {
	    // Get the children
	    Object[] obj = getChildren(arg0);

	    // Return whether the parent has children
	    return obj == null ? false : obj.length > 0;
	  }

	  /**
	   * Gets the root element(s) of the tree
	   * 
	   * @param arg0
	   *            the input data
	   * @return Object[]
	   */
	  public Object[] getElements(Object arg0) {
	    // These are the root elements of the tree
	    // We don't care what arg0 is, because we just want all
	    // the root nodes in the file system
	    return File.listRoots();
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
	  private List listeners;

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
	    listeners = new ArrayList();

	    // Create the images
	    try {
	      file = new Image(null, new FileInputStream("images/file.gif"));
	      dir = new Image(null, new FileInputStream("images/directory.gif"));
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
	    LabelProviderChangedEvent event = new LabelProviderChangedEvent(this);
	    for (int i = 0, n = listeners.size(); i < n; i++) {
	      ILabelProviderListener ilpl = (ILabelProviderListener) listeners
	          .get(i);
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
	  public Image getImage(Object arg0) {
	    // If the node represents a directory, return the directory image.
	    // Otherwise, return the file image.
	    return ((File) arg0).isDirectory() ? dir : file;
	  }

	  /**
	   * Gets the text to display for a node in the tree
	   * 
	   * @param arg0
	   *            the node
	   * @return String
	   */
	  public String getText(Object arg0) {
	    // Get the name of the file
	    String text = ((File) arg0).getName();

	    // If name is blank, get the path
	    if (text.length() == 0) {
	      text = ((File) arg0).getPath();
	    }

	    // Check the case settings before returning the text
	    return preserveCase ? text : text.toUpperCase();
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
}
