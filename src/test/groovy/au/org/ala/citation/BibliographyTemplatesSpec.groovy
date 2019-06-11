package au.org.ala.citation


import grails.test.mixin.TestMixin
import grails.test.mixin.web.GroovyPageUnitTestMixin
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestMixin(GroovyPageUnitTestMixin)
class BibliographyTemplatesSpec extends Specification {
    BHLAdaptor adaptor
    JsonSlurper slurper

    def setup() {
        adaptor = new BHLAdaptor()
        slurper = new JsonSlurper()
        mockTagLib(BibliographyTagLib)
    }

    def load(String name) {
        return this.class.getResource(name).text
    }

    def "test article generic 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-part-1.json')))
        def result = render(template: '/bibliography/article_generic', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('article-generic-1.txt')
        then:
        result == expected
    }

    def "test article generic 2"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-part-2.json')))
        def result = render(template: '/bibliography/article_generic', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('article-generic-2.txt')
        then:
        result == expected
    }

    def "test article journal 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-part-1.json')))
        def result = render(template: '/bibliography/article_journal', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('article-journal-1.txt')
        then:
        result == expected
    }

    def "test article magazine 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-part-1.json')))
        def result = render(template: '/bibliography/article_magazine', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('article-magazine-1.txt')
        then:
        result == expected
    }

    def "test article newspaper 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-part-1.json')))
        def result = render(template: '/bibliography/article_newspaper', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('article-newspaper-1.txt')
        then:
        result == expected
    }

    def "test publication generic 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-item-1.json')))
        def result = render(template: '/bibliography/publication_generic', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('publication-generic-1.txt')
        then:
        result == expected
    }

    def "test book 1"() {
        when:
        def item = adaptor.convert(slurper.parse(this.class.getResource('bhl-item-1.json')))
        def result = render(template: '/bibliography/book', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('book-1.txt')
        then:
        result == expected
    }

    def "test item 1"() {
        when:
        def item = slurper.parse(this.class.getResource('generic-item-1.json'))
        def result = render(template: '/bibliography/item', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('generic-item-1.txt')
        then:
        result == expected
    }

    def "test item 2"() {
        when:
        def item = slurper.parse(this.class.getResource('generic-item-2.json'))
        def result = render(template: '/bibliography/item', model: [item: item]).replaceAll(/\s+/, ' ')
        def expected = load('generic-item-1.txt')
        then:
        result == expected
    }

}


