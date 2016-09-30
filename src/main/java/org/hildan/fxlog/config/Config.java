package org.hildan.fxlog.config;

import java.io.IOException;
import java.nio.file.Paths;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.themes.Theme;
import org.jetbrains.annotations.NotNull;

/**
 * Contains the configuration of the app. This includes preferences, recent files, custom columnizers, colorizers etc.
 * <p>
 * The config is a singleton, and can be accessed using {@link Config#getInstance}.
 */
public class Config {

    /**
     * Holder class for thread-safe singleton pattern.
     */
    private static class Holder {
        static final Config INSTANCE = ConfigLoader.getUserConfig();
    }

    /**
     * Returns the current configuration.
     *
     * @return the current configuration
     */
    @NotNull
    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The version of the format of the config. This needs to be increased each time the serialization format of this
     * class is changed.
     */
    static final int FORMAT_VERSION = 3;

    private final Integer version;

    private final Preferences preferences;

    private final State state;

    private final ObservableList<Columnizer> columnizers;

    private final ObservableList<Colorizer> colorizers;

    /**
     * Creates an empty configuration.
     */
    Config() {
        this.version = FORMAT_VERSION;
        this.preferences = new Preferences();
        this.state = new State();
        this.columnizers = FXCollections.observableArrayList();
        this.colorizers = FXCollections.observableArrayList();
    }

    public Integer getVersion() {
        return version;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public State getState() {
        return state;
    }

    /**
     * Returns the list of available {@link Columnizer}s.
     *
     * @return the list of available {@link Columnizer}s, potentially empty.
     */
    @NotNull
    public ObservableList<Columnizer> getColumnizers() {
        return columnizers;
    }

    /**
     * Returns the list of available {@link Colorizer}s.
     *
     * @return the list of available {@link Colorizer}s, potentially empty.
     */
    @NotNull
    public ObservableList<Colorizer> getColorizers() {
        return colorizers;
    }

    /**
     * Writes this config to the default user config file.
     *
     * @throws IOException
     *         if an I/O error occurs while writing to the file
     */
    public void persist() throws IOException {
        ConfigLoader.writeUserConfig(this);
    }
}
