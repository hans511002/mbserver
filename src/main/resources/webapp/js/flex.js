
function updateCode(event)
{
	var code;
	if(typeof(event) == "string") {
		code = event;
	}
	else
	{
		var elt = getEventTarget(event);
		code = elt['id'];
	}

	if (typeof codes[code] != "undefined") {
		newCode = codes[code];
	}
	$("expr").value = "var flexApp = FABridge.example.root();\n\n" + newCode;
}

function testEval() {
	var funcExpr = $("expr").value;
	eval(funcExpr);
}

function trace(msg) {
	$("output").value = msg.toString() + "\n" + $("output").value;	
}

function getEventTarget(e) {
	if (/Explorer/.test(navigator.appName))
		return e.srcElement;
	else
		return e.target;
}

function dumpit(e) {
	var out = "";
	for (var aProp in e)
		out += ("obj[" + aProp + "] = " + e[aProp]) + "\n";
	trace(out);
}
