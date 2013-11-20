var transport = {};

var channel;

transport.openChannel = function (token) {
    channel = new goog.appengine.Channel(token);
    socket = channel.open();
    socket.onmessage = handleMessage;
};

function handleMessage(data) {
    if (data.data.indexOf("UPDATE_LIST") == -1) {
        mobwrite.force = true;
        mobwrite.syncRun1_();
    } else {
        action_handler.refreshDocumentList();
    }
}

transport.loadDocument = function (documentId, callback) {
    $.get("document/" + documentId, function (data) {
        return callback(data)
    });
};

transport.getDocumentHeaders = function (callback) {
    $.get("headers", function (data) {
        return callback(data)
    });
};

transport.createDocument = function (name) {
    $.post("document", {'name': name});
};