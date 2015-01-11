package io.github.nelsoncrosby.mcci;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents custom content on the Filesystem.
 * 
 * Implements a basic {@link #install} that hard-links the content into its
 *  specific subdirectory. 
 */
public abstract class Content {   
    public static class UnsupportedContentTypeException extends Exception {
        public UnsupportedContentTypeException(File conflicted) {
            super("The file " + conflicted.getAbsolutePath() + " wasn't a recognized content type");
        }
    }
    
    private static final List<Class<? extends Content>> REGISTERED_CONTENT_TYPES;
    static {
        REGISTERED_CONTENT_TYPES = new LinkedList<>();
        REGISTERED_CONTENT_TYPES.add(ForgeMod.class);
    }
    
    public static Content detectContentType(File src) throws UnsupportedContentTypeException {
        src = src.getAbsoluteFile();
        for (Class<? extends Content> type : REGISTERED_CONTENT_TYPES) {
            Content testAgainst;
            try {
                testAgainst = type.newInstance();
                testAgainst.source = src;
            } catch (InstantiationException | IllegalAccessException e) {
                // This should never happen
                throw new Error("Should never happen!", e);
            }

            if (testAgainst.isValid()) {
                return testAgainst;
            }
        }
        throw new UnsupportedContentTypeException(src);
    }

    /** A file representing where the content actually is */
    private File source;

    /**
     * Provide the no-args constructor privately
     * 
     * Reflection in {@link #detectContentType} requires a default constructor
     *  of some kind, but we don't want others trying to instantiate this class
     *  without passing a {@link #source}. Must be linked to by all
     *  subclasses.
     */
    protected Content() {}

    /**
     * Construct an object from a {@link #source}.
     *  
     * @param source The {@link File} to use in {@link #source}
     */
    public Content(File source) {
        this.source = source.getAbsoluteFile();
    }

    /**
     * @return File representing where the content actually is
     */
    public File getSource() {
        return source;
    }

    /**
     * Install this content into a profile.
     * 
     * A default for this method is provided - hard-link (where supported by
     *  the filesystem; basic copy otherwise) the source file to the destination
     *  profile and the subdir provided by getDestSubdir.
     * 
     * If {@code msgLog} is not null, it is used as a logging buffer.
     *  
     * @param target The destination profile  
     * @param msgLog The logging buffer to append to (or {@code null})
     * @throws IOException Something went wrong either in creating the link or
     *      in copying the file. 
     */
    public void install(Profile target, Appendable msgLog) throws IOException {
        File destDir = new File(target.getGameDir(), getDestSubdir()).getAbsoluteFile();
        File dest = new File(destDir, source.getName()).getAbsoluteFile();
        if (msgLog != null) {
            msgLog.append("Linking ")
                    .append(source.getAbsolutePath())
                    .append(" to ")
                    .append(dest.getAbsolutePath())
                    .append('\n');
        }
        Path sourcePath = source.toPath();
        Path destPath = dest.toPath();
        dest.getParentFile().mkdirs();
        try {
            Files.createLink(destPath, sourcePath);
        } catch (UnsupportedOperationException e) {
            Files.copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    /**
     * @return The subdirectory of a profile that this content should go into
     */
    protected abstract String getDestSubdir();

    /**
     * Test if the content at {@link #source} is of this type
     *
     * @return {@code true} if this object represents valid content of this type
     */
    public abstract boolean isValid();
}
