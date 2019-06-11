package au.org.ala.citation

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * An adaptor for the Biodiversity Heritage Library
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2019 Atlas of Living Australia
 */
class BHLAdaptor extends CSLAdaptor<Map> {
    // Nobility particles and similar
    static List DROPPING_PARTICLES = ['de', 'di', 'du', 'des', 'von', 'van', 'af', 'zu']
    static Pattern YEAR_PATTERN = Pattern.compile(/(\d\d\d\d)(?:-(\d\d\d\d))?/)
    static Pattern NUMBER_PATTERN = Pattern.compile(/no\.([\d-]+)(?:\s*\(\d\d\d\d\))?/)
    static Pattern ISSUE_SEASON_YEAR_PATTERN = Pattern.compile(/(\w+)\s*\(\d\d\d\d\)/)

    static GENRE_MAP = [
            'Article': 'article-journal',
            'Book': 'book',
            'Collection': 'collection',
            'Correspondance': 'personal_communication',
            'List': 'list',
            'Monograph/Item': 'monograph',
            'Serial': 'book'
    ]

    static DUDS = [ (char) '@', (char) '$', (char) '#', (char) '^', (char) '&', (char) '+', (char) ',', (char) '/' ] as Set

    /**
     * The identifier for the BHL adaptor
     *
     * @return 'bhl'
     */
    @Override
    String getIdentifier() {
        return "bhl"
    }

    /**
     * Convert a (JSON) BHL source into a CSL item.
     *
     * @param source The source
     *
     * @return A CSL item that matches the source
     */
    @Override
    Map convert(Map source) {
        def item = [:]
        def url = null
        switch (source.BHLType ?: 'Item') {
            case 'Item':
                url = source.ItemUrl
                break
            case 'Part':
                url = source.PartUrl
                break
            default:
                break
        }
        if (url)
            item.link = url
        item.title = trimJunk(source.Title)
        item.author = source.Authors.collect { author ->
            def parts = author.Name.split(',')
            def name = [:]
            if (parts.size() > 1) {
                def adjusted = Arrays.asList(parts).collect { part ->
                    part = part.trim()
                    def start = DROPPING_PARTICLES.find { part.startsWith(it) }
                    if (start) {
                        name.droppingParticle = start
                        part = part.substring(start.length()).trim()
                    }
                    def end = DROPPING_PARTICLES.find { part.endsWith(it) }
                    if (end) {
                        name.droppingParticle = end
                        part = part.substring(0, part.length() - end.length()).trim()
                    }
                    part
                }
                name.family = adjusted[0]
                name.given = adjusted.drop(1).join(', ')
            } else {
                name.literal = parts[0]
            }
            name
        }
        item.issued = parseDate(source.Date ?: source.PublicationDate)
        item.containerTitle = trimJunk(source.ContainerTitle)
        String volume = source.Volume
        String number = source.Number
        String issue = source.Issue
        if (volume && !number) {
            Matcher nm = NUMBER_PATTERN.matcher(volume)
            if (nm.matches()) {
                volume = null
                number = nm.group(1)
                if (nm.groupCount() == 2 && !item.issued)
                    item.issued = parseDate(nm.group(2))
            }
        }
        if (issue) {
            Matcher im = ISSUE_SEASON_YEAR_PATTERN.matcher(issue)
            if (im.matches()) {
                issue = im.group(1)
                if (!item.issued)
                    item.issued = parseDate(im.group(2))
            }
        }
        item.volume = volume
        item.number = number
        item.issue = issue
        item.page = source.PageRange
        item.publisher = trimJunk(source.PublisherName)
        item.publisherPlace = trimJunk(source.PublisherPlace)
        item.type = GENRE_MAP[source.Genre] ?: 'manuscript'
        return item
    }

    /**
     * Parse a date, either in year or year-year format
     *
     * @param date The date striung to parse
     *
     * @return A CSL date object
     */
    def parseDate(String date) {
        if (!date)
            return null
        Matcher m = YEAR_PATTERN.matcher(date)
        if (!m.matches())
            return [literal: date]
        Integer from = m.group(1).toInteger()
        Integer to = m.group(2)?.toInteger()
        return to ? [dateParts: [[from], [to]]] : [dateParts: [[from]]]
    }

    /**
     * Trim garbage characters from the end of a title etc.
     *
     * @param value The string to trim
     *
     * @return A string without extraneous junk.
     */
    def trimJunk(String value) {
        value = value?.trim()
        if (!value)
            return null
        int p = value.length() - 1
        char ch = value.charAt(p)
        while (p >= 0 && (Character.isWhitespace(ch) || DUDS.contains(ch))) {
            p--
            ch = p < 0 ? -1 : value.charAt(p)
        }
        return value.substring(0, p + 1)
    }
}
