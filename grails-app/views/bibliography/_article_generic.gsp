<bib:block class="bib-author"><bib:formatAuthors authors="${item.author}"/></bib:block><%--
--%><bib:block class="bib-title" prefix="'" suffix="'">${item.title}</bib:block><%--
--%><bib:block class="bib-container-title">${item.containerTitle}</bib:block><%--
--%><bib:block class="bib-journal-vnp">
<g:if test="${item.volume}">
    <g:if test="${item.number && item.issue}">
        (${item.volume})${item.number},${item.issue}<g:if test="${item.page}">:<bib:formatPages page="${item.page}" marker="false"/></g:if>
    </g:if>
    <g:elseif test="${item.number}">
        (${item.volume})${item.number}<g:if test="${item.page}">:<bib:formatPages page="${item.page}" marker="false"/></g:if>
    </g:elseif>
    <g:elseif test="${item.issue}">
        (${item.volume})${item.issue}<g:if test="${item.page}">:<bib:formatPages page="${item.page}" marker="false"/></g:if>
    </g:elseif>
    <g:else>
        <g:if test="${!item.volume.startsWith.('v')}"><g:message code="bibliography.volume.abbrev" default="vol"/> </g:if>${item.volume} <g:if test="${item.page}">, <bib:formatPages page="${item.page}" marker="true"/></g:if>
    </g:else>
</g:if>
<g:else>
    <g:if test="${item.number}">${item.number}</g:if>
    <g:if test="${item.issue}">${item.issue}</g:if>
    <g:if test="${item.page}">, <bib:formatPages page="${item.page}" marker="false"/></g:if>
</g:else>
</bib:block><%--
--%><bib:block class="bib-publisher">${item.publisher}</bib:block><%--
--%><bib:block class="bib-publisher-place">${item.publisherPlace}</bib:block><%--
--%><bib:block class="bib-date" connector=""><bib:formatDate format="${bibDateFormat ?: 'year'}" date="${item.issued}"/></bib:block>