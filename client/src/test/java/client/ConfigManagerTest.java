package client;

import client.utils.ServerUtils;
import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

	private ConfigManager configManager;

	@BeforeEach
	void setUp() {
		ServerUtils serverUtils = new ServerUtils();
		configManager = new ConfigManager();
		configManager.setServer(serverUtils);
	}

	@Test
	void testSaveConfig() throws IOException {
		// Create a Config object and save it using ConfigManager
		Config config = new Config();
		configManager.saveConfig(config);

		// Verify the config is saved by checking if the file exists (simple assertion)
		Path configFilePath = Paths.get("config.json");
		assertTrue(configFilePath.toFile().exists(), "Config file should be created");
	}

	@Test
	void testLoadConfig() throws IOException {
		// Simulate loading config
		Config config = configManager.loadConfig();

		// Verify the default config is loaded correctly
		assertNotNull(config, "Loaded config should not be null");
	}

	@Test
	void testSetDefaultCollection() throws IOException {
		// Test setting the default collection
		Collection newCollection = new Collection("New Collection");
		configManager.setDefaultCollection(newCollection);

		// Check if the default collection was updated
		Collection defaultCollection = configManager.getDefaultCollection();
		assertEquals("New Collection", defaultCollection.getName(), "Default collection name should match");
	}

	@Test
	void testSetDefaultCollection_Saved() throws IOException {
		// Test setting the default collection
		Collection newCollection = new Collection("New Collection");
		configManager.setDefaultCollection(newCollection);

		// Verify that the config was saved with the new default collection
		Config config = configManager.loadConfig();
		assertTrue(config.getCollections().contains(newCollection), "New collection should be part of the config");
	}

	@Test
	void testLoadLanguage() throws IOException {
		// Set a language and verify it's loaded correctly
		Locale expectedLocale = Locale.ITALIAN;
		// Create a Config object and save it using ConfigManager
		Config config = new Config();
		config.setLanguage("it");
		configManager.saveConfig(config);

		// First save the language to the config
		configManager.saveLanguage(expectedLocale);

		// Now load the language and verify it's saved correctly
		Locale loadedLocale = configManager.loadLanguage();
		assertEquals(expectedLocale, loadedLocale, "Loaded language should match the saved one");
	}
	@Test
	void testSetConfig() {
		// Set a custom config and verify it's applied
		Config customConfig = new Config();
		customConfig.setLanguage("it");
		configManager.setConfig(customConfig);

		// Verify that the config has been updated
		assertEquals("it", configManager.loadLanguage().getLanguage(), "Language should be updated to 'en'");
	}

	@Test
	void testAddCollection() throws IOException {
		Collection collection = new Collection("Test Collection");
		Config config = new Config();
		configManager.saveConfig(config);
		configManager.addCollection(collection);

		Config updatedConfig = configManager.loadConfig();
		assertTrue(updatedConfig.getCollections().contains(collection),
				"The collection should be added to the config.");
	}

	@Test
	void testChangeCollectionName() throws IOException {
		Collection collection = new Collection("Old Name");
		Config config = new Config();
		config.addCollection(collection);
		configManager.saveConfig(config);
		configManager.changeCollectionName(collection, "New Name");
		Config updatedConfig = configManager.loadConfig();
		assertTrue(updatedConfig.getCollections().stream()
						.anyMatch(c -> c.getName().equals("New Name")),
				"The collection name should be updated in the config.");
	}

	@Test
	void testRemoveCollection() throws IOException {
		Collection collection = new Collection("Test Collection");
		Config config = new Config();
		config.addCollection(collection);
		configManager.saveConfig(config);
		configManager.removeCollection(collection);
		Config updatedConfig = configManager.loadConfig();
		assertFalse(updatedConfig.getCollections().contains(collection),
				"The collection should be removed from the config.");
	}

	@Test
	void testRefreshCollections() throws IOException {
		Collection collection1 = new Collection("Collection 1");
		Collection collection2 = new Collection("Collection 2");
		List<Collection> newCollections = List.of(collection1, collection2);
		Config config = new Config();
		configManager.saveConfig(config);
		configManager.refreshCollections(newCollections);
		Config updatedConfig = configManager.loadConfig();
		assertEquals(2, updatedConfig.getCollections().size(),
				"The config should have the refreshed collections.");
		assertTrue(updatedConfig.getCollections().containsAll(newCollections),
				"The new collections should replace the old ones in the config.");
	}

	@Test
	void testGetCollectionNameById() throws IOException {
		Collection collection = new Collection("Test Collection");
		collection.setId(1L); // Assume there's a setId method
		Config config = new Config();
		config.addCollection(collection);
		configManager.saveConfig(config);
		String name = configManager.getCollectionNameById(1L);
		assertEquals("Test Collection", name,
				"The method should return the correct collection name by ID.");
	}






}
