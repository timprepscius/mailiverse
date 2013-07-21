/***** mEditor.js *****/

CKEDITOR_DEFAULT_CONFIG = 
{
	toolbar : 'MyToolbar',
 
	toolbar_MyToolbar :
	[
//			{ name: 'document', items : [ 'NewPage','Preview' ] },
//			{ name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
			{ name: 'editing', items : ['Undo','Redo','-','Find','Replace','-','SelectAll'] },
//			{ name: 'insert', items : [ 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak' ,'Iframe' ] },
//	                '/',
			{ name: 'styles', items : [ 'Styles','Format' ] },
			{ name: 'basicstyles', items : [ 'Bold','Italic','Strike','-','RemoveFormat' ] },
			{ name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote' ] },
			{ name: 'links', items : [ 'Link','Unlink','Anchor' ] },
		{ name: 'tools', items : [ 'Maximize','-','About' ] }
	],
	
	height: "280px",
	enterMode: CKEDITOR.ENTER_BR
};

mEditor = 
{
	initialize: function()
	{
	},
		
	open: function(id, content) 
	{
		$('#' + id).ckeditor(CKEDITOR_DEFAULT_CONFIG);	
		$('#' + id).val(content);
		
		return id;
	},
		
	stripHTML: function(html)
	{
		var tmp = document.createElement("DIV");
		tmp.innerHTML = html;
		var value = tmp.textContent || tmp.innerText;
		if (value == null)
			return value;
			
//		value = value.replace(/^\t/gm,""); // somehow weird tabs are showing up
		value = value.replace(/\u00A0/g,""); // somehow weird A0 control characters are showing up
		var lines = value.split(/\r\n|\r|\n/);	
		return lines.join("\r\n");
	},
	
	getContent: function(id)
	{
		var editor = CKEDITOR.instances[id];
		if (editor)
		{
			var html = editor.getData();
			var text = mEditor.stripHTML(html);
			return { text: text, html:html };
		}
	
		return null;
	},
	
	setContent: function(id, body)
	{
		var editor = CKEDITOR.instances[id];
		if (editor)
			editor.setData(body);
	},
	
	close: function(id) 
	{
		var contents = mEditor.getContent(id);

		var editor = CKEDITOR.instances[id];
		if (editor) 
			editor.destroy(true);

		return contents;
	}
};

/***** mEditor.js *****/
