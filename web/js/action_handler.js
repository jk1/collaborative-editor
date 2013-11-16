/*
 * Initialize server connection & event handlers on document load
 */
$(document).ready(function () {
    mobwrite.syncGateway = '${pageContext.request.contextPath}/document';
    mobwrite.share('demo_editor_title', 'demo_editor_text');
    //initially no document is chosen
    $('#documentPanel').hide();
    //load all known document names from server
    transport.getDocumentHeaders(function (headers) {
        $.each(headers, function (index, header) {
            addDocumentToThePage(header.id, header.name);
        });
    });
    //bind event handlers
    $('#createDocument').on('click', createDocumentOnClick);
});

/**
 * Document selection
 */
function documentListOnClick(e) {
    e.preventDefault();
    transport.loadDocument(e.target.id, function (document) {
        $('#title').text(document.title);
        $('#demo_editor_text').text(document.text);
        $('#documentPanel').show();
    });
    $('.active').removeClass('active');
    $(e.target).addClass('active');
}

/**
 * Document creation
 */
function createDocumentOnClick(e) {
    input = $('#nameInput');
    transport.createDocument(input.val(), function(header){
        addDocumentToThePage(header.id, header.name);
    });

    input.val('');
}

function addDocumentToThePage(id, name) {
    $('.list-group').append($('<a class="list-group-item" id="' + id + '" href="#"><span class="glyphicon glyphicon-chevron-right"></span> &nbsp;' + name + '</a>'));
    $('.list-group-item').on('click', documentListOnClick);
}
