package au.org.ala.citation

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

import java.text.SimpleDateFormat

class BibliographyTagLib implements GrailsConfigurationAware {
    static namespace = "bib"

    static BLOCK_PREFIX = 'bib-block-prefix'
    static ENDASH = '\u2013'
    static EMDASH = '\u2014'
    static NBSP = '\u00a0'
    static DATE_FORMATS = ['year': 'yyyy', 'year-month': 'yyyy-MM', 'date': 'yyyy-MM-dd', 'year-month-long': 'MMMM, yyyy', 'date-long': 'dd MMMM, yyyy']
    static defaultEncodeAs = [taglib: 'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    boolean abbreviateGivenName = false
    boolean pageMarker = false
    int maxAuthors = -1
    String doiResolver

    @Override
    void setConfiguration(Config config) {
        abbreviateGivenName = config.citation.abbreviateGivenName?.toBoolean() ?: false
        pageMarker = config.citation.pageMarker?.toBoolean() ?: false
        maxAuthors = config.citation.maxAuthors?.toInteger() ?: -1
        doiResolver = config.citation.doiResolver ?: 'https://doi.org/'
    }

    /**
     * A bibliographic entry
     *
     * @attr type The type of entry
     */
    def entry = { attrs, body ->
        def type = attrs.type
        boolean html = out.encoder.codecIdentifier.codecName == 'HTML'
        pageScope.setVariable(BLOCK_PREFIX, null)
        if (html) {
            out << raw("<div class=\"bibliography-entry")
            if (type)
                out << ' bib-' << raw(type)
            out << raw("\">")
        }
        out << body()
        if (html) {
            out << raw("</div>")
        }
        pageScope.setVariable(BLOCK_PREFIX, null)
    }

    /**
     * Format a block in the data. If the result is empty then the block is
     * not included.
     *
     * @attr prefix A prefix to place around the block, if present, defaults to ''
     * @attr suffix A suffix to place after the block, if present, defaults to ''
     * @attr connector How to connect to the next block, defaults to ', '
     * @attr class A CSS class to apply to the block, if present.
     *
     */
    def block = { attrs, body ->
        boolean html = out.encoder.codecIdentifier.codecName == 'HTML'
        def clazz = attrs['class']
        def prefix = attrs.prefix ?: ''
        def suffix = attrs.suffix ?: ''
        def connector = attrs.containsKey('connector') ? attrs.connector : ', '

        def content = body()?.trim()
        if (content) {
            def blockPrefix = pageScope.getVariable(BLOCK_PREFIX)
            if (blockPrefix)
                out << blockPrefix
            if (html)
                out << raw("<span") << raw(clazz ? " class=\"${clazz}\"" : '') << raw('>')
            out << prefix
            out << raw(content) // Don't double encode
            out << suffix
            if (html)
                out << raw("</span>")
            pageScope.setVariable(BLOCK_PREFIX, connector)
        }
    }

    /**
     * Format a list of authors
     *
     * @attr authors REQUIRED The author as a CSL name
     * @attr abbreviate Abbreviate given names (defaults to the configuration default)
     * @attr locale The locale to use (defaults to the request locale and then the default locale)
     * @attr max The maximum number of authors to display before et-aling, -1 for no limit (defaults to the configuration default)
     * @attr encodeAs Encode for a specific output (defaults to html, use raw for plain text)
     */
    def formatAuthors = { attrs, body ->
        def authors = attrs.authors
        if (!authors)
            return
        int total = authors.size()
        Iterator ai = authors.iterator()
        boolean abbreviate = attrs.containsKey('abbreviate') ? attrs.abbreviate.toBoolean() : abbreviateGivenName
        def encodeAs = attrs.encodeAs ?: defaultEncodeAs.taglib
        Locale locale = attrs.locale ?: request.locale ?: Locale.default
        int max = attrs.max ?: maxAuthors
        int i = 0
        boolean html = out.encoder.codecIdentifier.codecName == 'HTML'

        while (ai.hasNext()) {
            def author = ai.next()
            out << bib.formatAuthor(author: author, locale: locale, abbreviate: abbreviate, encodeAs: encodeAs)
            i++
            if (i == max && i < total) {
                out << ' '
                if (html)
                    out << raw('<em>')
                out << message(code: 'bibliography.etal', default: 'et al')
                if (html)
                    out << raw('</em>')
                break
            }
            if (i < total - 1)
                out << ', '
            else if (i == total - 1)
                out << ' ' << message(code: 'bibliography.and', default: '&') << ' '
        }
    }

    /**
     * Format a single author name
     *
     * @attr author REQUIRED The author as a CSL name
     * @attr abbreviate Abbreviate given names (defaults to the configuration default)
     * @attr locale The locale to use (defaults to the request locale and then the default locale)
     * @attr encodeAs Encode for a specific output (defaults to html, use raw for plain text)
     */
    def formatAuthor = { attrs, body ->
        def author = attrs.author
        if (!author)
            return
        Locale locale = attrs.locale ?: request.locale ?: Locale.default
        boolean abbreviate = attrs.containsKey('abbreviate') ? attrs.abbreviate.toBoolean() : abbreviateGivenName
        if (author in String)
            out << author
        else if (author.family) {
            def family = author.family
            def droppingParticle = author.droppingParticle
            if (droppingParticle && family.startsWith(droppingParticle)) {
                family = family.substring(droppingParticle.length()).trim()
            }
            out << family
            if (author.given) {
                def given = author.given.split(/\s*,\s*/).collect { part ->
                    if (droppingParticle && part.endsWith(droppingParticle))
                        part = part.substring(0, part.length() - droppingParticle.length()).trim()
                    if (abbreviate && part.length() > 1)
                        part = part.substring(0, 1) + '.'
                    part
                }
                out << ',' << NBSP
                for (int i = 0; i < given.size(); i++) {
                    out << given[i]
                    if (!abbreviate && i < given.size() - 1)
                        out << NBSP
                }
            }
            if (droppingParticle) {
                out << NBSP
                out << droppingParticle
            }
        } else if (author.literal) {
            out << author.literal
        }
    }

    /**
     * Format a page or page-range.
     * <p>
     * Ranges marked with a latex-style -- get converted to an en-dash.
     *
     * @param page REQUIRED The page number(s)
     * @param marker Include a p or pp marker, if needed (use configuration citation.pageMarker by default)
     * @attr encodeAs Encode for a specific output (defaults to html, use raw for plain text)
     */
    def formatPages = { attrs, body ->
        if (!attrs.page)
            return
        String page = attrs.page.replaceAll(/-+/, ENDASH)
        boolean marker = attrs.containsKey('marker') ? attrs.marker.toBoolean() : pageMarker
        if (marker && !page.startsWith('p'))
            out << (page.contains(ENDASH) ? 'pp' : 'p')
        out << page
    }

    /**
     * Format a date.
     *
     * @attr date REQUIRED The date to format
     * @attr format The expected format, one of 'year', 'year-month', 'date', 'year-month-long', 'date-long', defaults to year
     * @attr locale The locale to use (defaults to the request locale and then the default locale)
     * @attr encodeAs Encode for a specific output (defaults to html, use raw for plain text)
     */
    def formatDate = { attrs, body ->
        def date = attrs.date
        if (!date)
            return
        Locale locale = attrs.locale ?: request.locale ?: Locale.default
        String format = attrs.format ?: 'year'
        if (!date)
            return
        if (date in String)
            out << date
        else {
            if (date.circa)
                out << message(code: 'bibliography.circa.abbrev', default: 'c')
            if (date.dateParts) {
                def makeCal = { List dp ->
                    def cal = Calendar.getInstance()
                    cal.set(0, 0, 1, 0, 0, 0)
                    if (dp.size() >= 1)
                        cal.set(Calendar.YEAR, dp[0])
                    if (dp.size() >= 2)
                        cal.set(Calendar.MONTH, dp[1] - 1)
                    if (dp.size() >= 3)
                        cal.set(Calendar.DAY_OF_MONTH, dp[2])
                    cal
                }
                def size = date.dateParts.inject(3, { s, dp -> Math.min(s, dp.size()) })
                switch (size) {
                    case 1:
                        format = 'year'
                        break
                    case 2:
                        if (format == 'date')
                            format = 'year-month'
                        if (format == 'date-long')
                            format = 'year-month-long'
                        break
                    default:
                        break
                }
                Calendar from = date.dateParts.size() >= 1 ? makeCal(date.dateParts[0]) : null
                Calendar to = date.dateParts.size() >= 2 ? makeCal(date.dateParts[1]) : null
                def df = new SimpleDateFormat(DATE_FORMATS[format] ?: 'yyyy', locale)
                if (from)
                    out << df.format(from.getTime())
                if (to) {
                    out << NBSP << EMDASH << NBSP
                    out << df.format(to.getTime())
                }
            } else if (date.literal)
                out << date.literal
            else if (date.raw)
                out << date.raw
            else if (date.season)
                out << date.season
        }
    }

    /**
     * Format a document object identifier
     *
     * @attr doi (REQUIRED) The DOI
     * @attr encodeAs Encode as html or raw (defaults to html)
     */
    def formatDoi = { attrs, body ->
        String doi = attrs.doi
        if (!doi)
            return
        def encodeAs = attrs.encodeAs ?: defaultEncodeAs.taglib
        boolean html = out.encoder.codecIdentifier.codecName == 'HTML'

        if (html) {
            out << raw('<a href="') << raw(doiResolver) << raw(doi) << raw('">')
        }
        out << doi
        if (html) {
            out << raw("</a>")
        }
    }


    /**
     * Format a URL
     *
     * @attr url (REQUIRED) The URL
     * @attr encodeAs Encode as html or raw (defaults to html)
     */
    def formatUrl = { attrs, body ->
        String url = attrs.url
        if (!url)
            return
        def encodeAs = attrs.encodeAs ?: defaultEncodeAs.taglib
        boolean html = out.encoder.codecIdentifier.codecName == 'HTML'

        if (html) {
            out << raw('<a href="') << raw(url) << raw('">')
        }
        out << url
        if (html) {
            out << raw("</a>")
        }
    }


}
