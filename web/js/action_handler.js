/*
 * Initialize server connection & event handlers on document load
 */
$(document).ready(function () {
    mobwrite.debug = true;
    //initially no document is chosen
    $('#documentPanel').hide();
    //load all known document names from server
    transport.getDocumentHeaders(function (headers) {
        $.each(headers, function (index, header) {
            addDocumentToPage(header.id, header.name);
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
    //change list highlighting
    $('.active').removeClass('active');
    $(e.target).addClass('active');
    $('#documentPanel').show();
    textArea = $('.document-area');
    //stop remote document sharing
    mobwrite.unshare(textArea.attr('id'));
    //switch to a new document
    $('#title').text(e.target.innerText);
    textArea.val('');
    newId = 'document_' + e.target.id;
    textArea.attr('id', newId);
    mobwrite.syncGateway = '/document/' + e.target.id;
    mobwrite.share(newId);
}

/**
 * Document creation
 */
function createDocumentOnClick(e) {
    input = $('#nameInput');
    transport.createDocument(input.val(), function(header){
        addDocumentToPage(header.id, header.name);
    });
    input.val('');
}

function addDocumentToPage(id, name) {
    $('.list-group').append($('<a class="list-group-item" id="' + id + '" href="#"><span class="glyphicon glyphicon-chevron-right"></span> &nbsp;' + name + '</a>'));
    $('.list-group-item').on('click', documentListOnClick);
}
