<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Collaborative Editor</title>
    <link rel="icon" href="${pageContext.request.contextPath}/img/favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/img/favicon.ico" type="image/x-icon" />
    <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/editor.css" rel="stylesheet">
</head>
<body>
<div id="wrap">
    <div class="navbar navbar-inverse">
        <a class="navbar-brand" href="#">Collaborative Editor</a>
        <ul class="nav navbar-nav navbar-right">
            <li class="active"><a href="${logoutUrl}">Logout</a></li>
        </ul>
    </div>
    <div class="container">
        <div class="row">
            <div class="col-md-3">
                <div class="input-group">
                    <span class="input-group-btn">
                        <button class="btn btn-default" type="button" id="createDocument">
                            <span class="glyphicon glyphicon-plus"></span>
                        </button>
                    </span>
                    <input type="text" class="form-control" placeholder="New document name" id="nameInput">
                </div>
                <br>

                <div class="panel panel-default">
                    <div class="panel-heading">
                        <span class="glyphicon glyphicon-book"></span> &nbsp;Available documents:
                    </div>
                    <div class="list-group">
                    </div>
                </div>
            </div>
            <div class="col-md-9">
                <div class="panel panel-default" id="documentPanel">
                    <div class="panel-heading">
                        <span class="glyphicon glyphicon-pencil"></span> &nbsp;<span id="title"></span>
                    </div>
                    <div class="panel-body">
                        <div class="editor-wrapper">
                            <div class="shadow editor"></div>
                            <div class="document-area editor" contenteditable="true"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="footer">
    <div class="container">
        <img src="https://developers.google.com/appengine/images/appengine-silver-120x30.gif"
             alt="Powered by Google App Engine"/>
    </div>
</div>
<%-- speed up page load a bit --%>
<script>
   var channelToken = "${token}";
   var clientId = "${clientId}";
</script>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script type="text/javascript" src="//netdna.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/_ah/channel/jsapi"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/diff_match_patch_uncompressed.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/mobwrite_core.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/mobwrite_div.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/transport.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/action_handler.js"></script>
</body>
</html>