/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.scenes.CollectionEditCtrl;
import client.scenes.NoteEditCtrl;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import client.scenes.MainCtrl;

public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(NoteEditCtrl.class).in(Scopes.SINGLETON);
        binder.bind(CollectionEditCtrl.class).in(Scopes.SINGLETON);

        // Bind ConfigManager and load Config
        ConfigManager configManager = new ConfigManager();
        Config config;
        try {
            // Load configuration from the configuration manager
            config = configManager.loadConfig();
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            // Fallback to default configuration if loading fails
            config = new Config();
        }

        // Bind Config as a singleton instance
        binder.bind(Config.class).toInstance(config);
    }
}