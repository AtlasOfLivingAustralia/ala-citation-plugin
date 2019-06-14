package au.org.ala.citation

import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * Test cases for the BHL adaptor
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @license See LICENSE
 */
class BHLAdaptorSpec extends Specification {
    BHLAdaptor adaptor = new BHLAdaptor()

    def "test part 1"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-part-1.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'article-journal'
        item.author != null
        item.author.size() == 2
        item.author[0].family == "Kodela"
        item.author[0].given == "Phillip"
        item.title == "The reduction of Acacia burkittii to Acacia acuminata subsp. burkittii (Acacia sect. Juliflorae: Fabaceae, Mimosoideae)"
        item.containerTitle == "Telopea : Journal of plant systematics."
        item.volume == "7"
        item.issue == "4"
        item.issued != null
        item.issued.dateParts != null
        item.issued.dateParts[0][0] == 1998
    }

    def "test part 2"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-part-2.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'article-journal'
        item.author != null
        item.author.size() == 3
        item.author[0].family == "Araújo Neto"
        item.author[0].given == "João C."
        item.title == "Efeito da temperatura e da luz na germinação de sementes de Acacia polyphylla DC."
        item.containerTitle == "Sociedade Botânica de São Paulo"
        item.volume == null
        item.issue == null
        item.issued == null
    }

    def "test part 3"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-part-3.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'list'
        item.author != null
        item.author.size() == 1
        item.author[0].family == "Heller"
        item.author[0].given == "Amos Arthur"
        item.title == "Hawaiian Plants from A. A. Heller"
        item.containerTitle == "Gray Herbarium miscellaneous plant lists"
        item.volume == null
        item.issue == null
        item.issued != null
        item.issued.dateParts != null
        item.issued.dateParts[0][0] == 1896
    }

    def "test item 1"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-item-1.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'monograph'
        item.author != null
        item.author.size() == 1
        item.author[0].family == "Mueller"
        item.author[0].given == "Ferdinand, Freiherr"
        item.author[0].droppingParticle == 'von'
        item.title == "Iconography of Australian species of Acacia and cognate genera"
        item.volume == null
        item.number == "1-4"
        item.issue == null
        item.issued != null
        item.issued.dateParts != null
        item.issued.dateParts[0][0] == 1887
    }

    def "test item 2"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-item-2.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'collection'
        item.author != null
        item.author.size() == 118
        item.author[0].family == "Bailey"
        item.author[0].given == "L. H. (Liberty Hyde)"
        item.title == "Walter Deane correspondence.  Senders A-Z"
        item.volume == "Senders A, 1885-1929"
        item.number == null
        item.issue == null
        item.issued != null
        item.issued.dateParts != null
        item.issued.dateParts[0][0] == 1885
    }
    def "test item 3"() {
        given:
        def js = new JsonSlurper()
        def json = js.parse(this.class.getResource('bhl-item-3.json'), 'UTF-8')
        when:
        def item = adaptor.convert(json)
        then:
        item != null
        item.type == 'book'
        item.author != null
        item.author.size() == 2
        item.author[0].literal == "Curtis & Cobb."
        item.title == "A descriptive catalogue of choice vegetable, flower, and agricultural seeds"
        item.volume == "1869 Flower and kitchen garden directory"
        item.number == null
        item.issue == null
        item.publisher == "Curtis & Cobb."
        item.publisherPlace == "Boston MA"
        item.issued != null
        item.issued.dateParts != null
        item.issued.dateParts[0][0] == 1853
    }

}
