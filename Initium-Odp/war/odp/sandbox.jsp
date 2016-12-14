<!doctype html>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<html>
<head>
	<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=45"/>
	<style type="text/css" media="screen">
	    #editor { 
	        width: 100%;
	        height: 100%;
	    }
	</style>
	
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
	<script src="/odp/javascript/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	
	<script type="text/javascript" src="/odp/javascript/script.js"></script>
	
	<title>Initium Game Site</title>

</head>

<body>
	<%JspSnippets.allowPopupMessages(out, request); %>
	
	<div>
		<a onclick='viewMap()'>viewMap</a>
		<a onclick='viewSettings()'>viewSettings</a>
		<a onclick='closeAllPagePopups()'>Close all</a>
	</div>
	
	<div id="page-popup-root">
	</div>
	
	<div id="editor">function foo(items) {
	    var x = "All this is syntax highlighted";
	    return x;
	}</div>	
	

	<script>
		$('#editor').on('keyup', function(e) {
	    	e.stopPropagation();
	    });
	
	    var editor = ace.edit("editor");
	    editor.setTheme("ace/theme/monokai");
	    editor.getSession().setMode("ace/mode/javascript");
	    ace.config.loadModule("ace/ext/tern", function() {
	        editor.setOptions({
	            enableTern: {
	                defs: ['browser', 'ecma5'],
	                plugins: {
	                    doc_comment: {
	                        fullDocs: true
	                    }
	                },                    
	                useWorker: true
	            },
	            enableSnippets: true,
	            enableBasicAutocompletion: true
	        });
	    });
	</script>
</body>
</html>
