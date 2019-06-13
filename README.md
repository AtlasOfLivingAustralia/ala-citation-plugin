# ALA Citation Plugin

A plugin to allow citations and bibliographic entries to be displayed easily.

The plugin uses the [Citation Style Language](https://citationstyles.org/)
(CSL) as a basis for input.
Bibliographic information using CSL terms can then be
formatted into a nice-looking bibliographic entry.
The plugin contains:

* Templates that allow bibliographic entries to be inserted into HTML
  * The templates should also be usable to produce plain-text entries for JSON, but that's a work in progress
* Style sheets to format the bibliographic entries
* A tag library for formatting bibliographiuc data
* Adaptors to convert bibliographic information into CSL form.

## Usage

Include the `bibliography.css` asset.

The simplest use is to provide a map of bibliographic
data in [CSL-form](#csl-input) to the `bibliography/item` template
and render a suitable chunk of HTML, for example:

```$xslt
<g:render template="bibliography/item" model="${[item: bibitem]}"/>
```

will render the item appropriately.

### Configuration

The following configuration variables can be set:

* `citation.abbreviateGivenName` Replace full given names with initials in author lists. Defaults to true
* `citation.maxAuthors` The number of authors in a list before truncating and adding an *et al*. Defaults to 4
* `citation.pageMarker` Include a p or pp in page numbers or ranges. Defaults to false
* `citation.doiResolver` The DOI resolver URL. Defaults to https://doi.org/
* `citation.adaptors` A list of adaptors to convert bibliographic data into CSL

## CSL Input

The item to be displayed uses the fields specified
by the CSL standard.
See 
https://docs.citationstyles.org/en/stable/specification.html#appendix-iv-variables 
for a full list and
https://github.com/citation-style-language/schema/blob/master/csl-data.json 
for a JSON schema describing the various formats.
Bibliography styler can be set up to use any fields buit the key fields are:

* **type** The type of item, see [item types](#item-types).
* **title** The item title
* **author** A list of authors, see [authors](#csl-author) for how to represent authors
* **issued** The date of publication, see [dates](#csl-date) for how to represent dates
* **volume** The volume number
* **number** The journal number
* **issue** The issue number
* **page** A page or page range (anything with 5-6, 5--6, 5---6 and the like will be turned into a range with an en-dash)
* **containerTitle** The title of a containing publication, journal, book, etc
* **editor** A list of authors, see [authors](#csl-author) for how to represent authors
* **DOI** The document identifier
* **URL** The URL, for web resources
* **accessed** The date of access for URLs
* **publisher** The name of the publisher
* **publisherPlace** The location of the publisher

### CSL Author

Authors come as an object with a number of fields:

* **family** The family name
* **given** A list of given names, separated by commas
* **droppingParticle** Nobility particles, such as 'von' or 'de' that are not strictly part of a
  family name but 
* **literal** A literal name

Generally, if available, the family, given and dropping particle will be used to construct a name.
Otherwise the literal is used.
The [tag library](#tag-library) will also accept a simple string.


### CSL Date

Dates are objects with the following possible fields:

* **dateParts** An array of arrays of integers. 
  The first array can have one or two elements, indicating a single date or date range.
  The second array contains either one, two or three elements for year, month and day.
  For example: `[[1987, 9], [1987, 10]]` is the range September 1987 to October 1987.
* **literal** A literal date string
* **raw** A raw (but potentially parsable) date
* **season** A season
* **circa** If true, this is an approxmate date

### Item Types

The item types are the standard item types
allowed by the CSL standard:
`article`, `article-journal`, `article-magazine`, `article-newspaper`,
`bill`, `book`, `broadcast`, `chapter`, `dataset`, `entry`,
`entry-dictionary`, `entry-encyclopedia`, `figure`, `graphic`,
`interview`, `legal_case`, `legislation`, `manuscript`, `map`,
`motion_picture`, `musical_score`, `pamphlet`, `paper-conference`,
`patent`, `personal_communication`, `post`, `post-weblog`,
`report`, `review`, `review-book`, `song`, `speech`, `thesis`,
`treaty`, `webpage`.

In addition, types common in biodiversity have been added:
`collection`, `monograph`, `list`

## Tag Library

The bibliography tag library can be used to format bibliographic elements
and contains the following tags.

(TODO) There is an ongoing attempt to get the tag library to work for both html and plain (raw) text.

### \<bib:entry\>

The start of a bibliographic entry.

* `type` (optional) The type of entry, using the [item types](#item-types) described above.

### \<bib:block\>

A block of related bibligraphic data.
If the body of the block is not empty, then the block takes care of styling and
connectors.

* `class` (optional) The CSS class to use for this block
* `prefix` (optional) Text to place in front of the block, if the content is not empty
* `suffix` (optional) Text to place in after the block, if the content is not empty
* `connector` (optional, defaults to ', ') How to connect this block to a following block.
  The connector is only used after a block is non-empty and a following block is non-empty.
  Connectors are stored in the `pageScope.bib-block-prefix` attribute and do not stack (TODO)
  
For example,
```
<bib:block class="bib-title" prefix="'" suffix="'" connector=". ">${title}</bib:block><%--
--%><bib:block class="bib-series" connector=", ">${series}</bib:block>
```

would produce the HTML

```
<span class="bib-title">'A concorde of sweete sound'</span>. <span class="bib-series">Tudor Composers</span>
```

(TODO) Note the `<%-- ... --%>` used to kill whitespace.
This is needed to avoid extraneous whitespace in the output that messes up the connectors.
Working out how to not need this would be good.


### \<bib:formatAuthors\>

Format a list of authors.

* `authors` (required) The author list
* `abbreviate` (optional, defaults to configuration) Abbreviate author given names, if possible
* `locale` (optional, defaults to request or default locale) The locale to use when formatting
* `max` (optional, defaults to configuration) The maximum number of authors before going to *et al*
* `encodeAs` (optional, defaults to html) Use html for html formatting, raw for plain text

### \<bib:formatAuthor\>

Format a single author.

* `author` (required) The author in [CSL format](#csl-author)
* `abbreviate` (optional, defaults to configuration) Abbreviate author given names, if possible
* `locale` (optional, defaults to request or default locale) The locale to use when formatting
* `encodeAs` (optional, defaults to html) Use html for html formatting, raw for plain text

### \<bib:formatPages\>

Format a page or page range.

* `page` (required) The page or page range
* `marker` (optional, defaults to configuration) Include page markers
* `encodeAs` (optional, defaults to html) Use html for html formatting, raw for plain text

### \<bib:formatDate\>

Format a date or date range.

* `date` (required) The date in [CSL format](#csl-date)
* `format` (optional, defaults to `year`) The date format, 
  one of `year`, `year-month`, `year-month-long`, `date`, `date-long`
* `locale` (optional, defaults to request or default locale) The locale to use when formatting
* `encodeAs` (optional, defaults to html) Use html for html formatting, raw for plain text

### \<bib:formatDoi\>

Format a document object identifier.

* `doi` (required) The DOI
* `encodeAs` (optional, defaults to html) Use html for html formatting, raw for plain text

## Customisation

The templates in the `bibliography` view directory can be customised to format whichever
way you happen to like by adding an appropriate template to your application.
Most templates simply forward to either `_article_generic`, `item_generic` or `publication_generic`
so overriding these templates will 

The bibliography CSS can also be adjusted, as required.
