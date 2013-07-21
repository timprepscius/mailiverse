/** resources **/

function resource_acquire(name)
{
	name = name + ".b64";
	if (EmbeddedResources != undefined && EmbeddedResources[name] != undefined)
		return EmbeddedResources[name];
		
	return 
		$.ajax({
			url:name,
			async:false,
		}).responseText();
}


/** resources **/
