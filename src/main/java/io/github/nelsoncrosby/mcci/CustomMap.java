package io.github.nelsoncrosby.mcci;

import io.github.nelsoncrosby.utils.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Represents a custom Minecraft save-file
 */
public class CustomMap extends Content {
    /**
     * Provide the no-args constructor privately
     * <p/>
     * Reflection in {@link #detectContentType} requires a default constructor
     * of some kind, but we don't want others trying to instantiate this class
     * without passing a {@link #source}. Must be linked to by all
     * subclasses.
     */
    CustomMap() {
    }

    /**
     * Construct an object from a {@link #source}.
     *
     * @param source The {@link java.io.File} to use in {@link #source}
     */
    public CustomMap(File source) {
        super(source);
    }

    /**
     * @return The subdirectory of a profile that this content should go into
     */
    @Override
    protected String getDestSubdir() {
        return "saves";
    }

    /**
     * Test if the content at {@link #source} is of this type
     * 
     * This test relies on the fact that a save-file has the file
     *  "level.dat" in the top-level of the directory.
     *
     * @return {@code true} if this object represents valid content of this type
     */
    @Override
    public boolean isValid() {
        try (ZipFile zf = new ZipFile(getSource())) {
            // level.dat needs to exist in all save-files
            ZipEntry levelDat = zf.getEntry("level.dat");
            // If it's null, then the file doesn't exist so can't be a save-file.
            return levelDat != null;
        } catch (ZipException e) {
            // Not a Zip-formatted file, so can't be this content type
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Install this content into a profile.
     * <p/>
     * This type requires that we extract all the files into the "saves" directory.
     * <p/>
     * If {@code msgLog} is not null, it is used as a logging buffer.
     *
     * @param target The destination profile
     * @param msgLog The logging buffer to append to (or {@code null})
     * @throws java.io.IOException Something went wrong either in creating the link or
     *                             in copying the file.
     */
    @Override
    public void install(Profile target, Appendable msgLog) throws IOException {
        File destDir = new File(target.getGameDir(), getDestSubdir()).getAbsoluteFile();
        String name = getSource().getName();
        File dest = new File(destDir, name.substring(0, name.lastIndexOf('.'))).getAbsoluteFile();
        dest.mkdirs();
        try (ZipFile zf = new ZipFile(getSource())) {
            ZipEntry entry;
            for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
                entry = entries.nextElement();
                if (msgLog != null)
                    msgLog.append("Extracting ").append(entry.getName())
                            .append(" into ").append(dest.getAbsolutePath()).append('\n');
                File entryDest = new File(dest, entry.getName());
                entryDest.getParentFile().mkdirs();
                StreamUtils.copyStreams(
                        zf.getInputStream(entry),
                        new FileOutputStream(entryDest)
                );
            }
        }
    }
}
