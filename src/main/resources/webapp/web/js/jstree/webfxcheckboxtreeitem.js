/*
 *	
 *	input.tree-check-box {
 *		width:		auto;
 *		margin:		0;
 *		padding:	0;
 *		height:		14px;
 *		vertical-align:	middle;
 *	}
 *
 */

function WebFXCheckBoxTreeItem(sText, sAction, bChecked, eParent, sIcon, sOpenIcon) {
	this.base = WebFXTreeItem;
	this.base(sText, sAction, eParent, sIcon, sOpenIcon);
	
	this._checked = bChecked;
}

WebFXCheckBoxTreeItem.prototype = new WebFXTreeItem;
WebFXCheckBoxTreeItem.prototype.value="";

WebFXCheckBoxTreeItem.prototype.toString = function (nItem, nItemCount) {
	var foo = this.parentNode;
	var indent = '';
	if (nItem + 1 == nItemCount) { this.parentNode._last = true; }
	var i = 0;
	while (foo.parentNode) {
		foo = foo.parentNode;
		indent = "<img id=\"" + this.id + "-indent-" + i + "\" src=\"" + ((foo._last)?webFXTreeConfig.blankIcon:webFXTreeConfig.iIcon) + "\">" + indent;
		i++;
	}
	this._level = i;
	if (this.childNodes.length) { this.folder = 1; }
	else { this.open = false; }
	if ((this.folder) || (webFXTreeHandler.behavior != 'classic')) {
		if (!this.icon) { this.icon = webFXTreeConfig.folderIcon; }
		if (!this.openIcon) { this.openIcon = webFXTreeConfig.openFolderIcon; }
	}
	else if (!this.icon) { this.icon = webFXTreeConfig.fileIcon; }
	var label = this.text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
	var str = "<div id=\"" + this.id;	
	 str+= "\" ondblclick=\"webFXTreeHandler.toggle(this);\" class=\"webfx-tree-item ";
	if(this.disabled)	{str+="webfx-tree-item-disabled\" ";	}
	else {str+="\" ";}
	str+="onkeydown=\"return webFXTreeHandler.keydown(this, event)\">" +indent + "<img id=\"" + this.id + "-plus\" src=\"" +  ((this.folder)?((this.open)?((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon):((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon)):((this.parentNode._last)?webFXTreeConfig.lIcon:webFXTreeConfig.tIcon)) +
	  "\" onclick=\"webFXTreeHandler.toggle(this);\">";
	
	// insert check box
	str += "<input type=\"checkbox\"" +	" id=\"" + this.id + "-box\"  class=\"tree-check-box ";
		if(this.disabled)	{str+="webfx-tree-item-disabled\" disabled ";}
		else {str+="\" ";}
	   str+=" onclick=\"";
	   if(this.change!=null) str+="webFXTreeHandler.changeHander(this);" ;
	   if(this.click)  str+="webFXTreeHandler.clickHander(this);";
	     str+=" webFXTreeHandler.all[this.parentNode.id].setChecked(this.checked,false); ";
	     str+="document.getElementById(this.id.replace('-box','')+ '-anchor').focus();\"";
		 str+=(this._checked ? " checked=\"checked\"" : "") +" />";
	// end insert checkbox
	
	str += "<img id=\"" + this.id + "-icon\" class=\"webfx-tree-icon\" src=\"" + ((webFXTreeHandler.behavior == 'classic' && this.open)?this.openIcon:this.icon) + "\" onclick=\"webFXTreeHandler.select(this);\">"+
	"<a href=\"" + this.action+"\"" ;
    if(this.disabled)	{str+=" onclick=\"return false;\" disabled";}
    else  if(this.click!=null || this.change!=null)
    {
        str+=" onclick=\"webFXTreeHandler.all[this.parentNode.id].chanageCheck();";
        if(this.click)  str+="webFXTreeHandler.clickHander(this);";
        if(this.change)str+="webFXTreeHandler.changeHander(this);";
        str+="\"" ;
    }
    else
    {
    	str+=" onclick=\"webFXTreeHandler.all[this.parentNode.id].chanageCheck();\"";
    }

	str+= " id=\"" + this.id + "-anchor\" onfocus=\"webFXTreeHandler.focus(this);\" onblur=\"webFXTreeHandler.blur(this);\">" + label + "</a></div>";
	str += "<div id=\"" + this.id + "-cont\" class=\"webfx-tree-container\" style=\"display: " + ((this.open)?'block':'none') + ";\">";
	for (var i = 0; i < this.childNodes.length; i++) {
		str += this.childNodes[i].toString(i,this.childNodes.length);
	}
	str += "</div>";
	this.plusIcon = ((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon);
	this.minusIcon = ((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon);
	return str;
};

WebFXCheckBoxTreeItem.prototype.getChecked = function () {
	var divEl = document.getElementById(this.id);
	var inputEl = divEl.getElementsByTagName("INPUT")[0];
	return this._checked = inputEl.checked;
};
WebFXCheckBoxTreeItem.prototype.chanageCheck = function () {
            this.setChecked(!this.getChecked(),true);
    };

WebFXCheckBoxTreeItem.prototype.setChecked = function (bChecked,isSet,isPass) {
//alert(bChecked);
        if (isSet && bChecked != this.getChecked()) {
		     var divEl = document.getElementById(this.id);
		     var inputEl = divEl.getElementsByTagName("INPUT")[0];
		     this._checked = inputEl.checked = bChecked;
		     if (typeof this.onchange == "function")
		         this.onchange();
	    }
	    else
	    {
	         this._checked =  bChecked;
        }
	    if(this.getChecked() && !this.open)this.toggle();
	    if(webFXTreeConfig.AutoChecked)
	    {
	        if(!isPass)
	        {
	            this.checkedParent();
	            this.checkedChild();
	        }
	    }
    };

WebFXCheckBoxTreeItem.prototype.checkedParent=function(){
      var node=this.parentNode;
      if(node)
      {
         	if(webFXTreeConfig.CheckedType)
	        {
                 if(node.parentNode && this.getChecked())
                 {
                      if(!node.disabled)
                      {node.setChecked(this.getChecked(),true,true);  
                      node.checkedParent(); 
                      }         
                 }
                 else if(node.parentNode)
                 {
                    var childs=node.childNodes,hasTrue=false;
                    if(childs && childs.length>0)
                    {
                        for(i=0;i<childs.length;i++)
                        {
                            if(childs[i].getChecked())
                            {
                                hasTrue=true;
                                break;
                            }
                        }
                        if(!hasTrue)
                        {
                        	 if(!node.disabled)
                        	 {node.setChecked(hasTrue,true,true);
                        	node.checkedParent(); }
                       	}
                    }
                 }
	        }
	        else
	        {
                 if(node.parentNode && !this.getChecked())
                 {
                       if(!node.disabled)
                       {node.setChecked(this.getChecked(),true,true);  
                      node.checkedParent();    }     
                 }
                 else if(node.parentNode)
                 {
                    var childs=node.childNodes,hasTrue=true;
                    if(childs && childs.length>0)
                    {
                        for(i=0;i<childs.length;i++)
                        {
                            if(!childs[i].getChecked())
                            {
                                hasTrue=false;
                                break;
                            }
                        }
                       if(hasTrue)
                       {
	                       	 if(!node.disabled)
	                       	 {node.setChecked(hasTrue,true,true);
	                       	node.checkedParent(); }
	                   }
                    }
                 }
	        }
      }    
  };
    
 WebFXCheckBoxTreeItem.prototype.checkedChild = function(){
        var childs=this.childNodes;
//        alert( childs.length);
        if(childs && childs.length>0)
        {
            for(i in childs)
            { 
                if(!childs[i].disabled)
                {childs[i].setChecked(this.getChecked(),true,true);
                childs[i].checkedChild(); 
                }
            }
        }
    };