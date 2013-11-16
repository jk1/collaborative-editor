var transport = {};

var channel;

transport.openChannel = function (token) {
    channel = new goog.appengine.Channel(token);
};

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

transport.createDocument = function (name, callback) {
    $.post("document",{'name':name}, function (data) {
        return callback(data)
    });
};