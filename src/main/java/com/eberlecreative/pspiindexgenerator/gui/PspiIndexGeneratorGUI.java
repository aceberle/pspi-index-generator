package com.eberlecreative.pspiindexgenerator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.lang3.StringUtils;

import com.eberlecreative.pspiindexgenerator.imagemodifier.CropAnchors;
import com.eberlecreative.pspiindexgenerator.pspi.generator.PspiIndexGenerator;
import com.eberlecreative.pspiindexgenerator.pspi.util.PspiImageSize;

import net.miginfocom.swing.MigLayout;

public class PspiIndexGeneratorGUI extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -7845485644403871508L;

    private static final String PREF_FORCE_OUTPUT = "forceOutput";
    private static final String PREF_COMPRESSION_QUALITY = "compressionQuality";
    private static final String RESIZE_NO_RESIZE = "No Resize";
    private static final String CROP_NO_CROP = "No Crop";
    private static final String RESIZE_LARGE = "Large";
    private static final String PREF_CROP_ANCHOR = "cropAnchor";
    private static final String PREF_RESIZE = "resize";
    private static final String PREF_IMAGE_FOLDER_PATTERN = "imageFolderPattern";
    private static final String PREF_IMAGE_FILE_PATTERN = "imageFilePattern";
    private static final String PREF_STRICT = "strict";
    private static final String PREF_LAST_INPUT_DIR = "lastInputDir";
    private static final String PREF_LAST_OUTPUT_DIR = "lastOutputDir";
    private static final String PREF_DATA_SOURCE = "dataSource";
    private static final String DATA_SOURCE_USE_FILE_NAMES = "Use File Names";
    private static final String PREF_LAST_DATA_FILE_PATH = "lastDataFilePath";
    private static final String PREF_OUTPUT_IMAGE_NAME_PATTERN = "outputImageNamePattern";
    private static final String PREF_DATA_FILE_PATH = "dataFilePath";
    private static final String PREF_OVERRIDE_IMAGE_NAMES = "overrideImageNames";
    private static final int RADIO_WIDTH = 80;
    private JTextField inputDirText;
    private JTextField outputDirText;
    private JTextField imageFolderPatternText;
    private JTextField imageFilePatternText;
    private JCheckBox strictCheckBox;
    private Map<String, List<JRadioButton>> radioButtonGroupsByName = new HashMap<>();
    private SpinnerNumberModel compressionQualityModel;
    private JSpinner compressionQualitySpinner;
    private JCheckBox forceOutputCheckbox;
    private JTextField dataFilePathText;
    private JTextField outputImageNamePatternText;
    private JCheckBox overrideImageNamesCheckBox;
    private JButton dataFileBrowseButton;

    public PspiIndexGeneratorGUI(Preferences preferences) {
        super("PSPI Index Generator");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        JLabel inputDirLabel = new JLabel("Input Directory:");

        inputDirText = new JTextField();
        inputDirLabel.setLabelFor(inputDirText);
        inputDirText.setColumns(10);
        inputDirText.setText(preferences.get(PREF_LAST_INPUT_DIR, ""));

        JButton inputDirBrowseButton = new JButton("Browse");
        inputDirBrowseButton.addActionListener(chooseDirectory(() -> inputDirText.getText(), path -> {
            final String pathString = path.getAbsolutePath();
            inputDirText.setText(pathString);
            preferences.put(PREF_LAST_INPUT_DIR, pathString);
            flush(preferences);
            if (outputDirText.getText().trim().length() == 0) {
                outputDirText.setText(pathString + "_generated");
            }
        }));

        JLabel outputDirLabel = new JLabel("Output Directory:");

        outputDirText = new JTextField();
        outputDirLabel.setLabelFor(outputDirText);
        outputDirText.setColumns(10);
        outputDirText.setText(preferences.get(PREF_LAST_OUTPUT_DIR, ""));

        JButton outputDirBrowseButton = new JButton("Browse");
        outputDirBrowseButton.addActionListener(chooseDirectory(() -> outputDirText.getText(), path -> {
            final String pathString = path.getAbsolutePath();
            preferences.put(PREF_LAST_OUTPUT_DIR, pathString);
            outputDirText.setText(pathString);
        }));
        
        getContentPane().setLayout(new MigLayout("", "[85px][312px,grow][69px]", "[][][][][][][][][][][][][][][][][][]"));
        getContentPane().add(inputDirLabel, "cell 0 0,alignx right,aligny center");
        getContentPane().add(inputDirText, "cell 1 0,growx,aligny center");
        getContentPane().add(inputDirBrowseButton, "cell 2 0,alignx left,aligny top");
        getContentPane().add(outputDirLabel, "cell 0 1,alignx right,aligny center");
        getContentPane().add(outputDirText, "cell 1 1,growx,aligny center");
        getContentPane().add(outputDirBrowseButton, "cell 2 1,alignx left,aligny top");
        
        JLabel strictLabel = new JLabel("Strict:");
        getContentPane().add(strictLabel, "cell 0 2,alignx right");
        
        strictCheckBox = new JCheckBox("");
        strictLabel.setLabelFor(strictCheckBox);
        getContentPane().add(strictCheckBox, "cell 1 2");
        
        JLabel forceOutputLabel = new JLabel("Force output:");
        getContentPane().add(forceOutputLabel, "cell 0 3,alignx right");
        
        forceOutputCheckbox = new JCheckBox("");
        forceOutputLabel.setLabelFor(forceOutputCheckbox);
        getContentPane().add(forceOutputCheckbox, "cell 1 3");
        
        JLabel resizeLabel = new JLabel("Resize Images:");
        getContentPane().add(resizeLabel, "cell 0 4,alignx right");
        
        JRadioButton resizeToLargeImagesRadio = new JRadioButton(appendDimensions(RESIZE_LARGE, PspiImageSize.LARGE));
        getContentPane().add(resizeToLargeImagesRadio, "flowx,cell 1 4");
        
        JRadioButton resizeToSmallImageRadio = new JRadioButton(appendDimensions("Small", PspiImageSize.SMALL));
        getContentPane().add(resizeToSmallImageRadio, "cell 1 4");
        
        JRadioButton noResizeImageRadio = new JRadioButton(RESIZE_NO_RESIZE);
        getContentPane().add(noResizeImageRadio, "cell 1 4");
        
        addRadioButtonGroup(PREF_RESIZE, resizeToLargeImagesRadio, resizeToSmallImageRadio, noResizeImageRadio);
        addListenerToRadioButtonGroup(PREF_RESIZE, radio -> {
            if(RESIZE_NO_RESIZE.equalsIgnoreCase(radio.getText())) {
                disableRadioGroup(PREF_CROP_ANCHOR);
                compressionQualitySpinner.setEnabled(false);
            } else {
                enableRadioGroup(PREF_CROP_ANCHOR);
                compressionQualitySpinner.setEnabled(true);
            }
        });
        
        JLabel cropImagesLabel = new JLabel("Crop Anchor:");
        getContentPane().add(cropImagesLabel, "cell 0 5,alignx right");
        
        JRadioButton topLeftCropRadio = new JRadioButton("Top-Left");
        getContentPane().add(topLeftCropRadio, "flowx,cell 1 5,alignx center");
        fixRadioLabel(topLeftCropRadio);
        
        JRadioButton topMiddleCropRadio = new JRadioButton("Top-Middle");
        getContentPane().add(topMiddleCropRadio, "cell 1 5,alignx center");
        fixRadioLabel(topMiddleCropRadio);
        
        JRadioButton topRightCropRadio = new JRadioButton("Top-Right");
        getContentPane().add(topRightCropRadio, "cell 1 5,alignx center");
        fixRadioLabel(topRightCropRadio);
        
        JRadioButton centerLeftCropRadio = new JRadioButton("Center-Left");
        getContentPane().add(centerLeftCropRadio, "flowx,cell 1 6,alignx center");
        fixRadioLabel(centerLeftCropRadio);
        
        JRadioButton centerMiddleCropRadio = new JRadioButton("Center-Middle");
        getContentPane().add(centerMiddleCropRadio, "cell 1 6");
        fixRadioLabel(centerMiddleCropRadio);
        
        JRadioButton centerRightCropRadio = new JRadioButton("Center-Right");
        getContentPane().add(centerRightCropRadio, "cell 1 6");
        fixRadioLabel(centerRightCropRadio);
        
        JRadioButton bottomLeftCropRadio = new JRadioButton("Bottom-Left");
        getContentPane().add(bottomLeftCropRadio, "flowx,cell 1 7,alignx center");
        fixRadioLabel(bottomLeftCropRadio);
        
        JRadioButton bottomMiddleCropRadio = new JRadioButton("Bottom-Middle");
        getContentPane().add(bottomMiddleCropRadio, "cell 1 7,alignx center");
        fixRadioLabel(bottomMiddleCropRadio);
        
        JRadioButton bottomRightCropRadio = new JRadioButton("Bottom-Right");
        getContentPane().add(bottomRightCropRadio, "cell 1 7,alignx center");
        fixRadioLabel(bottomRightCropRadio);
        
        addRadioButtonGroup(PREF_CROP_ANCHOR, topLeftCropRadio, topMiddleCropRadio, topRightCropRadio, centerLeftCropRadio, centerMiddleCropRadio, centerRightCropRadio, bottomLeftCropRadio, bottomMiddleCropRadio, bottomRightCropRadio);
        
        JLabel compressionQualityLabel = new JLabel("Compression Quality:");
        getContentPane().add(compressionQualityLabel, "cell 0 8");
        
        compressionQualitySpinner = new JSpinner();
        compressionQualityModel = new SpinnerNumberModel(90, 50, 100, 1);
        compressionQualitySpinner.setModel(compressionQualityModel);
        getContentPane().add(compressionQualitySpinner, "cell 1 8");
        
        JRadioButton useFileNamesRadioButton = new JRadioButton(DATA_SOURCE_USE_FILE_NAMES);
        getContentPane().add(useFileNamesRadioButton, "cell 0 9");
        
        JLabel imageFolderPatternLabel = new JLabel("Image Folder Pattern:");
        getContentPane().add(imageFolderPatternLabel, "cell 0 10,alignx trailing");
        
        imageFolderPatternText = new JTextField();
        imageFolderPatternLabel.setLabelFor(imageFolderPatternText);
        getContentPane().add(imageFolderPatternText, "cell 1 10,growx");
        imageFolderPatternText.setColumns(10);
        
        JLabel imageFilePatternLabel = new JLabel("Image File Pattern:");
        getContentPane().add(imageFilePatternLabel, "cell 0 11,alignx trailing");
        
        imageFilePatternText = new JTextField();
        imageFilePatternLabel.setLabelFor(imageFilePatternText);
        getContentPane().add(imageFilePatternText, "cell 1 11,growx");
        imageFilePatternText.setColumns(10);
        
        JRadioButton useDataFileRadioButton = new JRadioButton("Use Data File");
        getContentPane().add(useDataFileRadioButton, "cell 0 12");
        
        addRadioButtonGroup(PREF_DATA_SOURCE, useFileNamesRadioButton, useDataFileRadioButton);
        addListenerToRadioButtonGroup(PREF_DATA_SOURCE, radio -> {
            final String selectedDataSource = radio.getText();
            preferences.put(PREF_DATA_SOURCE, selectedDataSource);
            if(DATA_SOURCE_USE_FILE_NAMES.equalsIgnoreCase(selectedDataSource)) {
                enable(imageFolderPatternText, imageFilePatternText);
                disable(dataFilePathText, dataFileBrowseButton);
            } else {
                disable(imageFolderPatternText, imageFilePatternText);
                enable(dataFilePathText, dataFileBrowseButton);
            }
        });
        
        dataFilePathText = new JTextField();
        dataFilePathText.setColumns(10);
        getContentPane().add(dataFilePathText, "cell 1 13,growx");
        
        dataFileBrowseButton = new JButton("Browse");
        getContentPane().add(dataFileBrowseButton, "cell 2 13");
        dataFileBrowseButton.addActionListener(chooseExcelFile(() -> dataFilePathText.getText(), path -> {
            final String pathString = path.getAbsolutePath();
            preferences.put(PREF_LAST_DATA_FILE_PATH, pathString);
            dataFilePathText.setText(pathString);
        }));
        
        JLabel overrideImageNamesLabel = new JLabel("Override Output Image Names:");
        getContentPane().add(overrideImageNamesLabel, "cell 0 14");
        
        overrideImageNamesCheckBox = new JCheckBox("");
        overrideImageNamesLabel.setLabelFor(overrideImageNamesCheckBox);
        getContentPane().add(overrideImageNamesCheckBox, "cell 1 14");
        overrideImageNamesCheckBox.addChangeListener(event -> {
            if(overrideImageNamesCheckBox.isSelected()) {
                enable(outputImageNamePatternText);
            } else {
                disable(outputImageNamePatternText);
            }
        });
        
        JLabel imageNamePatternLabel = new JLabel("Output Image Name Pattern:");
        getContentPane().add(imageNamePatternLabel, "cell 0 15,alignx trailing");
        
        outputImageNamePatternText = new JTextField();
        getContentPane().add(outputImageNamePatternText, "cell 1 15,growx");
        outputImageNamePatternText.setColumns(10);
        
        JButton savePreferencesButton = new JButton("Save Settings");
        getContentPane().add(savePreferencesButton, "flowx,cell 1 16 2 1,alignx right");
        savePreferencesButton.addActionListener(e -> {
            savePreferences(preferences);
        });
        
        JButton resetPreferencesButton = new JButton("Reload Settings");
        getContentPane().add(resetPreferencesButton, "cell 1 16 2 1,alignx right");
        resetPreferencesButton.addActionListener(e -> {
            resetPreferences(preferences);
        });
        
        JButton clearSettingsButton = new JButton("Clear Settings");
        getContentPane().add(clearSettingsButton, "cell 1 16 2 1,alignx right");
        clearSettingsButton.addActionListener(e -> {
            clearPreferences(preferences);
            resetPreferences(preferences);
        });
        
        
        final JDialog consoleDialog = new JDialog(this, "Progress...");
        consoleDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        final JPanel consolePanel = new JPanel(new BorderLayout());
        final JTextPane consoleTextPane = new JTextPane();
        final JScrollPane consoleScrollPane = new JScrollPane(consoleTextPane);
        consolePanel.add(consoleScrollPane, BorderLayout.CENTER);
        consoleDialog.getContentPane().add(consolePanel);
        
        final JButton generateButton = new JButton("Generate PSPI Index");
        getContentPane().add(generateButton, "cell 1 17 2 1,alignx right");
        final JFrame instance = this;
        generateButton.addActionListener(e -> {
            consoleDialog.setLocationRelativeTo(instance);
            consoleDialog.setLocation(200, 665);
            consoleDialog.setSize(750, 300);
            consoleTextPane.setText("");
            final PrintStream err = new PrintStream(new StringConsumingOutputStream(string -> {
                appendToPane(consoleTextPane, string, Color.RED);
            }));
            final PrintStream out = new PrintStream(new StringConsumingOutputStream(string -> {
                appendToPane(consoleTextPane, string, Color.BLACK);
            }));
            final PspiIndexGenerator.Builder builder = new PspiIndexGenerator.Builder()
                    .verboseLogging(true, out, err)
                    .strict(strictCheckBox.isSelected())
                    .forceOutput(forceOutputCheckbox.isSelected())
                    .imageFolderPattern(imageFolderPatternText.getText())
                    .imageFilePattern(imageFilePatternText.getText());
            final String selectedCrop = getSelectedRadioFromGroup(PREF_CROP_ANCHOR);
            if(!CROP_NO_CROP.equals(selectedCrop)) {
                builder.cropImages(CropAnchors.parseCropAnchor(selectedCrop));
            }
            final String selectedResize = getSelectedRadioFromGroup(PREF_RESIZE);
            if(!RESIZE_NO_RESIZE.equals(selectedResize)) {
                builder.resizeImages(PspiImageSize.fromString(selectedResize));
            }
            builder.compressionQuality(compressionQualityModel.getNumber().floatValue() / 100);
            final String selectedDataSource = getSelectedRadioFromGroup(PREF_DATA_SOURCE);
            if(!DATA_SOURCE_USE_FILE_NAMES.equals(selectedDataSource)) {
                final String dataFilePath = dataFilePathText.getText();
                preferences.put(PREF_DATA_FILE_PATH, dataFilePath);
                builder.dataFile(new File(dataFilePath));
            }
            if(overrideImageNamesCheckBox.isSelected()) {
                final String text = outputImageNamePatternText.getText();
                if(StringUtils.isBlank(text)) {
                    JOptionPane.showMessageDialog(this, "Expected Output Image Name Pattern to be provided!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                builder.outputFilePattern(text);
            }
            final PspiIndexGenerator generator = builder.build();
            final String inputDirPath = inputDirText.getText();
            preferences.put(PREF_LAST_INPUT_DIR, inputDirPath);
            final File inputDirectory = new File(inputDirPath);
            final String outputDirPath = outputDirText.getText();
            preferences.put(PREF_LAST_OUTPUT_DIR, outputDirPath);
            final File outputDirectory = new File(outputDirPath);
            generateButton.setEnabled(false);
            consoleDialog.setVisible(true);
            final ExecutorService pool = Executors.newCachedThreadPool();
            pool.execute(() -> {
                try {
                    generator.generate(inputDirectory, outputDirectory);
                    JOptionPane.showMessageDialog(this, "Generation is complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace(err);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    generateButton.setEnabled(true);
                }
            });
        });
        
        resetPreferences(preferences);
        
        pack();
    }
    
    private static void enable(JComponent...comps) {
        for(JComponent comp : comps) {
            comp.setEnabled(true);
        }
    }
    
    private static void disable(JComponent...comps) {
        for(JComponent comp : comps) {
            comp.setEnabled(false);
        }
    }

    private String appendDimensions(String orig, PspiImageSize imageSize) {
        return String.format("%s (%sx%s)", orig, imageSize.getWidth(), imageSize.getHeight());
    }

    private void clearPreferences(Preferences preferences) {
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StyleContext sc = StyleContext.getDefaultStyleContext();
                AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
                aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
                aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
                int len = tp.getDocument().getLength();
                tp.setCaretPosition(len);
                tp.setCharacterAttributes(aset, false);
                tp.replaceSelection(msg);
            }
        });
    }

    private void resetPreferences(Preferences preferences) {
        strictCheckBox.setSelected(preferences.getBoolean(PREF_STRICT, false));
        final boolean isOverrideImageNamesSelected = preferences.getBoolean(PREF_OVERRIDE_IMAGE_NAMES, false);
        overrideImageNamesCheckBox.setSelected(isOverrideImageNamesSelected);
        outputImageNamePatternText.setText(preferences.get(PREF_OUTPUT_IMAGE_NAME_PATTERN, ""));
        outputImageNamePatternText.setEnabled(isOverrideImageNamesSelected);
        forceOutputCheckbox.setSelected(preferences.getBoolean(PREF_FORCE_OUTPUT, true));
        compressionQualityModel.setValue(preferences.getFloat(PREF_COMPRESSION_QUALITY, PspiIndexGenerator.DEFAULT_COMPRESSION_QUALITY * 100));
        resetRadioGroupValue(preferences, PREF_CROP_ANCHOR, PspiIndexGenerator.DEFAULT_CROP_ANCHOR);
        resetRadioGroupValue(preferences, PREF_RESIZE, RESIZE_NO_RESIZE);
        resetRadioGroupValue(preferences, PREF_DATA_SOURCE, DATA_SOURCE_USE_FILE_NAMES);
        imageFilePatternText.setText(preferences.get(PREF_IMAGE_FILE_PATTERN, PspiIndexGenerator.DEFAULT_IMAGE_FILE_PATTERN));
        imageFolderPatternText.setText(preferences.get(PREF_IMAGE_FOLDER_PATTERN, PspiIndexGenerator.DEFAULT_IMAGE_FOLDER_PATTERN));
        dataFilePathText.setText(preferences.get(PREF_DATA_FILE_PATH, ""));
        
    }

    private void savePreferences(Preferences preferences) {
        preferences.putBoolean(PREF_STRICT, strictCheckBox.isSelected());
        preferences.putBoolean(PREF_OVERRIDE_IMAGE_NAMES, overrideImageNamesCheckBox.isSelected());
        preferences.put(PREF_OUTPUT_IMAGE_NAME_PATTERN, outputImageNamePatternText.getText());
        preferences.putBoolean(PREF_FORCE_OUTPUT, forceOutputCheckbox.isSelected());
        preferences.putFloat(PREF_COMPRESSION_QUALITY, compressionQualityModel.getNumber().floatValue());
        saveRadioGroupValue(preferences, PREF_CROP_ANCHOR);
        saveRadioGroupValue(preferences, PREF_RESIZE);
        saveRadioGroupValue(preferences, PREF_DATA_SOURCE);
        preferences.put(PREF_IMAGE_FILE_PATTERN, imageFilePatternText.getText());
        preferences.put(PREF_IMAGE_FOLDER_PATTERN, imageFolderPatternText.getText());
        preferences.put(PREF_DATA_FILE_PATH, dataFilePathText.getText());
        flush(preferences);
    }

    private void flush(Preferences preferences) {
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveRadioGroupValue(Preferences preferences, final String radioPrefName) {
        preferences.put(radioPrefName, getSelectedRadioFromGroup(radioPrefName));
    }
    
    private void disableRadioGroup(String groupName) {
        iterateRadios(groupName, radio -> radio.setEnabled(false));
    }
    
    private void enableRadioGroup(String groupName) {
        iterateRadios(groupName, radio -> radio.setEnabled(true));
    }
    
    private String getSelectedRadioFromGroup(String groupName) {
        for(JRadioButton radio : radioButtonGroupsByName.get(groupName)) {
            if(radio.isSelected()) {
                return radio.getText();
            }
        }
        throw new RuntimeException("Unable to find selected value for radio group named: " + groupName);
    }
    
    private void iterateRadios(String groupName, Consumer<JRadioButton> consumer) {
        for(JRadioButton radio : radioButtonGroupsByName.get(groupName)) {
            consumer.accept(radio);
        }
    }
    
    private void resetRadioGroupValue(Preferences preferences, String groupName, String defaultValue) {
        final String text = preferences.get(groupName, defaultValue);
        iterateRadios(groupName, radio -> {
            final boolean selected = radio.getText().equalsIgnoreCase(text);
            radio.setSelected(selected);
        });
    }

    private void addRadioButtonGroup(String groupName, JRadioButton...radios) {
        final List<JRadioButton> group = new ArrayList<>();
        radioButtonGroupsByName.put(groupName, group);
        Arrays.stream(radios).forEach(group::add);
        addListenerToRadioButtonGroup(groupName, selectedRadio -> {
            iterateRadios(groupName, currentRadio -> {
                if(currentRadio != selectedRadio) {
                    currentRadio.setSelected(false);
                }
            });
        });

    }
    
    private void addListenerToRadioButtonGroup(String groupName, Consumer<JRadioButton> consumer) {
        iterateRadios(groupName, radio -> {
            radio.addItemListener(e -> {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    consumer.accept(radio);
                }
            });
        });
    }

    private void fixRadioLabel(JRadioButton radio) {
        radio.setVerticalTextPosition(SwingConstants.TOP);
        radio.setHorizontalTextPosition(SwingConstants.CENTER);
        radio.setPreferredSize(new Dimension(RADIO_WIDTH, 20));
        radio.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private ActionListener chooseDirectory(Supplier<String> startAtSupplier, Consumer<File> textConsumer) {
        return getFileChooserActionListener(startAtSupplier, textConsumer, JFileChooser.DIRECTORIES_ONLY, null);
    }
    
    private ActionListener chooseExcelFile(Supplier<String> startAtSupplier, Consumer<File> textConsumer) {
        return getFileChooserActionListener(startAtSupplier, textConsumer, JFileChooser.FILES_ONLY, new FileNameExtensionFilter("Excel Spreadsheet (*.xslx)", "xlsx"));
    }

    private ActionListener getFileChooserActionListener(Supplier<String> startAtSupplier, Consumer<File> textConsumer, final int fileSelectionMode, final FileFilter fileFilter) {
        return e -> {
            final String startAt = startAtSupplier.get();
            final JFileChooser fileChooser = new JFileChooser(startAt.length() > 0 ? new File(startAt) : null);
            fileChooser.setFileSelectionMode(fileSelectionMode);
            if(fileFilter != null) {
                fileChooser.setFileFilter(fileFilter);
            }
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                textConsumer.accept(fileChooser.getSelectedFile());
            }
        };
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final Preferences preferences = Preferences.userNodeForPackage(PspiIndexGeneratorGUI.class);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final PspiIndexGeneratorGUI wnd = new PspiIndexGeneratorGUI(preferences);
                wnd.setVisible(true);
            }
        });
    }

}
