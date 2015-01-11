package io.github.nelsoncrosby.mcci;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * 
 */
public class MCCI {
    private LauncherConfig launcherConfig;
    
    public MCCI() {
        launcherConfig = new LauncherConfig();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                launcherConfig.saveInfo();
            }
        }));
    }
    
    public void newProfile(String name, File gameDir) {
        launcherConfig.selectProfile(new Profile(name, gameDir));
    }
    
    public List<String> getProfileNames() {
        SortedSet<Profile> profiles = launcherConfig.getProfiles();
        List<String> ret = new ArrayList<>(profiles.size());
        for (Profile profile : profiles) {
            ret.add(profile.getName());
        }
        return ret;
    }
    
    public String selectedProfile() {
        return launcherConfig.getSelectedProfile();
    }
    
    public void selectProfile(String profileName) {
        launcherConfig.setSelectedProfile(profileName);
    }
    
    public void installContentToSelectedProfile(File content, Appendable msgLog)
            throws IOException, Content.UnsupportedContentTypeException
    {
        msgLog.append("Detecting content type\n");
        Content.detectContentType(content).install(launcherConfig.selectedProfile(), msgLog);
        msgLog.append("Done!");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MCCI app = new MCCI();
                SwingUI ui = new SwingUI(app);
                ui.setVisible(true);
            }
        });
    }
}
