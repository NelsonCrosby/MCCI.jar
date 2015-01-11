package io.github.nelsoncrosby.mcci;

import io.github.nelsoncrosby.utils.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Represents a collection of content
 */
public class ContentCollection extends Content {
    /**
     * Provide the no-args constructor privately
     * <p/>
     * Reflection in {@link #detectContentType} requires a default constructor
     * of some kind, but we don't want others trying to instantiate this class
     * without passing a {@link #source}. Must be linked to by all
     * subclasses.
     */
    ContentCollection() {
    }

    /**
     * Construct an object from a {@link #source}.
     *
     * @param source The {@link java.io.File} to use in {@link #source}
     */
    public ContentCollection(File source) {
        super(source);
    }

    /**
     * @return The subdirectory of a profile that this content should go into
     */
    @Override
    protected String getDestSubdir() {
        return ".";
    }

    /**
     * Test if the content at {@link #source} is of this type
     * 
     * This test relies on the fact that a Minecraft Forge mod has the file
     *  "mcmeta.info" in the top-level of the archive.
     *
     * @return {@code true} if this object represents valid content of this type
     */
    @Override
    public boolean isValid() {
        try (ZipFile zf = new ZipFile(getSource())) {
            ZipEntry entry;
            File tempdir = Files.createTempDirectory("MCCI").toFile();
            for (Enumeration<? extends ZipEntry> entries = zf.entries();
                    entries.hasMoreElements();) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".zip") || entry.getName().endsWith(".jar")) {
                    File dest = new File(tempdir, entry.getName());
                    dest.getParentFile().mkdirs();
                    StreamUtils.copyStreams(
                            zf.getInputStream(entry),
                            new FileOutputStream(dest)
                    );
                    try {
                        Content.detectContentType(dest);
                        // Haven't thrown, must be a supported content type
                        return true;
                    } catch (UnsupportedContentTypeException ignored) {
                        // Not content, ignore this one
                    }
                }
            }
            // No content
            return false;
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
     * Extracts and installs all included content types
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
        try (ZipFile zf = new ZipFile(getSource())) {
            ZipEntry entry;
            File tempdir = Files.createTempDirectory("MCCI").toFile();
            for (Enumeration<? extends ZipEntry> entries = zf.entries();
                 entries.hasMoreElements();) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".zip") || entry.getName().endsWith(".jar")) {
                    File entryDest = new File(tempdir, entry.getName());
                    entryDest.getParentFile().mkdirs();
                    if (msgLog != null)
                        msgLog.append("Extracting ").append(entry.getName())
                                .append(" to ").append(entryDest.getAbsolutePath()).append('\n');
                    StreamUtils.copyStreams(
                            zf.getInputStream(entry),
                            new FileOutputStream(entryDest)
                    );
                    try {
                        Content content = Content.detectContentType(entryDest);
                        // Haven't thrown, must be a supported content type
                        content.install(target, msgLog);
                    } catch (UnsupportedContentTypeException ignored) {
                        // Not content, ignore this one
                        if (msgLog != null)
                            msgLog.append(entry.getName()).append(" wasn't content, ignoring").append('\n');
                    }
                }
            }
        }
    }
}
