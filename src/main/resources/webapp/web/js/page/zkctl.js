var MenuTree = new MzTreeView('MenuTree');
MenuTree.nodes['-1'+MenuTree.divider+'root']='text:/root'; 
MenuTree.setIconPath('js/TreeImages/');

var treeNodes={};
var curNode=null;
var curNodeCom=0;

function hashCode(str){
    var hash = 0;
    if (str.length == 0) return hash;
    for (i = 0; i < str.length; i++) {
        char = str.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

var nodeValue=document.getElementById('nodeValue');
var treeNode=document.getElementById('treeNode');
var nodePath=document.getElementById('nodePath');
var saveButton=document.getElementById('saveButton');
var rmrButton=document.getElementById('rmrButton');
var optmsg=document.getElementById('optmsg');
var loading=document.getElementById('loading');
var formatButton=document.getElementById('formatButton');
var listAll=getQuery("listAll");
listAll=listAll=="true";

String.prototype.startWith=function(str){
  var reg=new RegExp("^"+str);
  return reg.test(this);
}

String.prototype.endWith=function(str){
  var reg=new RegExp(str+"$");
  return reg.test(this);
}
function fillTree(pnode,node,subNodes,fun){
 	var parent=node;
 	var parentPath=node;
	if(node=="/"){
		parent="root";
		parentPath="";
	}
	var rootKey=parent+MenuTree.divider;//':';
	for (var keys in MenuTree.nodes) {
		if (keys.startWith(rootKey)) {
			delete MenuTree.nodes[keys];
    	}
    }
	for(var i=0;i<subNodes.length;i++){
		var subNode=subNodes[i];
		MenuTree.nodes[parent+MenuTree.divider+parentPath+"/"+subNode]="hasChild:true;text:"+subNode+";method:getNodeClick(\""+pnode+"\",\""+subNode+"\");value:"+subNode+";url:#;";
	}
	if(curNode){
		if(subNodes && subNodes.length){
			MenuTree.dataFormat();
			MenuTree.currentNode.hasChild=true;
			MenuTree.currentNode.childNodes=[];
			MenuTree.currentNode.icon="folder";
			MenuTree.currentNode.isLoad=false;
			MenuTree.expand(MenuTree.currentNode.id);
//			if(MenuTree.currentNode.id==node){
//			}
			if(fun)fun();
		}
	}else{
		treeNode.innerHTML=MenuTree.toString();
	}
}
function checkZk(fun){
	ZKAction.checkZk(function(data){
		if(data){
			fun();
		}else{
			document.getElementById('treeNode').innerHTML="未连接ZK服务器";
		}
	});
}

function listPath(path,fun){
	if(!path)path="/";
	loading.style.display="block";
	ZKAction.getChild(path,listAll,function(data){
		if(data==null || data=="null"){
			alert("未连接ZK服务器");
			return ;
		}
		var _path=path;
		var tnode=treeNodes;
		var pnode=path;
//		if(path.length>1){
//			_path=path.split("/");
//			tnode=tnode["/"];
//			for(var i=1;i<_path.length-1;i++){
//				tnode=tnode[_path[i]];
//			}
//			_path=_path[_path.length-1];
//		}
		tnode[_path]=data;
		fillTree(pnode,_path,data,fun);
	});
	getNodeData(path);
}
function getNodeData(path){
	loading.style.display="block";
	ZKAction.getData(path,formatButton.checked,function(data){
		if(data==null || data=="null"){
			nodeValue.value="获取数据失败";
		}else{
//			data=data.trim();
//			if(data!=""){
//				if(data.startsWith("{") || data.startsWith("[")){
//					data=JSON.stringify(JSON.parse(data,null), function(key, value) {
//						return value;
//					}, "    ");
//				}
//			}
			nodeValue.value=data[0];
			curNodeCom=data[1];
			comType.innerHTML=curNodeCom>0?"压缩数据":"未压缩";
		}
		loading.style.display="none";
	});
}

function pageInit(){
 	checkZk(listPath);
}
function getNodeClick(pnode,subNode,obj,node){
	loading.style.display="block";
	curNode=node;
	optmsg.innerHTML="";
	var path=pnode+"/"+subNode;
	if(path.startsWith("//"))
		path=path.substring(1);
	nodePath.value=path;
 	listPath(path);
}
function saveNode(){
	if(!confirm("确定保存")){
		return;
	}
	loading.style.display="block";
	saveButton.disabled=true;
	var path=nodePath.value;
	var val=nodeValue.value;
	curNodeCom=curNodeCom?curNodeCom:0;
	ZKAction.setData(path,val,curNodeCom,function(res){
		saveButton.disabled=false;
		optmsg.innerHTML="save "+res;
		MenuTree.expand(MenuTree.currentNode.id);
		var _path=path.substring(0,path.lastIndexOf("/"));
		var sourceId=path;
		MenuTree.focus(_path);
		listPath(_path,function(){
			MenuTree.focus(sourceId);
			getNodeData(sourceId);
		});
	});
}


function rmrNode(){
	var path=nodePath.value;
	if(!confirm("确定删除节点:"+path)){
		return;
	}
	loading.style.display="block";
	rmrButton.disabled=true;
 	ZKAction.rmrNode(path,function(res){
 		rmrButton.disabled=false;
		optmsg.innerHTML="rmr "+res;
		MenuTree.delnode(path);
		MenuTree.expand(MenuTree.currentNode.id);
		loading.style.display="none";
	});
}

function rmrChildNode(){
	var path=nodePath.value;
	if(!confirm("确定删除"+path+"下的子节点")){
		return;
	}
	loading.style.display="block";
	rmrChildButton.disabled=true;
 	ZKAction.rmrChildNode(path,function(res){
 		rmrChildButton.disabled=false;
		optmsg.innerHTML="rmr child "+res;
		MenuTree.expand(MenuTree.currentNode.id);
		loading.style.display="none";
		MenuTree.expand(MenuTree.currentNode.id);
	});
}

function changeFormat (){
	loading.style.display="block";
	ZKAction.formatJson(nodeValue.value,formatButton.checked ,function(data){
		nodeValue.value=data;
		loading.style.display="none";
	});
}
