package au.org.ala.citation
/**
 * Convert data in whatever format is available into a CSL item
 *
 * @param T The type of source data expected
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @license See LICENSE
 */
abstract class CSLAdaptor<T> {
    /**
     * Get the adaptor identifier.
     * <p>
     * Identifiers are used to map the source information onto the correct adaptor.
     *
     * @return The adaptor identifier
     */
    abstract String getIdentifier()

    /**
     * Convert source data into a CSL item.
     *
     * @param source The source
     *
     * @return
     */
    abstract Map convert(T source)
}
