package au.org.ala.citation

import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import org.springframework.context.support.ResourceBundleMessageSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(BibliographyTagLib)
class BibliographyTagLibSpec extends Specification {
    BHLAdaptor adaptor
    Map part1
    Map part2
    Map item1

    @Shared
    ResourceBundleMessageSource messageSource = null

    @Shared
    Closure mockMessage = {Map map ->
        return messageSource.getMessage((String)map.code, (Object[])map.args, Locale.default)
    }

    def setupSpec(){
        URL url = new File('grails-app/i18n').toURI().toURL()
        messageSource = new ResourceBundleMessageSource()
        messageSource.bundleClassLoader = new URLClassLoader(url)
        messageSource.basename = 'messages'
        messageSource.setDefaultEncoding("utf-8")
    }

    def setup() {
        adaptor = new BHLAdaptor()
        JsonSlurper js = new JsonSlurper()
        part1 = adaptor.convert(js.parse(this.class.getResource('bhl-part-1.json')))
        part2 = adaptor.convert(js.parse(this.class.getResource('bhl-part-2.json')))
        item1 = adaptor.convert(js.parse(this.class.getResource('bhl-item-1.json')))
        tagLib.metaClass.message = mockMessage
    }

    def "test block 1"() {
        expect:
        applyTemplate('<bib:block class="block1">fred</bib:block>') == '<span class="block1">fred</span>'
    }

    def "test block 2"() {
        expect:
        applyTemplate('<bib:block class="block2"><span>xxx</span></bib:block>') == '<span class="block2"><span>xxx</span></span>'
    }

    def "test block 3"() {
        expect:
        applyTemplate('<bib:block class="block3"></bib:block>') == ''
    }

    def "test block 4"() {
        expect:
        applyTemplate('<bib:block class="block4" prefix="(" suffix=")">xxx</bib:block>')  == '<span class="block4">(xxx)</span>'
    }

    def "test block 5"() {
        expect:
        applyTemplate('<bib:block>fred</bib:block>')  == '<span>fred</span>'
    }

    def "test block 6"() {
        expect:
        applyTemplate('<bib:block>fred</bib:block><bib:block>xxx</bib:block>') == '<span>fred</span>, <span>xxx</span>'
    }

    def "test block 7"() {
        expect:
        applyTemplate('<bib:block connector="!">fred</bib:block><bib:block>xxx</bib:block>') == '<span>fred</span>!<span>xxx</span>'
    }

    def "test author 1"() {
        expect:
        tagLib.formatAuthor(author: part1.author[0], abbreviate: false) == 'Kodela,&nbsp;Phillip'
    }

    def "test author 2"() {
        expect:
        tagLib.formatAuthor(author: part1.author[0], abbreviate: true) == 'Kodela,&nbsp;P.'
    }

    def "test author 3"() {
        expect:
        tagLib.formatAuthor(author: item1.author[0], abbreviate: false) == 'Mueller,&nbsp;Ferdinand&nbsp;Freiherr&nbsp;von'
    }

    def "test author 4"() {
        expect:
        tagLib.formatAuthor(author: item1.author[0], abbreviate: true) == 'Mueller,&nbsp;F.F.&nbsp;von'
    }

    def "test author 5"() {
        expect:
        tagLib.formatAuthor(author: part2.author[0], abbreviate: false) == 'Ara&uacute;jo Neto,&nbsp;Jo&atilde;o C.'
    }

    def "test author 6"() {
        expect:
        tagLib.formatAuthor(author: part2.author[0], abbreviate: true) == 'Ara&uacute;jo Neto,&nbsp;J.'
    }

    def "test authors 1"() {
        expect:
        tagLib.formatAuthors(authors: part1.author, abbreviate: false) == 'Kodela,&nbsp;Phillip and Tindale,&nbsp;Mary'
    }

    def "test authors 2"() {
        expect:
        tagLib.formatAuthors(authors: part1.author, abbreviate: true) == 'Kodela,&nbsp;P. and Tindale,&nbsp;M.'
    }

    def "test authors 3"() {
        expect:
        tagLib.formatAuthors(authors: part2.author, abbreviate: false) == 'Ara&uacute;jo Neto,&nbsp;Jo&atilde;o C., Aguiar,&nbsp;Ivor B. and Ferreira,&nbsp;Vilma M.'
    }

    def "test authors 4"() {
        expect:
        tagLib.formatAuthors(authors: part2.author, abbreviate: true) == 'Ara&uacute;jo Neto,&nbsp;J., Aguiar,&nbsp;I. and Ferreira,&nbsp;V.'
    }

    def "test authors 5"() {
        expect:
        tagLib.formatAuthors(authors: part2.author, abbreviate: true, max: 2) == 'Ara&uacute;jo Neto,&nbsp;J., Aguiar,&nbsp;I. <em>et al</em>'
    }

    def "test authors 6"() {
        expect:
        tagLib.formatAuthors(authors: part2.author, abbreviate: true, max: 2, encodeAs: 'raw') == 'Ara\u00fajo Neto,\u00a0J., Aguiar,\u00a0I. et al'
    }

    def "test pages 1"() {
        expect:
        tagLib.formatPages(page: '1', marker: false) == '1'
    }

    def "test pages 2"() {
        expect:
        tagLib.formatPages(page: '7', marker: true) == 'p7'
    }

    def "test pages 3"() {
        expect:
        tagLib.formatPages(page: '1-2', marker: false) == '1&ndash;2'
    }

    def "test pages 4"() {
        expect:
        tagLib.formatPages(page: '7-8', marker: true) == 'pp7&ndash;8'
    }

    def "test pages 5"() {
        expect:
        tagLib.formatPages(page: '1--8', marker: false) == '1&ndash;8'
    }

    def "test pages 6"() {
        expect:
        tagLib.formatPages(page: '7--9', marker: true) == 'pp7&ndash;9'
    }

    def "test pages 7"() {
        expect:
        tagLib.formatPages(page: '7--9', marker: true, encodeAs: 'raw') == 'pp7\u20139'
    }

    def "test date 1"() {
        when:
        def date = [dateParts: [[1999]]]
        then:
        tagLib.formatDate(date: date) == '1999'
    }

    def "test date 2"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date) == '1999'
    }

    def "test date 3"() {
        when:
        def date = [dateParts: [[1999, 3, 10]]]
        then:
        tagLib.formatDate(date: date) == '1999'
    }

    def "test date 4"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date, format: 'year-month') == '1999-03'
    }

    def "test date 5"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date, format: 'year-month-long') == 'March, 1999'
    }

    def "test date 6"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date, format: 'year-month-long', locale: Locale.FRENCH) == 'mars, 1999'
    }

    def "test date 7"() {
        when:
        def date = [dateParts: [[1999, 3, 10]]]
        then:
        tagLib.formatDate(date: date, format: 'date') == '1999-03-10'
    }

    def "test date 8"() {
        when:
        def date = [dateParts: [[1999, 3, 10]]]
        then:
        tagLib.formatDate(date: date, format: 'date-long') == '10 March, 1999'
    }

    def "test date 9"() {
        when:
        def date = [dateParts: [[1999, 3, 10]]]
        then:
        tagLib.formatDate(date: date, format: 'date-long', locale: Locale.FRENCH) == '10 mars, 1999'
    }

    def "test date 10"() {
        when:
        def date = [raw: "1987"]
        then:
        tagLib.formatDate(date: date, format: 'date-long', locale: Locale.FRENCH) == '1987'
    }

    def "test date 11"() {
        when:
        def date = [literal: "March, 2017"]
        then:
        tagLib.formatDate(date: date) == 'March, 2017'
    }

    def "test date 12"() {
        when:
        def date = [season: "Spring"]
        then:
        tagLib.formatDate(date: date) == 'Spring'
    }

    def "test date 13"() {
        when:
        def date = [literal: "2017", circa: true]
        then:
        tagLib.formatDate(date: date) == 'c2017'
    }

    def "test date 14"() {
        when:
        def date = "2017"
        then:
        tagLib.formatDate(date: date) == '2017'
    }

    def "test date 15"() {
        when:
        def date = [dateParts: [[1999]]]
        then:
        tagLib.formatDate(date: date, format: 'year-month') == '1999'
    }

    def "test date 16"() {
        when:
        def date = [dateParts: [[1999]]]
        then:
        tagLib.formatDate(date: date, format: 'year-month-long') == '1999'
    }

    def "test date 17"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date, format: 'date') == '1999-03'
    }

    def "test date 18"() {
        when:
        def date = [dateParts: [[1999, 3]]]
        then:
        tagLib.formatDate(date: date, format: 'date-long') == 'March, 1999'
    }

    def "test doi 1"() {
        expect:
        tagLib.formatDoi(doi: "10.1109/5.771073") == '<a href="https://doi.org/10.1109/5.771073">10.1109/5.771073</a>'
    }

    def "test doi 2"() {
        expect:
        tagLib.formatDoi(doi: "10.1109/5.771073", encodeAs: 'raw') == '10.1109/5.771073'
    }

    def "test url 1"() {
        expect:
        tagLib.formatUrl(url: "https://www.ala.org.au") == '<a href="https://www.ala.org.au">https://www.ala.org.au</a>'
    }

    def "test url 2"() {
        expect:
        tagLib.formatUrl(url: "https://www.ala.org.au", encodeAs: 'raw') == 'https://www.ala.org.au'
    }

}
