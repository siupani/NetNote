package client;

import com.fasterxml.jackson.annotation.JsonProperty;
import commons.Collection;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private Collection defaultCollection;
    private final List<Collection> collections;
    private String language;

    public Config() {
        this.defaultCollection = null;
        this.collections = new ArrayList<>();
        this.language = "English";  // Default language
    }

    public Collection getCollection(Collection collection) {
        if (collections.contains(collection)) {
            return collection;
        } else {
            throw new IllegalArgumentException("No such collection exists");
        }
    }

    public Collection getDefaultCollection() {
        return defaultCollection;
    }

    @JsonProperty("defaultCollection")
    public void setDefaultCollection(Collection defaultCollection) {
        this.defaultCollection = defaultCollection;
    }

    public void setCollectionName(Collection collection, String newName) {
        if (collections.stream().map(Collection::getId).toList().contains(collection.getId())) {
            for (int i = 0; i < collections.size(); i++) {
                if (collections.get(i).getId() == collection.getId()) {
                    collections.get(i).setName(newName.strip());
                }
            }
        } else {
            throw new IllegalArgumentException("No such collection exists");
        }
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void addCollection(Collection collection) {
        this.collections.add(collection);
    }

    public void removeCollection(Collection collection) {
        if (collections.contains(collection)) {
            collections.remove(collection);
        } else {
            throw new IllegalArgumentException("Collection not found");
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCollections(List<Collection> collections) {
        this.collections.clear();
        this.collections.addAll(collections);
    }
}