/*
 * Initialize server connection & event handlers on document load
 */
$(document).ready(function () {
    mobwrite.debug = true;
    mobwrite.syncUsername = channelToken;
    mobwrite.maxSyncInterval = 1000;
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
 * Handle document selection. When user selects document in a list
 * the document is opened and remote sharing session begins. Only one
 * sharing session may be in progress at any given moment, so
 */
function documentListOnClick(e) {
    e.preventDefault();
    //change list highlighting
    $('.active').removeClass('active');
    $(e.target).addClass('active');
    //stop remote document sharing for the old document, if any
    textArea = $('.document-area');
    mobwrite.unshare(textArea.attr('id'));
    //switch to a new document
    $('#documentPanel').show();
    $('#title').text(e.target.innerText);
    textArea.val('');
    newId = 'document_' + e.target.id;
    textArea.attr('id', newId);
    // start remote sharing for a new document
    mobwrite.syncGateway = '/document/' + e.target.id;
    mobwrite.share(newId);
}

/**
 * Handle new document creation
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
