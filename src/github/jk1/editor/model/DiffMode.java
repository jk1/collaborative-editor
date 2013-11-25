package github.jk1.editor.model;

/**
 * Diff string types:
 *
 * <ol>
 *     <li>Delta - diff between previous text version and a new one</li>
 *     <li>Raw - entire text content, for use cases when delta is not informative enough</li>
 * </ol>
 *
 * @author Evgeny Naumenko
 */
public enum DiffMode {

    DELTA,
    RAW

}
