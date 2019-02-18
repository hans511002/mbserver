function HTTPRequest() 
{
	this.request 	= null;
	this.response 	= null;
	this.method		= 'POST';
	//this.open    	= open;
	//this.send		= send;
	try {
		this.xmlhttp = new XMLHttpRequest();
	} catch (e) {
		var XMLHTTP_IDS = new Array(
			'MSXML2.XMLHTTP.5.0',
			'MSXML2.XMLHTTP.4.0',
			'MSXML2.XMLHTTP.3.0',
			'MSXML2.XMLHTTP',
			'Microsoft.XMLHTTP' );
		var success = false;
		for (var i=0;i < XMLHTTP_IDS.length && !success; i++) {
			try {
				this.xmlhttp = new ActiveXObject(XMLHTTP_IDS[i]);
				success = true;
				break;
			} catch (e) {}
		}
		if (!success) {
			throw new Error('Unable to create XMLHttpRequest.');
		}
	}
}

HTTPRequest.prototype.open = function (url)
{
	this.xmlhttp.open(this.method, url, false);
	this.xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf8');
}

HTTPRequest.prototype.send = function (queryString)
{
	this.request = queryString;
	this.xmlhttp.send(queryString);
	if(this.xmlhttp.readyState == 4 && this.xmlhttp.status == 200) {
		this.response = this.xmlhttp.responseText;
	}
}
