<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Collaborative Editor</title>
    <style type="text/css">
        body {
            background-color: white;
            font-family: sans-serif;
        }

        h1, h2, h3 {
            font-weight: normal;
        }

        textarea {
            font-family: sans-serif;
        }
    </style>
    <script src="/js/diff_match_patch_uncompressed.js"></script>
    <script src="/js/mobwrite_core.js"></script>
    <script src="/js/mobwrite_form.js"></script>
</head>
<body onload="mobwrite.syncGateway = '/document' ;mobwrite.share('demo_editor_title', 'demo_editor_text');">
<a href="${logoutUrl}">Logout</a>
<table style="height: 100%; width: 100%">
    <tr>
        <td height=1><h1>Collaborative Editor</h1></td>
    </tr>
    <tr>
        <td height=1><input type="text" id="demo_editor_title" style="width: 50%"></td>
    </tr>
    <tr>
        <td><textarea id="demo_editor_text" style="width: 100%; height: 100%"></textarea></td>
    </tr>
</table>
</body>
</html>