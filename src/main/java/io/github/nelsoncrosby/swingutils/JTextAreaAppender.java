package io.github.nelsoncrosby.swingutils;

import javax.swing.*;

/**
 * An {@link Appendable} that appends to a given JTextArea.
 * 
 * TODO: Documentation (currently a straight copy of {@link Appendable} docs)
 */
public class JTextAreaAppender implements Appendable {
    private final JTextArea target;

    /**
     * Create an instance that appends to {@code target}
     *
     * @param target The {@link JTextArea} to append to
     */
    public JTextAreaAppender(JTextArea target) {
        this.target = target;
    }

    /**
     * Appends the specified character sequence to this <tt>Appendable</tt>.
     * <p/>
     * <p> Depending on which class implements the character sequence
     * <tt>csq</tt>, the entire sequence may not be appended.  For
     * instance, if <tt>csq</tt> is a {@link java.nio.CharBuffer} then
     * the subsequence to append is defined by the buffer's position and limit.
     *
     * @param csq The character sequence to append.  If <tt>csq</tt> is
     *            <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *            appended to this Appendable.
     * @return A reference to this <tt>Appendable</tt>
     */
    @Override
    public Appendable append(CharSequence csq) {
        if (csq == null) csq = "null";
        target.append(csq.toString());
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this
     * <tt>Appendable</tt>.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in
     * exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.append(csq.subSequence(start, end)) </pre>
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *              will be appended as if <tt>csq</tt> contained the four
     *              characters <tt>"null"</tt>.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return A reference to this <tt>Appendable</tt>
     * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *                                   is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *                                   <tt>csq.length()</tt>
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        if (csq == null) csq = "null";
        append(csq.subSequence(start, end));
        return this;
    }

    /**
     * Appends the specified character to this <tt>Appendable</tt>.
     *
     * @param c The character to append
     * @return A reference to this <tt>Appendable</tt>
     */
    @Override
    public Appendable append(char c) {
        target.append(String.valueOf(c));
        return this;
    }
}
