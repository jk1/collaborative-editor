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
    <script SRC="resources/javascript/diff_match_patch_uncompressed.js"></script>
    <script SRC="resources/javascript/mobwrite_core.js"></script>
    <script SRC="resources/javascript/mobwrite_form.js"></script>
</head>
<body onload="mobwrite.share('demo_editor_title', 'demo_editor_text');">

<table style="height: 100%; width: 100%">
    <tr>
        <td height=1><h1>MobWrite as a Collaborative Editor</h1></td>
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