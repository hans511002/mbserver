//逗号格式化数字  formatNum(1234005651.789,2); 
function formatNum(num,n)
{//参数说明：num 要格式化的数字 n 保留小数位
    num = String(num.toFixed(n));
    var re = /(-?\d+)(\d{3})/;
    while(re.test(num)) num = num.replace(re,"$1,$2")
    return num;
}
function _format(pattern,num,z)
{
	//alert("pattern = " + pattern + "\n" + "num = " + num + "\n" + "z = " + z + "\n");
	var j = pattern.length >= num.length ? pattern.length : num.length ;   
    var p = pattern.split("");
    var n = num.split("");   
    var bool = true,nn = "";
    for(var i=0;i<j;i++){   
        var x = n[n.length-j+i];   
        var y = p[p.length-j+i];  
        //alert("x="+x+"_y="+y); 
        if( z == 0){   
            if(bool){   
                if( ( x && y && (x != "0" || y == "0")) || ( x && x != "0" && !y ) || ( y && y == "0" && !x )  ){   
                    nn += x ? x : "0";   
                    bool = false;   
                }  
            } else {
                nn +=  x ? x : "0" ;   
            }
        } else {
            if( y && ( y == "0" || ( y == "#" && x ) ))   
                nn += x ? x : "0" ;
        }
    }
   // alert("nn="+nn);
    return nn;   
}   
function _formatNumber(numChar,pattern){   
    var patterns = pattern.split(".");   
    var numChars = numChar.split(".");
    var z = patterns[0].indexOf(",") == -1 ? -1 : patterns[0].length - patterns[0].indexOf(",") ;   
    var num1 = _format(patterns[0].replace(",",""),numChars[0],0);
    var num2 = _format( patterns[1]?patterns[1].split('').reverse().join(''):"", numChars[1]?numChars[1].split('').reverse().join(''):"",1);   
    num1 =  num1.split("").reverse().join('');
    var reCat = eval("/[0-9]{" + (z-1) + "," + (z-1) + "}/gi");   
    var arrdata = z > -1 ? num1.match(reCat) : undefined ;   
    if( arrdata && arrdata.length > 0 ){   
        var w = num1.replace(arrdata.join(''),'');   
        num1 = arrdata.join(',') + ( w == "" ? "" : "," ) + w ;   
    }   
    num1 = num1.split("").reverse().join("");   
    return (num1 == "" ? "0" : num1) + (num2 != "" ? "." + num2.split("").reverse().join('') : "" );                       
}    
function formatNumber(num,opt)
{
	if(opt.pattern.indexOf("%")>0)
		num=num*100;
    var reCat = /[0#,.]{1,}/gi;
    var zeroExc = opt.zeroExc == undefined ? true : opt.zeroExc ;   
    var pattern = opt.pattern.match(reCat)[0];
    var patterns = opt.pattern.split(".");
    var numChars = num.toString().split(".");
    if(numChars[1] && patterns[1])
    	num=parseFloat(num).toFixed(patterns[1].length);
    numChar = num.toString();
    //alert(num+" numChar="+numChar);
    return !(zeroExc && numChar == 0) ? opt.pattern.replace(pattern,_formatNumber(numChar,pattern)) : opt.pattern.replace(pattern,"0");
}

/*
	var n = 1234567890000;   
    var p = "######.00";   
    var x = formatNumber(n,{pattern:p});   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    var p = "#";   
    var x = formatNumber(n,{pattern:p});   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    n = 1234567890000;   
    p = "######.##";   
    x = formatNumber(n,{pattern:p});   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
  
    var n = 1234567890000.00;   
    p = "######.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.00;   
    p = "######.00";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    p = "######.00";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    p = "######.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.00;   
    p = "###,###.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    p = "###,###.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.00;   
    p = "##,####.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    p = "$###,###.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 1234567890000.01;   
    p = "###,###.##元";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 0.01123123;   
    p = "$###,###.##";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 0.3453213;   
    p = "$###,###.00";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 0;   
    p = "###,###.00元";   
    x = formatNumber(n,{pattern:p})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n");   
  
    var n = 0;   
    p = "###,###.00元";   
    x = formatNumber(n,{pattern:p,zeroExc:false})   
    alert("n = " + n + "\n" + "p = " + p + "\n" + "x = " + x + "\n" + "zeroExc = " + false + "\n");  

*/


