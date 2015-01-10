package io.github.nelsoncrosby.mcci;

import org.json.JSONObject;

import java.io.File;

/**
 * Object representing a launcher profile
 */
public class Profile {
    /** The JSON data for this profile */
    private JSONObject profileInfo;

    /**
     * Construct a brand-new profile 
     *  
     * @param name The name of the profile
     */
    public Profile(String name) {
        this.profileInfo = new JSONObject();
        setName(name);
    }

    /**
     * Construct a profile from existing JSON data
     *  
     * @param profileInfo The JSON data to use
     */
    public Profile(JSONObject profileInfo) {
        this.profileInfo = profileInfo;
    }

    /**
     * @return The name of this profile
     */
    public String getName() {
        return profileInfo.getString("name");
    }

    /** 
     * @param name The new name for this profile
     */
    public void setName(String name) {
        profileInfo.put("name", name);
    }

    /** 
     * @return The gameDir for this profile
     */
    public File getGameDir() {
        return new File(profileInfo.getString("gameDir"));
    }

    /**
     * @param gameDir The new gameDir for this profile
     */
    public void setGameDir(File gameDir) {
        profileInfo.put("gameDir", gameDir.getAbsolutePath());
    }

    /**
     * @return The JSON data represented by this profile
     */
    public JSONObject getProfileInfo() {
        return profileInfo;
    }
}
