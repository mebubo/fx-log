package org.hildan.fxlog.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.apache.commons.io.input.Tailer;
import org.hildan.fxlog.coloring.ColorizedRowFactory;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.core.LogTailListener;
import org.hildan.fxlog.filtering.Filter;

public class MainController implements Initializable {

    private Config config;

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<LogEntry> logsTable;

    @FXML
    private ChoiceBox<Columnizer> columnizerSelector;

    @FXML
    private ChoiceBox<Colorizer> colorizerSelector;

    @FXML
    private TextField filterField;

    private Property<Columnizer> columnizer;

    private Property<Colorizer> colorizer;

    private ObservableList<LogEntry> columnizedLogs;

    private FilteredList<LogEntry> filteredLogs;

    private Property<Predicate<LogEntry>> filter;

    private Tailer tailer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        columnizedLogs = FXCollections.observableArrayList();
        filteredLogs = new FilteredList<>(columnizedLogs);
        filter = new SimpleObjectProperty<>(log -> true);
        colorizer = new SimpleObjectProperty<>();
        columnizer = new SimpleObjectProperty<>();
        configureColumnizerSelector();
        configureColorizerSelector();
        configureFiltering();
        configureLogsTable();
    }

    private void configureColorizerSelector() {
        ObservableList<Colorizer> colorizers = config.getColorizers();
        colorizerSelector.setItems(colorizers);
        colorizer.bindBidirectional(colorizerSelector.valueProperty());
        if (!colorizers.isEmpty()) {
            colorizer.setValue(colorizers.get(0));
        }
    }

    private void configureColumnizerSelector() {
        ObservableList<Columnizer> columnizers = config.getColumnizers();
        columnizerSelector.setItems(columnizers);
        columnizer.bindBidirectional(columnizerSelector.valueProperty());
        if (!columnizers.isEmpty()) {
            columnizer.setValue(columnizers.get(0));
        }
    }

    private void configureFiltering() {
        filteredLogs.predicateProperty().bind(filter);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                filter.setValue(Filter.matchRawLog(".*?" + newValue + ".*"));
            } else {
                filter.setValue(log -> true);
            }
        });
    }

    private void configureLogsTable() {
        logsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (columnizer.getValue() != null) {
            logsTable.getColumns().addAll(columnizer.getValue().getColumns());
        }
        columnizer.addListener((observable, oldValue, newValue) -> {
            logsTable.getColumns().clear();
            logsTable.getColumns().addAll(newValue.getColumns());
        });
        logsTable.setItems(filteredLogs);
        ColorizedRowFactory colorizedRowFactory = new ColorizedRowFactory();
        colorizedRowFactory.colorizerProperty().bind(colorizer);
        logsTable.setRowFactory(colorizedRowFactory);
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Log File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Log files (*.txt, *.log)", "*.txt", "*.log"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Other files", "*.*"));
        File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (file != null) {
            closeFile();
            LogTailListener listener = new LogTailListener(columnizer.getValue(), columnizedLogs);
            tailer = Tailer.create(file, listener, 500);
        }
    }

    public void closeFile() {
        if (tailer != null) {
            tailer.stop();
        }
        columnizedLogs.clear();
    }

    public void openPreferences() {
        // TODO handle preferences
    }

    public void quit() {
        Platform.exit();
    }

    public void copyRaw() {
        copySelectedLogsToClipboard(LogEntry::getInitialLog);
    }

    public void copyPretty() {
        copySelectedLogsToClipboard(LogEntry::toColumnizedString);
    }

    private void copySelectedLogsToClipboard(Function<LogEntry, String> logToLine) {
        String textLogs = logsTable.getSelectionModel()
                                   .getSelectedItems()
                                   .stream()
                                   .map(logToLine)
                                   .collect(Collectors.joining(String.format("%n")));
        ClipboardContent content = new ClipboardContent();
        content.putString(textLogs);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public void selectAll() {
        logsTable.getSelectionModel().selectAll();
    }

    public void unselectAll() {
        logsTable.getSelectionModel().clearSelection();
    }

    public void selectDarkTheme() {
        List<String> styles = mainPane.getScene().getStylesheets();
        styles.clear();
        styles.add(getClass().getResource("/org/hildan/fxlog/dark_theme.css").toExternalForm());
    }

    public void selectLightTheme() {
        List<String> styles = mainPane.getScene().getStylesheets();
        styles.clear();
        styles.add(getClass().getResource("/org/hildan/fxlog/light_theme.css").toExternalForm());
    }
}
