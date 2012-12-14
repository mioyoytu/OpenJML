/*
 * This file is part of the OpenJML plugin project.
 * Copyright 2004-2013 David R. Cok
 */
package org.jmlspecs.openjml.eclipse;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

// FIXME - all strings need to be internationalized
// FIXME - review for other options

/**
 * This class creates a Preferences page in Eclipse
 * This page hold data needed for CodeSonar and Eclipse interaction
 * <P>
 * The preferences page manages various JML and OpenJML and plug-in specific
 * options. As usual these are stored in the preferences store, but OpenJML
 * uses the System properties to hold values of options, so we need also to 
 * store new changed values in the System properties. Also, we have the
 * unusual functionality to initialize the Eclipse preferences from properties.
 * <P>
 * The key used for preferences is the same as the key used in System properties.
 * <P>
 * Notes: Field editors are a convenient way to create a preferences page,
 * but not quite convenient enough on a couple of counts. 
 * <UL>
 * <LI> We need to observe when fields are changed. The normal way to do this
 * would be to register a listener, but only one listener is allowed, and 
 * the implementation of FieldEditorPreferencePage overwrites (during initialize())
 * any listener added when a field editor is created.
 * <LI> So we have to put the propertyChange call on the derived SettingsPage
 * itself, which means that it is the same listener for all fields.
 * <LI> There is insufficient access to FieldEditorPreferencePage functionality
 * such as the list of all fields or being able to force loads and stores.
 * </UL>
 */
public class SettingsPage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage {

	public SettingsPage() {
		super(FLAT);
		// No descriptive text needed. setDescription("Options for OpenJML");
	}
	
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }
    
    /** A mapping of option keys to field names */
    protected Map<String,FieldEditor> fieldMap = new HashMap<String,FieldEditor>();

    /** Overridden to add the field to the fieldMap */
    @Override
    public void addField(FieldEditor field) {
    	super.addField(field);
    	fieldMap.put(field.getPreferenceName(), field);
    }
    

    /** Cached copy of the verbosity editor so we can handle its change
     * events specially.
     */
    protected FieldEditor verbosity;

    /** Overriding performOk() callback in order to maintain copies
     * of the option values appropriately. This is called when
     * 'Apply' or 'OK' is clicked.
     */
	@Override
	public boolean performOk() {
		super.performOk();
		String value = getPreferenceStore().getString(Options.verbosityKey);
        Utils.verboseness = Integer.parseInt((String)value);
        return true;
	}

	/** The method that constructs all the editors and arranges them on the
	 * settings page.
	 */
    @Override
    protected void createFieldEditors() {
    	
    	// JML

    	MouseListener listener = new MouseAdapter() {
    		@Override
			public void mouseUp(MouseEvent e) {
				Properties properties = Utils.getProperties();
				for (Map.Entry<Object,Object> entry : properties.entrySet()) {
					Object keyobj = entry.getKey();
					if (!(keyobj instanceof String)) continue;
					String key = (String)keyobj;
					if (!(entry.getValue() instanceof String)) continue;
					String value = (String)entry.getValue();
					if (key.contains("openjml")) { // FIXME - no explicit string
						if (Utils.verboseness >= Utils.DEBUG) {
							Log.log("Reading property: " + key + " = " + value);
						}
						FieldEditor field = fieldMap.get(key);
						if (field != null) {
							if (field instanceof BooleanFieldEditor) {
								getPreferenceStore().setValue(key,!value.isEmpty());
							} else if (field instanceof StringFieldEditor) {
								getPreferenceStore().setValue(key,value);
							} else if (field instanceof ComboFieldEditor) {
								getPreferenceStore().setValue(key,value); // FIXME - how do we know it is a valid value
								if (field == verbosity) Utils.verboseness = Integer.parseInt(value);
							} else {
								Log.errorlog("Ignoring unknown field editor type " + field.getClass() + " for property " + key + "=" + value,null);
							}
						} else {
							// Assume anything else has a String value
							getPreferenceStore().setValue(key,value);
						}
					} else {
						// There are lots of these - mostly Java or Eclipse related
						//Log.log("Ignoring property " + key + "=" + value);
					}
				}
				initialize();
			}
		};
		
		addField(new ButtonFieldEditor(Options.updateKey,"",
				"Update from properties files",
				listener,
				getFieldEditorParent())
		);

		addField(new LabelFieldEditor("zzzzz.JML","",SWT.NONE,
				getFieldEditorParent()));
		addField(new LabelFieldEditor("zzzzz.JML","JML Options",SWT.SEPARATOR|SWT.HORIZONTAL,
				getFieldEditorParent()));

		// FIXME - i10n all the strings

        addField(new BooleanFieldEditor(Options.nonnullByDefaultKey, "NonNull By Default",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.checkPurityKey, "Skip Purity Check",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.showNotImplementedKey, "Warn about not-implemented constructs",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.showNotExecutableKey, "Warn about not-executable constructs",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.checkSpecsPathKey, "Check Specification Path",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.noInternalSpecsKey, "Use external system specs (add system specs to specspath on Paths Page)",
                getFieldEditorParent()));
        addField(new StringFieldEditor(Options.optionalKeysKey, "Optional Annotation Keys",
                getFieldEditorParent()));
        
        
        // RAC
        
		addField(new LabelFieldEditor("zzzzz.RAC","",SWT.NONE,
				getFieldEditorParent()));
		addField(new LabelFieldEditor("zzzzz.RAC","Options relating to RAC",SWT.SEPARATOR|SWT.HORIZONTAL,
				getFieldEditorParent()));

        addField(new BooleanFieldEditor(Options.enableRacKey, "Enable Runtime Assertion Checking",
                getFieldEditorParent()));
        addField(new DirectoryFieldEditor(Options.racbinKey, "Directory for RAC output",
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(Options.noInternalRuntimeKey, "Use an external runtime library (add the library to the project classpath)",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(Options.checkSpecsPathKey, "Add runtime library to project automatically",
                getFieldEditorParent()));

        // Debug and verbosity

		addField(new LabelFieldEditor("zzzzz.VERBOSE","",SWT.NONE,
				getFieldEditorParent()));
		addField(new LabelFieldEditor("zzzzz.VERBOSE","Verboseness and Debugging",SWT.SEPARATOR|SWT.HORIZONTAL,
				getFieldEditorParent()));

        addField(new BooleanFieldEditor(Options.javaverboseKey, "Java verbose",
                getFieldEditorParent()));
        
        addField(verbosity=new ComboFieldEditor(Options.verbosityKey, "Verbosity Level",
        		new String[][]{ 
        			{"quiet", Integer.toString(Utils.QUIET) }, 
        			{"normal", Integer.toString(Utils.NORMAL)}, 
        		    {"progress", Integer.toString(Utils.PROGRESS)}, 
        		    {"verbose", Integer.toString(Utils.VERBOSE)}, 
        		    {"debug", Integer.toString(Utils.DEBUG)}},
                getFieldEditorParent()));
    }
    

}