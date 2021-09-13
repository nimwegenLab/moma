package com.jug.gui;

import com.jug.MoMA;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author jug
 */
class DialogPropertiesEditor extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5529104109524798394L;
	private static final PropEditedListener propEditListener = new PropEditedListener();
    private static Component parent = null;
    private PropertySheetPanel sheet;

    static class PropEditedListener implements PropertyChangeListener {

		@Override
		public void propertyChange( final PropertyChangeEvent evt ) {
            Property sourceProperty = (Property) evt.getSource();
			final String sourceName = sourceProperty.getName();

			try {
                switch (sourceName) {
                    case "GUROBI_TIME_LIMIT":
                        MoMA.GUROBI_TIME_LIMIT =
                                Double.parseDouble(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "GUROBI_TIME_LIMIT",
                                "" + MoMA.GUROBI_TIME_LIMIT);
                        break;
                    case "GUROBI_MAX_OPTIMALITY_GAP":
                        MoMA.GUROBI_MAX_OPTIMALITY_GAP =
                                Double.parseDouble(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "GUROBI_MAX_OPTIMALITY_GAP",
                                "" + MoMA.GUROBI_MAX_OPTIMALITY_GAP);
                        break;
                    case "SEGMENTATION_MODEL_PATH": {
                        String newPath = sourceProperty.getValue().toString();
                        if(newPath!=MoMA.SEGMENTATION_MODEL_PATH) {
                            File f = new File(newPath);
                            if(!f.exists() || f.isDirectory()) {
                                JOptionPane.showMessageDialog(
                                        parent,
                                        "Specified file does not exist. Falling back to previous path.",
                                        "Model file not found.",
                                        JOptionPane.ERROR_MESSAGE);
                                sourceProperty.setValue(MoMA.SEGMENTATION_MODEL_PATH);
                                break;
                            }

                            final int choice =
                                    JOptionPane.showConfirmDialog(
                                            parent,
                                            "Changing this value will rerun segmentation and optimization.\nYou will loose all manual edits performed so far!",
                                            "Continue?",
                                            JOptionPane.YES_NO_OPTION);

                            if (choice != JOptionPane.OK_OPTION) {
                                sourceProperty.setValue(MoMA.SEGMENTATION_MODEL_PATH); /* User aborted. Reset value to previous setting. */
                            } else {
                                MoMA.SEGMENTATION_MODEL_PATH = newPath;
                                MoMA.props.setProperty(
                                        "SEGMENTATION_MODEL_PATH",
                                        "" + MoMA.SEGMENTATION_MODEL_PATH);
                                    final Thread t = new Thread(() -> {
                                        ((MoMAGui) parent).restartFromGLSegmentation();
                                        ((MoMAGui) parent).restartTracking();
                                    });
                                    t.start();
                            }
                        }
                        break;
                    }
                    case "GL_OFFSET_TOP": {
                        int newValue = Integer.parseInt(evt.getNewValue().toString());
                        showPropertyEditedNeedsRerunDialog("Continue?",
                                "Changing this value will restart the optimization.\nYou will loose all manual edits performed so far!",
                                () -> newValue != MoMA.GL_OFFSET_TOP,
                                () -> sourceProperty.setValue(MoMA.GL_OFFSET_TOP),
                                () -> {
                                    MoMA.GL_OFFSET_TOP = newValue;
                                    MoMA.props.setProperty(
                                            "GL_OFFSET_TOP",
                                            "" + MoMA.GL_OFFSET_TOP);
                                    ((MoMAGui) parent).restartTrackingAsync();
                                });
                        break;
                    }
                    case "INTENSITY_FIT_ITERATIONS": {
                        MoMA.INTENSITY_FIT_ITERATIONS =
                                Integer.parseInt(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "INTENSITY_FIT_ITERATIONS",
                                "" + MoMA.INTENSITY_FIT_ITERATIONS);
                        break;
                    }
                    case "INTENSITY_FIT_PRECISION": {
                        MoMA.INTENSITY_FIT_PRECISION =
                                Double.parseDouble(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "INTENSITY_FIT_PRECISION",
                                "" + MoMA.INTENSITY_FIT_PRECISION);
                        break;
                    }
                    case "INTENSITY_FIT_INITIAL_WIDTH": {
                        MoMA.INTENSITY_FIT_INITIAL_WIDTH =
                                Double.parseDouble(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "INTENSITY_FIT_INITIAL_WIDTH",
                                "" + MoMA.INTENSITY_FIT_INITIAL_WIDTH);
                        break;
                    }
                    case "INTENSITY_FIT_RANGE_IN_PIXELS": {
                        MoMA.INTENSITY_FIT_RANGE_IN_PIXELS =
                                Integer.parseInt(evt.getNewValue().toString());
                        MoMA.props.setProperty(
                                "INTENSITY_FIT_RANGE_IN_PIXELS",
                                "" + MoMA.INTENSITY_FIT_RANGE_IN_PIXELS);
                        break;
                    }
                    default:
                        JOptionPane.showMessageDialog(
                                MoMA.getGui(),
                                "Value not changed - NOT YET IMPLEMENTED!",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        break;
                }
			} catch ( final NumberFormatException e ) {
				JOptionPane.showMessageDialog(
						MoMA.getGui(),
						"Illegal value entered -- value not changed!",
						"Error",
						JOptionPane.ERROR_MESSAGE );
			}
		}

	}

    private static void showPropertyEditedNeedsRerunDialog(String title, String message, Supplier<Boolean> condition, Runnable rejectionCallback, Runnable acceptCallback) {
        if (condition.get()) {
            final int choice =
                    JOptionPane.showConfirmDialog(
                            parent,
                            message,
                            title,
                            JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.OK_OPTION) {
                rejectionCallback.run();
            } else {
                acceptCallback.run();
            }
        }
    }

    private static class PropFactory {

        static Property buildFor(final String key, final Object value) {
			final DefaultProperty property = new DefaultProperty();
			property.setDisplayName( key );
			property.setName( key );
			property.setValue( value.toString() );
			property.setType( String.class );
			property.addPropertyChangeListener( propEditListener );

            String GRB = "GUROBI Properties";
            String SEG = "Segmentation Properties";
            String TRA = "Tracking Properties";
            String GL = "GrowthLine Properties";
            String BGREM = "Background Properties";
            String EXPORT = "Export Properties";

            switch (key) {
                case "BGREM_TEMPLATE_XMIN":
                case "BGREM_X_OFFSET":
                case "BGREM_TEMPLATE_XMAX":
                    property.setCategory(BGREM);
                    property.setShortDescription(key);
                    break;
                case "GL_WIDTH_IN_PIXELS":
                case "MIN_GAP_CONTRAST":
                case "MOTHER_CELL_BOTTOM_TRICK_MAX_PIXELS":
                case "GL_OFFSET_LATERAL":
                case "GL_OFFSET_TOP":
                case "MIN_CELL_LENGTH":
                    property.setCategory(TRA);
                    property.setShortDescription(key);
                    break;
                case "SIGMA_PRE_SEGMENTATION_X":
                case "SIGMA_GL_DETECTION_Y":
                case "SIGMA_GL_DETECTION_X":
                case "SIGMA_PRE_SEGMENTATION_Y":
                    property.setCategory(SEG);
                    property.setShortDescription(key);
                    break;
                case "SEGMENTATION_MIX_CT_INTO_PMFRF":
                case "SEGMENTATION_MODEL_PATH":
                    property.setCategory(SEG);
                    property.setShortDescription(key);
                    property.setEditable(true);
                    break;
                case "DEFAULT_PATH":
                    property.setShortDescription(key);
                    break;
                case "GUROBI_TIME_LIMIT":
                case "GUROBI_MAX_OPTIMALITY_GAP":
                    property.setCategory(GRB);
                    property.setShortDescription(key);
                    break;
                case "INTENSITY_FIT_ITERATIONS":
                case "INTENSITY_FIT_PRECISION":
                case "INTENSITY_FIT_INITIAL_WIDTH":
                case "INTENSITY_FIT_RANGE_IN_PIXELS":
                    property.setCategory(EXPORT);
                    property.setShortDescription(key);
                    break;
                default:
                    // ALL OTHERS ARE ADDED HERE
                    property.setShortDescription(key);
                    property.setEditable(false);
                    break;
            }
			return property;
		}
	}


	private JButton bClose;
	private final Properties props;

	public DialogPropertiesEditor( final Component parent, final Properties props ) {
		super( SwingUtilities.windowForComponent( parent ), "MoMA Properties Editor" );
		this.parent = parent;
		this.dialogInit();
		this.setModal( true );

		final int width = 800;
		final int height = 400;

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int screenWidth = ( int ) screenSize.getWidth();
		final int screenHeight = ( int ) screenSize.getHeight();
		this.setBounds( ( screenWidth - width ) / 2, ( screenHeight - height ) / 2, width, height );

		this.props = props;

		buildGui();
		setKeySetup();
	}

	private void buildGui() {
		this.rootPane.setLayout( new BorderLayout() );

		sheet = new PropertySheetPanel();
		sheet.setMode( PropertySheet.VIEW_AS_CATEGORIES );
		sheet.setDescriptionVisible( false );
		sheet.setSortingCategories( false );
		sheet.setSortingProperties( false );
		sheet.setRestoreToggleStates( false );
		for ( final String key : this.props.stringPropertyNames() ) {
			sheet.addProperty( PropFactory.buildFor( key, props.getProperty( key ) ) );
		}
//		sheet.setEditorFactory( PropertyEditorRegistry.Instance );

		bClose = new JButton( "Close" );
		bClose.addActionListener( this );
		this.rootPane.setDefaultButton( bClose );
		final JPanel horizontalHelper = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
		horizontalHelper.add( bClose );

		this.rootPane.add( sheet, BorderLayout.CENTER );
		this.rootPane.add( horizontalHelper, BorderLayout.SOUTH );
	}

	private void setKeySetup() {
		this.rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( "ESCAPE" ), "closeAction" );

		this.rootPane.getActionMap().put( "closeAction", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e ) {
				setVisible( false );
				dispose();
			}
		} );

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {
		if ( e.getSource().equals( bClose ) ) {
			this.setVisible( false );
			this.dispose();
		}
	}
}
