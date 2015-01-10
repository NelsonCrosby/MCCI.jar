package io.github.nelsoncrosby.mcci;

import io.github.nelsoncrosby.utils.StreamUtils;
import io.github.nelsoncrosby.utils.Sys;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Configuration of the Minecraft Launcher
 * 
 * Configuration is stored in a JSON object and loaded from/stored in the file
 *  {@code launche_profiles.json}. 
 */
public class LauncherConfig {
    /** A {@link File} shortcut to the {@code .minecraft} directory */
    public static final File DOT_MINECRAFT;
    static {
        // NCUtils is set up to create private directories similar to Minecraft,
        //  however not exactly. We can't rely on it.

        String userHome = System.getProperty("user.home");
        switch (Sys.SYSTEM) {
            case WINDOWS:
                // .minecraft is in ~\AppData\Roaming\.minecraft
                DOT_MINECRAFT = new File(userHome, "AppData/Roaming/.minecraft");
                break;
            case MAC:
                // .minecraft is in ~/Library/Application Support/.minecraft
                DOT_MINECRAFT = new File(userHome, "Library/Application Support/.minecraft");
                break;
            default:
                // Probably Unix-y
                // .minecraft is in ~/.minecraft
                DOT_MINECRAFT = new File(userHome, ".minecraft");
                break;
        }
    }

    /** A {@link File} shortcut to the {@code .minecraft/launcher_profiles.json} file */
    public static final File LAUNCHER_PROFILES_FILE = new File(DOT_MINECRAFT, "launcher_profiles.json");
    /** The comparator used for sorting profiles */
    public static final Comparator<Profile> PROFILES_SORTER = new Comparator<Profile>() {
        @Override
        public int compare(Profile o1, Profile o2) {
            // Sort profiles by name
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    /** The JSON representation of this config */
    private JSONObject configInfo;
    /** The sorted collection of profiles in {@link Profile} form */
    private SortedSet<Profile> profiles;

    /**
     * Load data from {@code .minecraft/launcher_profiles.json} and create a
     *  config object 
     */
    public LauncherConfig() {
        String launcherConfigData;
        try {
            launcherConfigData = StreamUtils
                    .readWholeFile(LAUNCHER_PROFILES_FILE)
                    .toString();
        } catch (IOException e) {
            e.printStackTrace();
            // Try to continue
            launcherConfigData = "{\"profiles\":{}}";
        }
        this.configInfo = new JSONObject(launcherConfigData);

        JSONObject profilesJson = configInfo.getJSONObject("profiles");
        this.profiles = new TreeSet<>(PROFILES_SORTER);
        for (String key : profilesJson.keySet()) {
            profiles.add(new Profile(profilesJson.getJSONObject(key)));
        }
    }

    /**
     * Get an iterator over {@link #profiles}
     * 
     * This iterator is guaranteed to be sorted by {@code profile.getName()}
     *  (as that is how we have implemented it). 
     *  
     * @return The sorted iterable of profiles
     */
    public Iterable<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Add a profile to the collection
     *  
     * @param profile The profile to add
     */
    public void addProfile(Profile profile) {
        profiles.add(profile);
    }

    /** 
     * @return The name of the currently selected profile
     */
    public String getSelectedProfile() {
        return configInfo.getString("selectedProfile");
    }

    /**
     * Set the name of the selected profile
     *  
     * @param profileName A profile to select
     */
    public void setSelectedProfile(String profileName) {
        configInfo.put("selectedProfile", profileName);
    }

    /** 
     * @return The selected {@link Profile} object
     */
    public Profile selectedProfile() {
        for (Profile profile : profiles) {
            if (Objects.equals(profile.getName(), getSelectedProfile()))
                return profile;
        }

        throw new Error("No valid profile selected (selected: " + getSelectedProfile() + ")");
    }

    /**
     * Select a profile from a {@link Profile} object
     * 
     * If {@code toSelect} is not already in the profiles collection, it gets
     *  added.
     *  
     * @param toSelect The {@link Profile} to select
     */
    public void selectProfile(Profile toSelect) {
        if (!profiles.contains(toSelect)) profiles.add(toSelect);
        setSelectedProfile(toSelect.getName());
    }

    /**
     * Export the config data back into JSON format and save it to
     *  {@code .minecraft/launcher_profiles.json}
     */
    public void saveInfo() {
        JSONObject profilesJson = new JSONObject();
        for (Profile profile : profiles) {
            profilesJson.put(profile.getName(), profile.getProfileInfo());
        }
        configInfo.put("profiles", profilesJson);
        
        String launcherConfigData = configInfo.toString();
        try {
            StreamUtils.writeToFile(launcherConfigData, LAUNCHER_PROFILES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
