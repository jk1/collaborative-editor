var action_handler = {};

/*
 * Initialize server connection & event handlers on document load
 */
$(document).ready(function () {
    transport.openChannel(channelToken);
    action_handler.setupMobwriteClient();
    //initially no document is chosen
    $('#documentPanel').hide();
    //load all known document names from server
    action_handler.refreshDocumentList();
    //bind event handlers
    $('#createDocument').on('click', createDocumentOnClick);
    $('.document-area').on("DOMSubtreeModified",function (e) {
        var height = $('.document-area').height();
        $('.shadow').css('min-height', height + 18 + 'px');
        $('.editor-wrapper').css('min-height',  height + 23 + 'px');
    });
});

action_handler.setupMobwriteClient = function () {
    //mobwrite.debug = true;
    mobwrite.syncUsername = clientId;
    //adaptive diff check, the more active a user is, the less time we wait between diff checks
    mobwrite.maxSyncInterval = 1000;
    mobwrite.minSyncInterval = 500;
};

action_handler.refreshDocumentList = function () {
    transport.getDocumentHeaders(function (headers) {
        $.each(headers, function (index, header) {
            addDocumentToPage(header.id, header.title);
        });
    });
};

/**
 * Handle document selection. When user selects document in a list
 * the document is opened and remote sharing session begins. Only one
 * sharing session may be in progress at any given moment
 */
function documentListOnClick(e) {
    e.preventDefault();
    //change list highlighting
    $('.active').removeClass('active');
    $(e.target).addClass('active');
    //stop remote document sharing for the old document, if any
    editor = $('.document-area');
    mobwrite.unshare(editor[0]);
    transport.dropView(clientId);
    //switch to a new document
    $('#documentPanel').show();
    $('#title').text(e.target.innerText);
    editor.attr('id', 'document_' + e.target.id);
    editor.val('');
    // start remote sharing for a new document
    mobwrite.syncGateway = '/document/' + e.target.id;
    mobwrite.share(editor[0]);
}


/**
 * Handle new document creation
 */
function createDocumentOnClick(e) {
    input = $('#nameInput');
    transport.createDocument(input.val(), function (header) {
        addDocumentToPage(header.id, header.name);
    });
    input.val('');
}

function addDocumentToPage(id, name) {
    if ($('#' + id).length == 0) {
        $('.list-group').append($('<a class="list-group-item" id="' + id + '" href="#"><span class="glyphicon glyphicon-chevron-right"></span> &nbsp;' + name + '</a>'));
        $('.list-group-item').on('click', documentListOnClick);
    }
}
