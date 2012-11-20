// Prerequisite: template.js

addLoad(function(){
	var queries = document.getElementsByTagName("pre");
	for (i=0; i < queries.length; i++){
		queries[i].innerHTML = queries[i].innerHTML.trim();
	}
});