<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Collaborative Editor</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
    <script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/diff_match_patch_uncompressed.js"></script>
    <script src="${pageContext.request.contextPath}/js/mobwrite_core.js"></script>
    <script src="${pageContext.request.contextPath}/js/mobwrite_form.js"></script>
</head>
<body onload="
        mobwrite.syncGateway = '${pageContext.request.contextPath}/document';
        mobwrite.share('demo_editor_title', 'demo_editor_text');
        ">
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
                        <button class="btn btn-default" type="button">
                            <span class=".glyphicon .glyphicon-plus"></span>
                        </button>
                    </span>
                <input type="text" class="form-control" placeholder="New document name">
            </div>
            <ul class="list-group">
                <li class="list-group-item">Cras justo odio</li>
                <li class="list-group-item">Dapibus ac facilisis in</li>
                <li class="list-group-item">Morbi leo risus</li>
                <li class="list-group-item">Porta ac consectetur ac</li>
                <li class="list-group-item">Vestibulum at eros</li>
            </ul>
        </div>
        <div class="col-md-9">
            <div class="panel panel-default">
                <div class="panel-heading">Document title</div>
                <div class="panel-body">
                    <textarea id="demo_editor_text" style="width:100%;height:100%;"></textarea>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>