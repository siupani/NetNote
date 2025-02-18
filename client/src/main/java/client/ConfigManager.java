package client;

import client.utils.ServerUtils;
import client.utils.LocaleUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;


public class ConfigManager {
    public static final Path CONFIG_FILE_PATH = Paths.get("config.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ServerUtils server;
    private Config config;
    private LocaleUtil localeUtil;

    public ConfigManager() {
        server = new ServerUtils();
        config = new Config();
        localeUtil = new LocaleUtil();
    }

    /**
     * Save the Config object to a JSON file
     *
     * @param config - config to save
     * @throws IOException - if file is not found
     */
    public void saveConfig(Config config) throws IOException {// Serialize the Config object to JSON
        String configJson = objectMapper.writeValueAsString(config);

        // Write the JSON string to the config file
        Files.write(CONFIG_FILE_PATH, configJson.getBytes());
        System.out.println("Config saved");
    }


    /**
     * Load the Config object from the JSON file
     *
     * @return - the config read
     * @throws IOException - if file is not found
     */
    public Config loadConfig() throws IOException {
        File configFile = new File(String.valueOf(CONFIG_FILE_PATH));
        if (!configFile.exists()) { // If file doesn't exist, return default config
            Config defaultConfig = new Config();
            saveConfig(defaultConfig); // Save it so the file gets created
            return defaultConfig;
        }

        // If the file exists, read it using Jackson
        Config config = objectMapper.readValue(configFile, Config.class);
        //System.out.println("Loaded collections: " + config.getCollections().size());
        if (config.getCollections().isEmpty()) {
            System.out.println("No collections found in config.");
        } else {
            for (Collection collection : config.getCollections()) {
                //System.out.println("Collection: " + collection.getName());
            }
        }

        return config;
    }

    /**
     * this method reads the current default collection from the config file
     *
     * @return the current default collection
     */
    public Collection getDefaultCollection() {
        try {
            this.config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading the config in getDefaultCollection()");
            e.printStackTrace();
        }
        Collection defaultCollection = this.config.getDefaultCollection();
        if (defaultCollection == null) {
            defaultCollection = server.createDefaultCollection();
            this.config.setDefaultCollection(defaultCollection);
            //add the default collection to the list of collections in config, if it is not there yet
            if (!this.config.getCollections().contains(defaultCollection)) {
                this.config.getCollections().add(defaultCollection);
            }

            try {
                saveConfig(this.config);
            } catch (IOException e) {
                System.err.println("Failed saving the config in getDefaultCollection()");
                e.printStackTrace();
            }
        }
        return defaultCollection;
    }

    /**
     * method that sets the new default collection and saves it in the config file
     *
     * @param newDefault
     */
    public void setDefaultCollection(Collection newDefault) {
        if (newDefault == null) {
            System.err.println("Attempted to set default collection to null; ignoring.");
            return;
        }

        try {
            this.config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in setDefaultCollection()");
            e.printStackTrace();
        }

        // Set and possibly add it to the list
        this.config.setDefaultCollection(newDefault);
        if (!this.config.getCollections().contains(newDefault)) {
            this.config.getCollections().add(newDefault);
        }

        try {
            saveConfig(this.config);
            System.out.println("Default collection updated to: " + newDefault.getName());
        } catch (IOException e) {
            System.err.println("Failed saving config in setDefaultCollection()");
            e.printStackTrace();
        }
    }

    /**
     * Loads the language setting from the configuration.
     *
     * @return The Locale object representing the loaded language. If loading fails or LocaleUtil
     * is not initialized, it returns the default Locale or Locale.ENGLISH.
     */
    public Locale loadLanguage() {
        try {
            this.config = loadConfig();
            String languageCode = this.config.getLanguage();
            if (this.localeUtil == null) {
                System.err.println("LocaleUtil is not initialized");
                return Locale.getDefault();
            }
            return this.localeUtil.mapLanguageToLocale(languageCode);
        } catch (IOException e) {
            System.err.println("Failed loading language in loadLanguage()");
            e.printStackTrace();
            return Locale.ENGLISH;
        }
    }

    /**
     * Saves the given language setting to the configuration.
     *
     * @param locale The Locale object representing the language to be saved.
     */
    public void saveLanguage(Locale locale) {
        try {
            this.config = loadConfig();
            String language = localeUtil.mapLocaleToLanguage(locale);
            this.config.setLanguage(language);
            saveConfig(this.config);
            System.out.println("Language saved: " + language);
        } catch (IOException e) {
            System.err.println("Failed saving language in saveLanguage()");
            e.printStackTrace();
        }
    }

    /**
     * Setter included for testing
     *
     * @param server - server to mock test
     */
    public void setServer(ServerUtils server) {
        this.server = server;
    }

    /**
     * Setter included for testing
     *
     * @param config - confit to mock test
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    public void addCollection(Collection collection) {
        try {
            config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in setConfig()");
        }
        if (!config.getCollections().contains(collection)) {
            config.addCollection(collection);
        } else {
            return;
        }
        try {
            saveConfig(config);
        } catch (IOException e) {
            System.err.println("Failed saving config in setConfig()");
        }
        System.out.println("Collection " + collection.getName() + " added to config.");
    }

    public void changeCollectionName(Collection collection, String newName) {
        try {
            config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in setConfig()");
        }
        config.setCollectionName(collection, newName);
        try {
            saveConfig(config);
        } catch (IOException e) {
            System.err.println("Failed saving config in setConfig()");
        }
        System.out.println("Collection name changed in config from " + collection.getName() + " to " + newName);
    }

    public void removeCollection(Collection collection) {
        try {
            config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in setConfig()");
        }
        config.removeCollection(collection);
        try {
            saveConfig(config);
        } catch (IOException e) {
            System.err.println("Failed saving config in setConfig()");
        }
    }

    public void refreshCollections(List<Collection> collections) {
        try {
            config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in setConfig()");
        }
        config.setCollections(collections);
        try {
            saveConfig(config);
        } catch (IOException e) {
            System.err.println("Failed saving config in setConfig()");
        }
        System.out.println("Collections refreshed in config.");
    }

    public String getCollectionNameById(Long id) {
        try {
            config = loadConfig();
        } catch (IOException e) {
            System.err.println("Failed re-loading config in getCollectionById()");
        }
        Collection foundCollection = config.getCollections().
                stream().
                filter(value -> value.getId() == id).
                findFirst().
                orElse(null);
        if (foundCollection == null) {
            return null;
        } else {
            return foundCollection.getName();
        }
    }

}