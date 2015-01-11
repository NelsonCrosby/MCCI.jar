package io.github.nelsoncrosby.mcci;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Represents a resource pack
 */
public class ResourcePack extends Content {
    /**
     * Provide the no-args constructor privately
     * <p/>
     * Reflection in {@link #detectContentType} requires a default constructor
     * of some kind, but we don't want others trying to instantiate this class
     * without passing a {@link #source}. Must be linked to by all
     * subclasses.
     */
    ResourcePack() {
    }

    /**
     * Construct an object from a {@link #source}.
     *
     * @param source The {@link java.io.File} to use in {@link #source}
     */
    public ResourcePack(File source) {
        super(source);
    }

    /**
     * @return The subdirectory of a profile that this content should go into
     */
    @Override
    protected String getDestSubdir() {
        return "resourcepacks";
    }

    /**
     * Test if the content at {@link #source} is of this type
     * 
     * This test relies on the fact that a resource pack has the file
     *  "pack.mcmeta" in the top-level of the archive.
     *
     * @return {@code true} if this object represents valid content of this type
     */
    @Override
    public boolean isValid() {
        try (ZipFile zf = new ZipFile(getSource())) {
            // pack.mcmeta needs to exist in all resource packs
            ZipEntry packMetaEntry = zf.getEntry("pack.mcmeta");
            // If it's null, then the file doesn't exist so can't be a resource pack.
            return packMetaEntry != null;
        } catch (ZipException e) {
            // Not a Zip-formatted file, so can't be this content type
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
