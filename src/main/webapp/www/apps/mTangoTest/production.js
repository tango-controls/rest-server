include.set_path('apps');
include.css("all");
include.resources();
include.engines("mTangoDeviceProxy","mTangoUI");
include.plugins("controller","controller/stateful","view","view/helpers","dom/element","model");
include(function(){
include.models("TangoTest");
include.controllers("mTangoTest","DataReadWrite","ExecuteCommand","SampleApplication");
include.views("views/data_read_write/header","views/data_read_write/content","views/data_read_write/footer","views/exec_cmd/header","views/exec_cmd/content","views/exec_cmd/footer","views/smpl_app/header","views/smpl_app/content","views/smpl_app/footer","views/DataReadWrite/poll-attribute","views/ExecuteCommand/execute-cmd");
});
;
include.set_path('engines/mTangoDeviceProxy/apps');
include.plugins("controller","model/jsonp","patches/model","patches/model/jsonp");
include.resources("webtoolkit.base64");
include(function(){
include.models("DeviceProxy","Action");
});
;
include.set_path('jmvc/plugins/controller');
include.plugins("lang","lang/inflector","dom/event","lang/class","lang/openajax","dom/data");
include("delegator","controller");
if(MVC.View){
include.plugins("controller/view");
}
;
include.set_path('jmvc/plugins/lang');
include({path:"standard_helpers.js",shrink_variables:false});
;
include.set_path('jmvc/plugins/lang');
MVC.String={};
MVC.String.strip=function(_1){
return _1.replace(/^\s+/,"").replace(/\s+$/,"");
};
MVC.Function={};
MVC.Function.params=function(_2){
var ps=_2.toString().match(/^[\s\(]*function[^(]*\((.*?)\)/)[1].split(",");
if(ps.length==1&&!ps[0]){
return [];
}
for(var i=0;i<ps.length;i++){
ps[i]=MVC.String.strip(ps[i]);
}
return ps;
};
MVC.Native={};
MVC.Native.extend=function(_5,_6){
if(!MVC[_5]){
MVC[_5]={};
}
var _7=MVC[_5];
for(var _8 in _6){
_7[_8]=_6[_8];
if(!MVC._no_conflict){
window[_5][_8]=_6[_8];
if(typeof _6[_8]=="function"){
var _9=MVC.Function.params(_6[_8]);
if(_9.length==0){
continue;
}
MVC.Native.set_prototype(_5,_8,_6[_8]);
}
}
}
};
MVC.Native.set_prototype=function(_a,_b,_c){
if(!_c){
_c=MVC[_a][_b];
}
window[_a].prototype[_b]=function(){
var _d=[this];
for(var i=0,_f=arguments.length;i<_f;i++){
_d.push(arguments[i]);
}
return _c.apply(this,_d);
};
};
MVC.Native.Object={};
MVC.Native.Object.extend=function(_10,_11){
for(var _12 in _11){
_10[_12]=_11[_12];
}
return _10;
};
MVC.Native.Object.to_query_string=function(_13,_14){
if(typeof _13!="object"){
return _13;
}
return MVC.Native.Object.to_query_string.worker(_13,_14).join("&");
};
MVC.Native.Object.to_query_string.worker=function(obj,_16){
var _17=[];
for(var _18 in obj){
if(obj.hasOwnProperty(_18)){
var _19=obj[_18];
if(_19&&_19.constructor===Date){
_19=_19.getUTCFullYear()+"-"+MVC.Number.to_padded_string(_19.getUTCMonth()+1,2)+"-"+MVC.Number.to_padded_string(_19.getUTCDate(),2)+" "+MVC.Number.to_padded_string(_19.getUTCHours(),2)+":"+MVC.Number.to_padded_string(_19.getUTCMinutes(),2)+":"+MVC.Number.to_padded_string(_19.getUTCSeconds(),2);
}
if(_19 instanceof Array&&_19.length){
var _1a=encodeURIComponent(_16?_16+"["+_18+"]":_18);
for(var i=0;i<_19.length;i++){
var _1c=encodeURIComponent(_19[i].toString());
_17.push(_1a+"="+_1c);
}
}else{
if(typeof _19!="object"){
var _1c=encodeURIComponent(_19.toString());
var _1a=encodeURIComponent(_16?_16+"["+_18+"]":_18);
_17.push(_1a+"="+_1c);
}else{
_17=_17.concat(MVC.Native.Object.to_query_string.worker(_19,_16?_16+"["+_18+"]":_18));
}
}
}
}
return _17;
};
MVC.Native.extend("String",{capitalize:function(s){
return s.charAt(0).toUpperCase()+s.substr(1).toLowerCase();
},include:function(s,_1f){
return s.indexOf(_1f)>-1;
},ends_with:function(s,_21){
var d=s.length-_21.length;
return d>=0&&s.lastIndexOf(_21)===d;
},camelize:function(s){
var _24=s.split(/_|-/);
for(var i=1;i<_24.length;i++){
_24[i]=MVC.String.capitalize(_24[i]);
}
return _24.join("");
},classize:function(s){
var _27=s.split(/_|-/);
for(var i=0;i<_27.length;i++){
_27[i]=MVC.String.capitalize(_27[i]);
}
return _27.join("");
},strip:MVC.String.strip});
MVC.Native.extend("Array",{include:function(a,_2a){
for(var i=0;i<a.length;i++){
if(a[i]==_2a){
return true;
}
}
return false;
}});
MVC.Array.from=function(_2c){
if(!_2c){
return [];
}
var _2d=[];
for(var i=0,_2f=_2c.length;i<_2f;i++){
_2d.push(_2c[i]);
}
return _2d;
};
MVC.Array.is=function(_30){
return Object.prototype.toString.call(a)==="[object Array]";
};
MVC.Native.extend("Function",{bind:function(f,obj){
var _33=MVC.Array.from(arguments);
_33.shift();
_33.shift();
var _34=f,_35=arguments[1];
return function(){
return _34.apply(_35,_33.concat(MVC.Array.from(arguments)));
};
},params:MVC.Function.params});
MVC.Native.extend("Number",{to_padded_string:function(n,len,_38){
var _39=n.toString(_38||10);
var ret="",_3b=len-_39.length;
for(var i=0;i<_3b;i++){
ret+="0";
}
return ret+_39;
}});
MVC.Native.Array=MVC.Array;
MVC.Native.Function=MVC.Function;
MVC.Native.Number=MVC.Number;
MVC.Native.String=MVC.String;
MVC.Object=MVC.Native.Object;
if(!MVC._no_conflict){
Array.from=MVC.Array.from;
}
;
include.set_path('jmvc/plugins/lang/inflector');
include.plugins("lang");
include("inflector");
;
include.set_path('jmvc/plugins/lang/inflector');
MVC.Inflector={Inflections:{plural:[[/(quiz)$/i,"$1zes"],[/^(ox)$/i,"$1en"],[/([m|l])ouse$/i,"$1ice"],[/(matr|vert|ind)ix|ex$/i,"$1ices"],[/(x|ch|ss|sh)$/i,"$1es"],[/([^aeiouy]|qu)y$/i,"$1ies"],[/(hive)$/i,"$1s"],[/(?:([^f])fe|([lr])f)$/i,"$1$2ves"],[/sis$/i,"ses"],[/([ti])um$/i,"$1a"],[/(buffal|tomat)o$/i,"$1oes"],[/(bu)s$/i,"$1ses"],[/(alias|status)$/i,"$1es"],[/(octop|vir)us$/i,"$1i"],[/(ax|test)is$/i,"$1es"],[/s$/i,"s"],[/$/,"s"]],singular:[[/(quiz)zes$/i,"$1"],[/(matr)ices$/i,"$1ix"],[/(vert|ind)ices$/i,"$1ex"],[/^(ox)en/i,"$1"],[/(alias|status)es$/i,"$1"],[/(octop|vir)i$/i,"$1us"],[/(cris|ax|test)es$/i,"$1is"],[/(shoe)s$/i,"$1"],[/(o)es$/i,"$1"],[/(bus)es$/i,"$1"],[/([m|l])ice$/i,"$1ouse"],[/(x|ch|ss|sh)es$/i,"$1"],[/(m)ovies$/i,"$1ovie"],[/(s)eries$/i,"$1eries"],[/([^aeiouy]|qu)ies$/i,"$1y"],[/([lr])ves$/i,"$1f"],[/(tive)s$/i,"$1"],[/(hive)s$/i,"$1"],[/([^f])ves$/i,"$1fe"],[/(^analy)ses$/i,"$1sis"],[/((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$/i,"$1$2sis"],[/([ti])a$/i,"$1um"],[/(n)ews$/i,"$1ews"],[/s$/i,""]],irregular:[["move","moves"],["sex","sexes"],["child","children"],["man","men"],["foreman","foremen"],["person","people"]],uncountable:["sheep","fish","series","species","money","rice","information","equipment"]},pluralize:function(_1){
for(var i=0;i<MVC.Inflector.Inflections.uncountable.length;i++){
var _3=MVC.Inflector.Inflections.uncountable[i];
if(_1.toLowerCase()==_3){
return _3;
}
}
for(var i=0;i<MVC.Inflector.Inflections.irregular.length;i++){
var _4=MVC.Inflector.Inflections.irregular[i][0];
var _5=MVC.Inflector.Inflections.irregular[i][1];
if((_1.toLowerCase()==_4)||(_1==_5)){
return _1.substring(0,1)+_5.substring(1);
}
}
for(var i=0;i<MVC.Inflector.Inflections.plural.length;i++){
var _6=MVC.Inflector.Inflections.plural[i][0];
var _7=MVC.Inflector.Inflections.plural[i][1];
if(_6.test(_1)){
return _1.replace(_6,_7);
}
}
},singularize:function(_8){
for(var i=0;i<MVC.Inflector.Inflections.uncountable.length;i++){
var _a=MVC.Inflector.Inflections.uncountable[i];
if(_8.toLowerCase()==_a){
return _a;
}
}
for(var i=0;i<MVC.Inflector.Inflections.irregular.length;i++){
var _b=MVC.Inflector.Inflections.irregular[i][0];
var _c=MVC.Inflector.Inflections.irregular[i][1];
if((_8.toLowerCase()==_b)||(_8.toLowerCase()==_c)){
return _8.substring(0,1)+_b.substring(1);
}
}
for(var i=0;i<MVC.Inflector.Inflections.singular.length;i++){
var _d=MVC.Inflector.Inflections.singular[i][0];
var _e=MVC.Inflector.Inflections.singular[i][1];
if(_d.test(_8)){
return _8.replace(_d,_e);
}
}
}};
MVC.Native.extend("String",{pluralize:function(_f,_10,_11){
if(typeof _10=="undefined"){
return MVC.Inflector.pluralize(_f);
}else{
return _10+" "+(1==parseInt(_10)?_f:_11||MVC.Inflector.pluralize(_f));
}
},singularize:function(_12,_13){
if(typeof _13=="undefined"){
return MVC.Inflector.singularize(_12);
}else{
return _13+" "+MVC.Inflector.singularize(_12);
}
},is_singular:function(_14){
if(MVC.String.singularize(_14)==null&&_14){
return true;
}
return false;
}});
;
include.set_path('jmvc/plugins/dom/event');
if(typeof Prototype=="undefined"){
include("standard");
}else{
include("prototype_event");
}
;
include.set_path('jmvc/plugins/dom/event');
if(document.addEventListener){
MVC.Event={observe:function(el,_2,_3,_4){
if(_4==null){
_4=false;
}
el.addEventListener(_2,_3,_4);
},stop_observing:function(el,_6,_7,_8){
if(_8==null){
_8=false;
}
el.removeEventListener(_6,_7,false);
}};
}else{
if(document.attachEvent){
MVC.Event={observe:function(_9,_a,_b){
if(MVC.Event._find(_9,_a,_b)!=-1){
return;
}
var _c=function(e){
if(!e){
e=window.event;
}
var _e={_event:e,type:e.type,target:e.srcElement,currentTarget:_9,relatedTarget:_a=="mouseover"?e.fromElement:e.toElement,eventPhase:(e.srcElement==_9)?2:3,clientX:e.clientX,clientY:e.clientY,screenX:e.screenX,screenY:e.screenY,altKey:e.altKey,ctrlKey:e.ctrlKey,shiftKey:e.shiftKey,charCode:e.keyCode,stopPropagation:function(){
this._event.cancelBubble=true;
},preventDefault:function(){
this._event.returnValue=false;
},which:e.which||(e.button&1?1:(e.button&2?3:(e.button&4?2:0)))};
if(Function.prototype.call){
_b.call(_9,_e);
}else{
_9._currentHandler=_b;
_9._currentHandler(_e);
_9._currentHandler=null;
}
};
_9.attachEvent("on"+_a,_c);
var h={element:_9,eventType:_a,handler:_b,wrappedHandler:_c};
var d=_9.document||_9,w=d.parentWindow,id=MVC.Event._uid();
if(!w._allHandlers){
w._allHandlers={};
}
w._allHandlers[id]=h;
if(!_9._handlers){
_9._handlers=[];
}
_9._handlers.push(id);
if(!w._onunloadHandlerRegistered){
w._onunloadHandlerRegistered=true;
w.attachEvent("onunload",MVC.Event._removeAllHandlers);
}
},stop_observing:function(_13,_14,_15){
var i=MVC.Event._find(_13,_14,_15);
if(i==-1){
return;
}
var d=_13.document||_13,w=d.parentWindow,_19=_13._handlers[i],h=w._allHandlers[_19];
_13.detachEvent("on"+_14,h.wrappedHandler);
_13._handlers.splice(i,1);
delete w._allHandlers[_19];
},_find:function(_1b,_1c,_1d){
var _1e=_1b._handlers;
if(!_1e){
return -1;
}
var d=_1b.document||_1b,w=d.parentWindow;
for(var i=_1e.length-1;i>=0;i--){
var h=w._allHandlers[_1e[i]];
if(h.eventType==_1c&&h.handler==_1d){
return i;
}
}
return -1;
},_removeAllHandlers:function(){
var w=this;
for(var id in w._allHandlers){
if(!w._allHandlers.hasOwnProperty(id)){
continue;
}
var h=w._allHandlers[id];
if(h.element){
h.element.detachEvent("on"+h.eventType,h.wrappedHandler);
}
delete w._allHandlers[id];
}
},_counter:0,_uid:function(){
return "h"+MVC.Event._counter++;
}};
}
}
if(!MVC._no_conflict&&typeof Event=="undefined"){
Event=MVC.Event;
}
;
include.set_path('jmvc/plugins/lang/class');
(function(){
var _1=false,_2=/xyz/.test(function(){
xyz;
})?/\b_super\b/:/.*/;
MVC.Class=function(){
};
MVC.Class.extend=function(_3,_4,_5){
if(typeof _3!="string"){
_5=_4;
_4=_3;
_3=null;
}
if(!_5){
_5=_4;
_4=null;
}
_5=_5||{};
var _6=this;
var _7=this.prototype;
_1=true;
var _8=new this();
_1=false;
for(var _9 in _5){
_8[_9]=typeof _5[_9]=="function"&&typeof _7[_9]=="function"&&_2.test(_5[_9])?(function(_a,fn){
return function(){
var _c=this._super;
this._super=_7[_a];
var _d=fn.apply(this,arguments);
this._super=_c;
return _d;
};
})(_9,_5[_9]):_5[_9];
}
function _e(){
if(!_1&&this.init){
this.init.apply(this,arguments);
}
};
_e.prototype=_8;
_e.prototype.Class=_e;
_e.constructor=_e;
for(var _9 in this){
if(this.hasOwnProperty(_9)&&_9!="prototype"){
_e[_9]=this[_9];
}
}
for(var _9 in _4){
_e[_9]=typeof _4[_9]=="function"&&typeof _e[_9]=="function"&&_2.test(_4[_9])?(function(_f,fn){
return function(){
var tmp=this._super;
this._super=_6[_f];
var ret=fn.apply(this,arguments);
this._super=tmp;
return ret;
};
})(_9,_4[_9]):_4[_9];
}
_e.extend=arguments.callee;
if(_3){
_e.className=_3;
}
if(_e.init){
_e.init(_e);
}
if(_6.extended){
_6.extended(_e);
}
return _e;
};
})();
if(!MVC._no_conflict&&typeof Class=="undefined"){
Class=MVC.Class;
}
;
include.set_path('jmvc/plugins/lang/openajax');
if(!window["OpenAjax"]){
OpenAjax=new function(){
var t=true;
var f=false;
var g=window;
var _4="org.openajax.hub.";
var h={};
this.hub=h;
h.implementer="http://openajax.org";
h.implVersion="1.0";
h.specVersion="1.0";
h.implExtraData={};
var _6={};
h.libraries=_6;
h.registerLibrary=function(_7,_8,_9,_a){
_6[_7]={prefix:_7,namespaceURI:_8,version:_9,extraData:_a};
this.publish(_4+"registerLibrary",_6[_7]);
};
h.unregisterLibrary=function(_b){
this.publish(_4+"unregisterLibrary",_6[_b]);
delete _6[_b];
};
h._subscriptions={c:{},s:[]};
h._cleanup=[];
h._subIndex=0;
h._pubDepth=0;
h.subscribe=function(_c,_d,_e,_f,_10){
if(!_e){
_e=window;
}
var _11=_c+"."+this._subIndex;
var sub={scope:_e,cb:_d,fcb:_10,data:_f,sid:this._subIndex++,hdl:_11};
var _13=_c.split(".");
this._subscribe(this._subscriptions,_13,0,sub);
return _11;
};
h.publish=function(_14,_15){
var _16=_14.split(".");
this._pubDepth++;
this._publish(this._subscriptions,_16,0,_14,_15);
this._pubDepth--;
if((this._cleanup.length>0)&&(this._pubDepth==0)){
for(var i=0;i<this._cleanup.length;i++){
this.unsubscribe(this._cleanup[i].hdl);
}
delete (this._cleanup);
this._cleanup=[];
}
};
h.unsubscribe=function(sub){
var _19=sub.split(".");
var sid=_19.pop();
this._unsubscribe(this._subscriptions,_19,0,sid);
};
h._subscribe=function(_1b,_1c,_1d,sub){
var _1f=_1c[_1d];
if(_1d==_1c.length){
_1b.s.push(sub);
}else{
if(typeof _1b.c=="undefined"){
_1b.c={};
}
if(typeof _1b.c[_1f]=="undefined"){
_1b.c[_1f]={c:{},s:[]};
this._subscribe(_1b.c[_1f],_1c,_1d+1,sub);
}else{
this._subscribe(_1b.c[_1f],_1c,_1d+1,sub);
}
}
};
h._publish=function(_20,_21,_22,_23,msg,pcb,_26){
if(typeof _20!="undefined"){
var _27;
if(_22==_21.length){
_27=_20;
}else{
this._publish(_20.c[_21[_22]],_21,_22+1,_23,msg,pcb,_26);
this._publish(_20.c["*"],_21,_22+1,_23,msg,pcb,_26);
_27=_20.c["**"];
}
if(typeof _27!="undefined"){
var _28=_27.s;
var max=_28.length;
for(var i=0;i<max;i++){
if(_28[i].cb){
var sc=_28[i].scope;
var cb=_28[i].cb;
var fcb=_28[i].fcb;
var d=_28[i].data;
var sid=_28[i].sid;
var _30=_28[i].cid;
if(typeof cb=="string"){
cb=sc[cb];
}
if(typeof fcb=="string"){
fcb=sc[fcb];
}
if((!fcb)||(fcb.call(sc,_23,msg,d))){
if((!pcb)||(pcb(_23,msg,_26,_30))){
cb.call(sc,_23,msg,d,sid);
}
}
}
}
}
}
};
h._unsubscribe=function(_31,_32,_33,sid){
if(typeof _31!="undefined"){
if(_33<_32.length){
var _35=_31.c[_32[_33]];
this._unsubscribe(_35,_32,_33+1,sid);
if(_35.s.length==0){
for(var x in _35.c){
return;
}
delete _31.c[_32[_33]];
}
return;
}else{
var _37=_31.s;
var max=_37.length;
for(var i=0;i<max;i++){
if(sid==_37[i].sid){
if(this._pubDepth>0){
_37[i].cb=null;
this._cleanup.push(_37[i]);
}else{
_37.splice(i,1);
}
return;
}
}
}
}
};
h.reinit=function(){
for(var lib in OpenAjax.hub.libraries){
delete OpenAjax.hub.libraries[lib];
}
OpenAjax.hub.registerLibrary("OpenAjax","http://openajax.org/hub","1.0",{});
delete OpenAjax._subscriptions;
OpenAjax._subscriptions={c:{},s:[]};
delete OpenAjax._cleanup;
OpenAjax._cleanup=[];
OpenAjax._subIndex=0;
OpenAjax._pubDepth=0;
};
};
OpenAjax.hub.registerLibrary("OpenAjax","http://openajax.org/hub","1.0",{});
}
OpenAjax.hub.registerLibrary("JavaScriptMVC","http://JavaScriptMVC.com","1.5",{});
;
include.set_path('jmvc/plugins/dom/data');
MVC.Dom={data:function(_1,_2,_3){
_1=_1==window?windowData:_1;
var _4=_1.__mvc;
if(!_4){
_1.__mvc={};
}
if(_3!==undefined){
_1.__mvc[_2]=_3;
}
return _2?_1.__mvc[_2]:_1.__mvc;
},remove_data:function(_5,_6){
_5=_5==window?windowData:_5;
var _7=_5.__mvc;
if(_6){
if(_7){
delete _7[_6];
_6="";
for(_6 in _7){
break;
}
if(!_6){
MVC.Dom.remove_data(_5);
}
}
}else{
try{
delete _5.__mvc;
}
catch(e){
if(_5.removeAttribute){
_5.removeAttribute("__jmvc");
}
}
}
}};
;
include.set_path('jmvc/plugins/controller');
MVC.Delegator=function(_1,_2,f,_4){
this._event=_2;
this._selector=_1;
this._func=f;
this.element=_4||document.documentElement;
MVC.Delegator.jmvc(this.element);
if(_2=="contextmenu"&&MVC.Browser.Opera){
return this.context_for_opera();
}
if(_2=="submit"&&MVC.Browser.IE){
return this.submit_for_ie();
}
if(_2=="change"&&MVC.Browser.IE){
return this.change_for_ie();
}
if(_2=="change"&&MVC.Browser.WebKit){
return this.change_for_webkit();
}
this.add_to_delegator();
};
MVC.Object.extend(MVC.Delegator,{jmvc:function(_5){
var _6=MVC.Dom.data(_5);
if(!_6.delegation_events){
_6.delegation_events={};
}
if(_6.responding==null){
_6.responding=true;
}
return _6;
},add_kill_event:function(_7){
if(!_7.kill){
if(!_7){
_7=window.event;
}
var _8=false;
_7.kill=function(){
_8=true;
try{
if(_7.stopPropagation){
_7.stopPropagation();
}
if(_7.preventDefault){
_7.preventDefault();
}
}
catch(e){
}
};
_7.is_killed=function(){
return _8;
};
_7.stop_propagation=function(){
_8=true;
try{
if(_7.stopPropagation){
_7.stopPropagation();
}
}
catch(e){
}
};
_7.prevent_default=function(){
try{
if(_7.preventDefault){
_7.preventDefault();
}
}
catch(e){
}
};
}
},sort_by_order:function(a,b){
if(a.order<b.order){
return 1;
}
if(b.order<a.order){
return -1;
}
var ae=a._event,be=b._event;
if(ae=="click"&&be=="change"){
return 1;
}
if(be=="click"&&ae=="change"){
return -1;
}
return 0;
},events:{},onload_called:false});
MVC.Event.observe(window,"load",function(){
MVC.Delegator.onload_called=true;
});
MVC.Delegator.prototype={event:function(){
if(MVC.Browser.IE){
if(this._event=="focus"){
return "activate";
}else{
if(this._event=="blur"){
return "deactivate";
}
}
}
return this._event;
},capture:function(){
return MVC.Array.include(["focus","blur"],this._event);
},add_to_delegator:function(_d,_e,_f){
var s=_d||this._selector;
var e=_e||this.event();
var f=_f||this._func;
var _13=MVC.Dom.data(this.element,"delegation_events");
if(!_13[e]||_13[e].length==0){
var _14=MVC.Function.bind(this.dispatch_event,this);
MVC.Event.observe(this.element,e,_14,this.capture());
_13[e]=[];
_13[e]._bind_function=_14;
}
_13[e].push(this);
},_remove_from_delegator:function(_15){
var _16=_15||this.event();
var _17=MVC.Dom.data(this.element,"delegation_events")[_16];
for(var i=0;i<_17.length;i++){
if(_17[i]==this){
_17.splice(i,1);
break;
}
}
if(_17.length==0){
MVC.Event.stop_observing(this.element,_16,_17._bind_function,this.capture());
}
},submit_for_ie:function(){
this.add_to_delegator(null,"click");
this.add_to_delegator(null,"keypress");
this.filters={click:function(el,_1a,_1b){
if(el.nodeName.toUpperCase()=="INPUT"&&el.type.toLowerCase()=="submit"){
for(var e=0;e<_1b.length;e++){
if(_1b[e].tag=="FORM"){
return true;
}
}
}
return false;
},keypress:function(el,_1e,_1f){
if(el.nodeName.toUpperCase()!="INPUT"){
return false;
}
var res=typeof Prototype!="undefined"?(_1e.keyCode==13):(_1e.charCode==13);
if(res){
for(var e=0;e<_1f.length;e++){
if(_1f[e].tag=="FORM"){
return true;
}
}
}
return false;
}};
},change_for_ie:function(){
this.add_to_delegator(null,"click");
this.add_to_delegator(null,"keyup");
this.add_to_delegator(null,"beforeactivate");
this.end_filters={click:function(el,_23){
switch(el.nodeName.toLowerCase()){
case "select":
if(typeof el.selectedIndex=="undefined"){
return false;
}
var _24=MVC.Dom.data(el);
if(_24._change_old_value==null){
_24._change_old_value=el.selectedIndex.toString();
return false;
}else{
if(_24._change_old_value==el.selectedIndex.toString()){
return false;
}
_24._change_old_value=el.selectedIndex.toString();
return true;
}
break;
case "input":
if(el.type.toLowerCase()=="checkbox"){
return true;
}
return false;
}
return false;
},keyup:function(el,_26){
if(el.nodeName.toLowerCase()!="select"){
return false;
}
if(typeof el.selectedIndex=="undefined"){
return false;
}
var _27=MVC.Dom.data(el);
if(_27._change_old_value==null){
_27._change_old_value=el.selectedIndex.toString();
return false;
}else{
if(_27._change_old_value==el.selectedIndex.toString()){
return false;
}
_27._change_old_value=el.selectedIndex.toString();
return true;
}
},beforeactivate:function(el,_29){
return el.nodeName.toLowerCase()=="input"&&el.type.toLowerCase()=="radio"&&!el.checked&&MVC.Delegator.onload_called;
}};
},change_for_webkit:function(){
this.add_to_delegator(null,"change");
this.end_filters={change:function(el,_2b){
if(el.nodeName.toLowerCase()=="input"){
return true;
}
if(typeof el.value=="undefined"){
return false;
}
var old=el.getAttribute("_old_value");
el.setAttribute("_old_value",el.value);
return el.value!=old;
}};
},context_for_opera:function(){
this.add_to_delegator(null,"click");
this.end_filters={click:function(el,_2e){
return _2e.shiftKey;
}};
},regexp_patterns:{tag:/^\s*(\*|[\w\-]+)(\b|$)?/,id:/^#([\w\-\*]+)(\b|$)/,className:/^\.([\w\-\*]+)(\b|$)/},selector_order:function(){
if(this.order){
return this.order;
}
var _2f=this._selector.split(/\s+/);
var _30=this.regexp_patterns;
var _31=[];
if(this._selector){
for(var i=0;i<_2f.length;i++){
var v={},r,p=_2f[i];
for(var _36 in _30){
if(_30.hasOwnProperty(_36)){
if((r=p.match(_30[_36]))){
if(_36=="tag"){
v[_36]=r[1].toUpperCase();
}else{
v[_36]=r[1];
}
p=p.replace(r[0],"");
}
}
}
_31.push(v);
}
}
this.order=_31;
return this.order;
},match:function(el,_38,_39){
if(this.filters&&!this.filters[_38.type](el,_38,_39)){
return null;
}
var _3a=0;
var _3b=this.selector_order();
if(_3b.length==0){
return {node:_39[0].element,order:0,delegation_event:this};
}
for(var n=0;n<_39.length;n++){
var _3d=_39[n],_3e=_3b[_3a],_3f=true;
for(var _40 in _3e){
if(!_3e.hasOwnProperty(_40)||_40=="element"){
continue;
}
if(_3e[_40]&&_40=="className"){
if(!MVC.Array.include(_3d.className.split(" "),_3e[_40])){
_3f=false;
}
}else{
if(_3e[_40]&&_3d[_40]!=_3e[_40]){
_3f=false;
}
}
}
if(_3f){
_3a++;
if(_3a>=_3b.length){
if(this.end_filters&&!this.end_filters[_38.type](el,_38)){
return null;
}
return {node:_3d.element,order:n,delegation_event:this};
}
}
}
return null;
},dispatch_event:function(_41){
var _42=_41.target,_43=false,_44=true,_45=[];
var _46=MVC.Dom.data(this.element,"delegation_events")[_41.type];
var _47=this.node_path(_42);
for(var i=0;i<_46.length;i++){
var _49=_46[i];
var _4a=_49.match(_42,_41,_47);
if(_4a){
_45.push(_4a);
}
}
if(_45.length==0){
return true;
}
MVC.Delegator.add_kill_event(_41);
_45.sort(MVC.Delegator.sort_by_order);
var _4b;
for(var m=0;m<_45.length;m++){
_4b=_45[m];
_44=_4b.delegation_event._func({event:_41,element:MVC.$E(_4b.node),delegate:this.element})&&_44;
if(_41.is_killed()){
return false;
}
}
},node_path:function(el){
var _4e=this.element,_4f=[],_50=el;
if(_50==_4e){
return [{tag:_50.nodeName,className:_50.className,id:_50.id,element:_50}];
}
do{
_4f.unshift({tag:_50.nodeName,className:_50.className,id:_50.id,element:_50});
}while(((_50=_50.parentNode)!=_4e)&&_50);
if(_50){
_4f.unshift({tag:_50.nodeName,className:_50.className,id:_50.id,element:_50});
}
return _4f;
},destroy:function(){
if(this._event=="contextmenu"&&MVC.Browser.Opera){
return this._remove_from_delegator("click");
}
if(this._event=="submit"&&MVC.Browser.IE){
this._remove_from_delegator("keypress");
return this._remove_from_delegator("click");
}
if(this._event=="change"&&MVC.Browser.IE){
this._remove_from_delegator("keyup");
this._remove_from_delegator("beforeactivate");
return this._remove_from_delegator("click");
}
this._remove_from_delegator();
}};
;
include.set_path('jmvc/plugins/controller');
MVC.Object.is_number=function(o){
return o&&(typeof o=="number"||(typeof o=="string"&&!isNaN(o)));
};
MVC.Controller=MVC.Class.extend({init:function(){
if(!this.className){
return;
}
this.singularName=MVC.String.singularize(this.className);
if(!MVC.Controller.controllers[this.className]){
MVC.Controller.controllers[this.className]=[];
}
MVC.Controller.controllers[this.className].unshift(this);
var _2,_3;
if(!this.modelName){
this.modelName=MVC.String.is_singular(this.className)?this.className:MVC.String.singularize(this.className);
}
if(this._should_attach_actions){
this._create_actions();
}
if(include.get_env()=="test"){
var _4=MVC.root.join("test/functional/"+this.className+"_controller_test.js");
var _5=include.check_exists(_4);
if(_5){
MVC.Console.log("Loading: \"test/functional/"+this.className+"_controller_test.js\"");
include("../test/functional/"+this.className+"_controller_test.js");
}else{
MVC.Console.log("Test Controller not found at \"test/functional/"+this.className+"_controller_test.js\"");
}
}
this._path=include.get_path().match(/(.*?)controllers/)[1]+"controllers";
},_should_attach_actions:true,_create_actions:function(){
this.actions={};
for(var _6 in this.prototype){
val=this.prototype[_6];
if(typeof val=="function"&&_6!="Class"){
for(var a=0;a<MVC.Controller.actions.length;a++){
act=MVC.Controller.actions[a];
if(act.matches(_6)){
var _8=this.dispatch_closure(_6);
this.actions[_6]=new act(_6,_8,this.className,this._element,this._events);
}
}
}
}
},dispatch_closure:function(_9){
return MVC.Function.bind(function(_a){
_a=_a||{};
_a.action=_9;
_a.controller=this;
_a=_a.constructor==MVC.Controller.Params?_a:new MVC.Controller.Params(_a);
return this.dispatch(_9,_a);
},this);
},dispatch:function(_b,_c){
if(!_b){
_b="index";
}
if(typeof _b=="string"){
if(!(_b in this.prototype)){
throw "No action named "+_b+" was found for "+this.Class.className+" controller.";
}
}else{
_b=_b.name;
}
var _d=this._get_instance(_b,_c);
return this._dispatch_action(_d,_b,_c);
},_get_instance:function(_e,_f){
return new this(_e,_f);
},_dispatch_action:function(_10,_11,_12){
if(!this._listening){
return;
}
_10.params=_12;
_10.action_name=_11;
return _10[_11](_12);
},controllers:{},actions:[],publish:function(_13,_14){
OpenAjax.hub.publish(_13,_14);
},get_controller_with_name_and_action:function(_15,_16){
var _17=MVC.Controller.controllers[_15];
if(!_17){
return null;
}
for(var i=0;i<_17.length;i++){
var _19=_17[i];
if(_19.prototype[_16]){
return _19;
}
}
return null;
},modelName:null,_listening:true,_events:MVC.Delegator.events,_element:document.documentElement},{continue_to:function(_1a){
var _1b=MVC.Array.from(arguments);
var _1a=_1b.shift();
if(typeof this[_1a]!="function"){
throw "There is no action named "+_1a+". ";
}
return MVC.Function.bind(function(){
this.action_name=_1a;
this[_1a].apply(this,_1b.concat(MVC.Array.from(arguments)));
},this);
},delay:function(_1c,_1d,_1e){
if(typeof this[_1d]!="function"){
throw "There is no action named "+_1d+". ";
}
return setTimeout(MVC.Function.bind(function(){
this.Class._dispatch_action(this,_1d,_1e);
},this),_1c);
},publish:function(_1f,_20){
this.Class.publish(_1f,_20);
}});
MVC.Controller.Action=MVC.Class.extend({init:function(){
if(this.matches){
MVC.Controller.actions.push(this);
}
}},{init:function(_21,_22,_23,_24){
this.action=_21;
this.callback=_22;
this.className=_23;
this.element=_24;
},destroy:function(){
}});
MVC.Controller.Action.Subscribe=MVC.Controller.Action.extend({match:new RegExp("(.*?)\\s?(subscribe)$"),matches:function(_25){
return this.match.exec(_25);
}},{init:function(_26,_27,_28,_29){
this._super(_26,_27,_28,_29);
this.message();
this.subscription=OpenAjax.hub.subscribe(this.message_name,MVC.Function.bind(this.subscribe,this));
},message:function(){
this.parts=this.action.match(this.Class.match);
this.message_name=this.parts[1];
},subscribe:function(_2a,_2b){
var _2c=_2b||{};
_2c.event_name=_2a;
this.callback(_2c);
},destroy:function(){
OpenAjax.hub.unsubscribe(this.subscription);
this._super();
}});
MVC.Controller.Action.Event=MVC.Controller.Action.extend({match:new RegExp("^(?:(.*?)\\s)?(change|click|contextmenu|dblclick|keydown|keyup|keypress|mousedown|mousemove|mouseout|mouseover|mouseup|reset|resize|scroll|select|submit|dblclick|focus|blur|load|unload)$"),matches:function(_2d){
return this.match.exec(_2d);
}},{init:function(_2e,_2f,_30,_31){
this._super(_2e,_2f,_30,_31);
this.css_and_event();
var _32=this.selector();
if(_32!=null){
this.delegator=new MVC.Delegator(_32,this.event_type,_2f,_31);
}
},css_and_event:function(){
this.parts=this.action.match(this.Class.match);
this.css=this.parts[1]||"";
this.event_type=this.parts[2];
},main_controller:function(){
if(!this.css&&MVC.Array.include(["blur","focus"],this.event_type)){
MVC.Event.observe(window,this.event_type,MVC.Function.bind(function(_33){
this.callback({event:_33,element:window});
},this));
return;
}
return this.css;
},plural_selector:function(){
if(this.css=="#"||this.css.substring(0,2)=="# "){
var _34=this.css.substring(2,this.css.length);
if(this.element==document.documentElement){
return "#"+this.className+(_34?" "+_34:"");
}else{
return (_34?" "+_34:"");
}
}else{
return "."+MVC.String.singularize(this.className)+(this.css?" "+this.css:"");
}
},singular_selector:function(){
if(this.element==document.documentElement){
return "#"+this.className+(this.css?" "+this.css:"");
}else{
return this.css;
}
},selector:function(){
if(MVC.Array.include(["load","unload","resize","scroll"],this.event_type)){
MVC.Event.observe(window,this.event_type,MVC.Function.bind(function(_35){
this.callback({event:_35,element:window});
},this));
return;
}
if(this.className=="main"){
this.css_selector=this.main_controller();
}else{
this.css_selector=MVC.String.is_singular(this.className)?this.singular_selector():this.plural_selector();
}
return this.css_selector;
},destroy:function(){
if(this.delegator){
this.delegator.destroy();
}
this._super();
}});
MVC.Controller.Params=function(_36){
var _36=_36||{};
var _37=false;
this.kill=function(){
_37=true;
if(_36.event&&_36.event.kill){
_36.event.kill();
}
};
this.is_killed=function(){
return _36.event.is_killed?_36.event.is_killed():_37;
};
for(var _38 in _36){
if(_36.hasOwnProperty(_38)){
this[_38]=_36[_38];
}
}
this.constructor=MVC.Controller.Params;
};
MVC.Controller.Params.prototype={form_params:function(){
var _39={};
if(this.element.nodeName.toLowerCase()!="form"){
return _39;
}
var els=this.element.elements,_3b=[];
for(var i=0;i<els.length;i++){
var el=els[i];
if(el.type.toLowerCase()=="submit"){
continue;
}
var key=el.name||el.id,_3f=key.match(/(\w+)/g),_40;
if(!key){
continue;
}
switch(el.type.toLowerCase()){
case "checkbox":
case "radio":
_40=!!el.checked;
break;
default:
_40=el.value;
break;
}
if(_3f.length>1){
var _41=_3f.length-1;
var _42=_3f[0].toString();
if(!_39[_42]){
_39[_42]={};
}
var _43=_39[_42];
for(var k=1;k<_41;k++){
_42=_3f[k];
if(!_43[_42]){
_43[_42]={};
}
_43=_43[_42];
}
_43[_3f[_41]]=_40;
}else{
if(key in _39){
if(typeof _39[key]=="string"){
_39[key]=[_39[key]];
}
_39[key].push(_40);
}else{
_39[key]=_40;
}
}
}
return _39;
},class_element:function(){
var _45=this.element;
var _46=this._className();
var _47=function(el){
var _49=el.className.split(" ");
for(var i=0;i<_49.length;i++){
if(_49[i]==_46){
return true;
}
}
return false;
};
while(_45&&!_47(_45)){
_45=_45.parentNode;
if(_45==document){
return null;
}
}
return MVC.$E(_45);
},is_event_on_element:function(){
return this.event.target==this.element;
},_className:function(){
return this.controller.singularName;
},element_instance:function(){
var ce,_4c,_4d,_4e=this.controller.modelName,id,_50=new RegExp("^"+_4e+"_(.*)$");
if(!(_4d=MVC.Model.models[_4e])){
throw "No model for the "+this.controller.className+" controller!";
}
ce=this.class_element();
return Model._find_by_element(ce,_4e,_4d);
}};
if(!MVC._no_conflict&&typeof Controller=="undefined"){
Controller=MVC.Controller;
}
;
include.set_path('jmvc/plugins/model/jsonp');
include.plugins("model","lang/date","io/jsonp");
include("remote_model");
;
include.set_path('jmvc/plugins/model');
include.plugins("lang/class","lang/openajax");
include("simple_store");
include("model");
;
include.set_path('jmvc/plugins/model');
MVC.Store=MVC.Class.extend({init:function(_1){
this._data={};
this.storing_class=_1;
},find_one:function(id){
return id?this._data[id]:null;
},create:function(_3){
var id=_3[_3.Class.id];
this._data[id]=_3;
},destroy:function(id){
delete this._data[id];
},find:function(f){
var _7=[];
for(var id in this._data){
var _9=this._data[id];
if(!f||f(_9)){
_7.push(_9);
}
}
return _7;
},clear:function(){
this._data={};
},is_empty:function(){
return !this.find().length;
}});
;
include.set_path('jmvc/plugins/model');
MVC.Model=MVC.Class.extend({store_type:MVC.Store,init:function(){
if(!this.className){
return;
}
MVC.Model.models[this.className]=this;
this.store=new this.store_type(this);
},find:function(id,_2,_3){
if(!_2){
_2={};
}
if(typeof _2=="function"){
_3=_2;
_2={};
}
if(id=="all"){
return this.create_many_as_existing(this.find_all(_2,_3));
}else{
if(!_2[this.id]&&id!="first"){
_2[this.id]=id;
}
return this.create_as_existing(this.find_one(id=="first"?null:_2,_3));
}
},asynchronous:true,create_as_existing:function(_4){
if(!_4){
return null;
}
if(_4.attributes){
_4=_4.attributes();
}
var _5=new this(_4);
_5.is_new_record=this.new_record_func;
this.publish("create.as_existing",{data:_5});
return _5;
},create_many_as_existing:function(_6){
if(!_6){
return null;
}
var _7=[];
for(var i=0;i<_6.length;i++){
_7.push(this.create_as_existing(_6[i]));
}
return _7;
},id:"id",new_record_func:function(){
return false;
},validations:[],has_many:function(){
for(var i=0;i<arguments.length;i++){
this._associations.push(arguments[i]);
}
},belong_to:function(){
for(var i=0;i<arguments.length;i++){
this._associations.push(arguments[i]);
}
},_associations:[],element_id_to_id:function(_b){
var re=new RegExp(this.className+"_","i");
return _b.replace(re,"");
},find_by_element:function(el){
return this._find_by_element(MVC.$E(el),this.className,this);
},_find_by_element:function(ce,_f,_10){
var _11,id,_13=new RegExp("^"+_f+"_(.*)$");
if(ce&&ce.id&&(_11=ce.id.match(_13))&&_11.length>1){
id=_11[1];
}else{
id=ce.has_class(_13)[1];
}
return _10.store.find_one(id);
},add_attribute:function(_14,_15){
if(!this.attributes[_14]){
this.attributes[_14]=_15;
}
if(!this.default_attributes[_14]){
this.default_attributes[_14]=null;
}
},attributes:{},default_attributes:{},_clean_callbacks:function(_16){
if(!_16){
_16=function(){
};
}
if(typeof _16=="function"){
return {onSuccess:_16,onFailure:_16};
}
if(!_16.onSuccess&&!_16.onComplete){
throw "You must supply a positive callback!";
}
if(!_16.onSuccess){
_16.onSuccess=_16.onComplete;
}
if(!_16.onFailure&&_16.onComplete){
_16.onFailure=_16.onComplete;
}
return _16;
},models:{},callback:function(_17){
var f=typeof _17=="string"?this[_17]:_17;
var _19=MVC.Array.from(arguments);
_19.shift();
_19.unshift(f,this);
return MVC.Function.bind.apply(null,_19);
},publish:function(_1a,_1b){
OpenAjax.hub.publish(this.className+"."+_1a,_1b);
},namespace:null},{init:function(_1c){
this.errors=[];
this.set_attributes(this.Class.default_attributes||{});
this.set_attributes(_1c);
},set_attributes:function(_1d){
for(var key in _1d){
if(_1d.hasOwnProperty(key)){
this._setAttribute(key,_1d[key]);
}
}
return _1d;
},update_attributes:function(_1f,_20){
this.set_attributes(_1f);
return this.save(_20);
},valid:function(){
return this.errors.length==0;
},validate:function(){
},_setAttribute:function(_21,_22){
if(MVC.Array.include(this.Class._associations,_21)){
this._setAssociation(_21,_22);
}else{
this._setProperty(_21,_22);
}
},_setProperty:function(_23,_24){
if(this["set_"+_23]&&!this["set_"+_23](_24)){
return;
}
var old=this[_23];
this[_23]=MVC.Array.include(["created_at","updated_at"],_23)?MVC.Date.parse(_24):_24;
if(_23==this.Class.id&&this[_23]){
this.is_new_record=this.Class.new_record_func;
if(this.Class.store){
if(!old){
this.Class.store.create(this);
}else{
if(old!=this[_23]){
this.Class.store.destroy(old);
this.Class.store.create(this);
}
}
}
}
this.Class.add_attribute(_23,MVC.Object.guess_type(_24));
},_setAssociation:function(_26,_27){
this[_26]=function(){
if(!MVC.String.is_singular(_26)){
_26=MVC.String.singularize(_26);
}
var _28=window[MVC.String.classize(_26)];
if(!_28){
return _27;
}
return _28.create_many_as_existing(_27);
};
},attributes:function(){
var _29={};
var cas=this.Class.attributes;
for(var _2b in cas){
if(cas.hasOwnProperty(_2b)){
_29[_2b]=this[_2b];
}
}
return _29;
},is_new_record:function(){
return true;
},save:function(_2c){
var _2d;
this.errors=[];
this.validate();
if(!this.valid()){
return false;
}
_2d=this.is_new_record()?this.Class.create(this.attributes(),_2c):this.Class.update(this[this.Class.id],this.attributes(),_2c);
this.is_new_record=this.Class.new_record_func;
return true;
},destroy:function(_2e){
this.Class.destroy(this[this.Class.id],_2e);
this.Class.store.destroy(this[this.Class.id]);
},add_errors:function(_2f){
if(_2f){
this.errors=this.errors.concat(_2f);
}
},_resetAttributes:function(_30){
this._clear();
},_clear:function(){
var cas=this.Class.default_attributes;
for(var _32 in cas){
if(cas.hasOwnProperty(_32)){
this[_32]=null;
}
}
},element_id:function(){
return this.Class.className+"_"+this[this.Class.id];
},element:function(){
return MVC.$E(this.element_id());
},elements:function(){
return MVC.Query("."+this.element_id());
},publish:function(_33,_34){
this.Class.publish(_33,_34||{data:this});
},callback:function(_35){
var f=typeof _35=="string"?this[_35]:_35;
var _37=MVC.Array.from(arguments);
_37.shift();
_37.unshift(f,this);
return MVC.Function.bind.apply(null,_37);
}});
MVC.Object.guess_type=function(_38){
if(typeof _38!="string"){
if(_38==null){
return typeof _38;
}
if(_38.constructor==Date){
return "date";
}
if(_38.constructor==Array){
return "array";
}
return typeof _38;
}
if(_38=="true"||_38=="false"){
return "boolean";
}
if(!isNaN(_38)){
return "number";
}
return typeof _38;
};
if(!MVC._no_conflict&&typeof Model=="undefined"){
Model=MVC.Model;
}
;
include.set_path('jmvc/plugins/lang/date');
(function(){
var _1=Date.parse;
MVC.Native.extend("Date",{add_days:function(_2,_3){
_2.setDate(_2.getDate()+_3);
return _2;
},add_weeks:function(_4,_5){
return MVC.Date.add_days(_4,_5*7);
},day_name:function(_6){
return MVC.Date.day_names[_6.getDay()];
},first_day_of_week:function(_7){
var _8=new Date(_7);
_8.setDate(_7.getDate()-_7.getDay());
return _8;
},month_name:function(_9){
return MVC.Date.month_names[_9.getMonth()];
},number_of_days_in_month:function(_a){
var _b=_a.getFullYear(),_c=_a.getMonth(),m=[31,28,31,30,31,30,31,31,30,31,30,31];
if(_c!=1){
return m[_c];
}
if(_b%4!=0||(_b%100==0&&_b%400!=0)){
return m[1];
}
return m[1]+1;
},day_names:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],month_names:["January","February","March","April","May","June","July","August","September","October","November","December"],parse:function(_e){
if(typeof _e!="string"){
return null;
}
var f1=/\d{4}-\d{1,2}-\d{1,2}/,f2=/\d{4}\/\d{1,2}\/\d{1,2}/,f3=/(\d{4})[-\/](\d{1,2})[-\/](\d{1,2})\s(\d{1,2}):(\d{1,2}):(\d{1,2})/;
var _12;
if((_12=_e.match(f3))){
return new Date(Date.UTC(parseInt(_12[1],10),(parseInt(_12[2],10)-1),parseInt(_12[3],10),parseInt(_12[4],10),parseInt(_12[5],10),parseInt(_12[6],10)));
}
if(_e.match(f1)){
var _13=_e.match(f1)[0].split("-");
return new Date(Date.UTC(parseInt(_13[0],10),(parseInt(_13[1],10)-1),parseInt(_13[2],10)));
}
if(_e.match(f2)){
var _13=_e.match(f2)[0].split("/");
return new Date(Date.UTC(parseInt(_13[0],10),(parseInt(_13[1],10)-1),parseInt(_13[2],10)));
}
return _1(_e);
}});
})();
MVC.Native.Date=MVC.Date;
;
include.set_path('jmvc/plugins/io/jsonp');
include.plugins("lang");
include("jsonp");
;
include.set_path('jmvc/plugins/io/jsonp');
MVC.JsonP=function(_1,_2){
this.url=_1;
this.options=_2||{};
this.remove_script=this.options.remove_script==false?false:true;
this.options.parameters=this.options.parameters||{};
this.error_timeout=this.options.error_timeout*1000||1000*70;
this.send();
};
MVC.JsonP.prototype={send:function(){
var n="c"+MVC.get_random(5);
if(this.options.session){
var _4=typeof this.options.session=="function"?this.options.session():this.options.session;
this.url+=(MVC.String.include(this.url,";")?"&":";")+MVC.Object.to_query_string(_4);
}
var _5=typeof this.options.parameters=="function"?this.options.parameters():this.options.parameters;
this.url+=(MVC.String.include(this.url,"?")?"&":"?")+MVC.Object.to_query_string(_5);
this.add_method();
var _6=this.callback_and_random(n);
var _7=this.check_error(this.url,this.options.onFailure);
MVC.JsonP._cbs[_6]=MVC.Function.bind(function(_8){
clearTimeout(_7);
this.remove_scripts();
var _9={};
if(_8==null){
_9.responseText="";
}else{
if(typeof _8=="string"){
_9.responseText=_8;
}else{
_9=_8;
_9.responseText=_8.toString();
}
}
var _a=true;
if(this.options.onSuccess){
_a=this.options.onSuccess(_9);
}
if(this.options.onComplete&&_a){
this.options.onComplete(_9);
}
delete MVC.JsonP._cbs[_6];
},this);
include({path:this.url});
},add_method:function(){
if(this.options.method&&this.options.method!="get"){
this.url+="&_method="+this.options.method;
}
},callback_and_random:function(n){
this.options.callback="MVC.JsonP._cbs."+n;
this.url+="&callback="+this.options.callback;
return n;
},check_error:function(_c,_d){
return setTimeout(function(){
if(_d){
_d(_c);
}else{
throw "URL:"+_c+" timedout!";
}
},this.error_timeout);
},remove_scripts:function(){
if(this.remove_script){
setTimeout(MVC.Function.bind(this._remove_scripts,this),2000);
}
},_remove_scripts:function(){
var _e=document.getElementsByTagName("script");
var _f=new RegExp(this.url);
for(var s=0;s<_e.length;s++){
var _11=_e[s];
if(MVC.String.include(_11.src.toLowerCase(),this.url.toLowerCase())){
_11.parentNode.removeChild(_11);
}
}
}};
MVC.JsonP._cbs={};
MVC.IO.JsonP=MVC.JsonP;
;
include.set_path('jmvc/plugins/model/jsonp');
MVC.Model.JsonP=MVC.Model.extend({error_timeout:4000,init:function(){
if(!this.className){
return;
}
if(!this.domain){
throw ("a domain must be provided for remote model");
}
if(!this.controller_name){
this.controller_name=this.className;
}
this.plural_controller_name=MVC.String.pluralize(this.controller_name);
this._super();
},find_all:function(_1,_2){
var _3=this._clean_callbacks(_2);
var _4=_3.onSuccess;
var _5=_3.onFailure;
var n=parseInt(Math.random()*100000);
var _7=this.find_url?this.find_url+"?":this.domain+"/"+this.plural_controller_name+".json?";
if(!_4){
_4=(function(){
});
}
new MVC.JsonP(_7,{parameters:_1,onFailure:_2.onFailure,onSuccess:MVC.Function.bind(function(_8){
var _9=this.create_many_as_existing(_8);
_4(_9);
},this),method:"get"});
},create:function(_a,_b){
var _c=this._clean_callbacks(_b);
var _d=_c.onSuccess;
this.add_standard_params(_a,"create");
var _e=this,_f=this.className,url=this.create_url?this.create_url+"?":this.domain+"/"+this.plural_controller_name+".json?";
var tll=this.top_level_length(_a,url);
var _12=this.seperate(_a[this.controller_name],tll,this.controller_name);
var _13=_12.postpone,_14=_12.send;
if(!_d){
_d=(function(){
});
}
_a["_method"]="POST";
if(_12.send_in_parts){
_a[this.controller_name]=_14;
_a["_mutlirequest"]="true";
new MVC.JsonP(url,{parameters:_a,onComplete:MVC.Function.bind(this.parts_create_callback(_a,_d,_13),this),onFailure:_d.onFailure,method:"post"});
}else{
_a["_mutlirequest"]=null;
new MVC.JsonP(url,{parameters:_a,onComplete:MVC.Function.bind(this.single_create_callback(_d),this),onFailure:_d.onFailure,method:"post"});
}
},parts_create_callback:function(_15,_16,_17){
return function(_18){
if(!_18.id){
throw "Your server must callback with the id of the object.  It is used for the next request";
}
_15[this.controller_name]=_17;
_15.id=_18.id;
this.create(_15,_16);
};
},single_create_callback:function(_19){
return function(_1a){
if(_1a[this.className]){
var _1b=new this(_1a[this.className]);
_1b.add_errors(_1a.errors);
_19(_1b);
}else{
_19(new this(_1a));
}
};
},add_standard_params:function(_1c,_1d){
if(!_1c.referer){
_1c.referer=window.location.href;
}
},callback_name:"callback",domain:null,top_level_length:function(_1e,url){
var p=MVC.Object.extend({},_1e);
delete p[this.controller_name];
return url.length+MVC.Object.to_query_string(p).length;
},seperate:function(_21,_22,_23){
var _24=2000-9-_22;
var _25={};
var _26={};
var _27=false;
for(var _28 in _21){
if(!_21.hasOwnProperty(_28)){
continue;
}
var _29=_21[_28],_2a;
var _2b=encodeURIComponent(_23+"["+_28+"]").length;
if(typeof _29=="string"){
_2a=encodeURIComponent(_29).length;
}else{
_2a=_29.toString().length;
}
if(_24-_2b<=30){
_26[_28]=_29;
_27=true;
continue;
}
_24=_24-_2b-2;
if(_24>_2a){
_25[_28]=_29;
_24-=_2a;
}else{
if(typeof _29=="string"){
var _2c=_24;
while(encodeURIComponent(_29.substr(0,_2c)).length>_24){
_2c=parseInt(_2c*0.75)-1;
}
_25[_28]=_29.substr(0,_2c);
_26[_28]=_29.substr(_2c);
_27=true;
_24=0;
}else{
_26[_28]=_29;
}
}
}
return {send:_25,postpone:_26,send_in_parts:_27};
},random:parseInt(Math.random()*1000000)},{});
;
include.set_path('jmvc/plugins/patches/model');
include.plugin("model");
include("model_patch");
;
include.set_path('jmvc/plugins/patches/model');
MVC.Model.prototype._setAttribute=function(_1,_2){
if(MVC.Array.include(this.Class._associations,this.Class.attributes[_1])){
this._setAssociation(_1,_2);
}else{
this._setProperty(_1,_2);
}
};
MVC.Model.prototype._setAssociation=function(_3,_4){
var me=this;
me[_3]=(function(_6,_7){
var _8=me.Class.attributes[_6];
if(!MVC.String.is_singular(_8)){
_8=MVC.String.singularize(_8);
}
var _9=window[_8];
if(!_9){
return _7;
}
return _9.create_as_existing(_7);
})(_3,_4);
};
MVC.Model.prototype.declared_attributes=function(){
var _a={};
var _b=this.Class.attributes;
for(var _c in _b){
if(_b.hasOwnProperty(_c)){
if(MVC.Array.include(this.Class._associations,this.Class.attributes[_c])){
_a[_c]=this[_c].attributes();
}else{
_a[_c]=this[_c];
}
}
}
return _a;
};
;
include.set_path('jmvc/plugins/patches/model/jsonp');
include.plugin("model/jsonp");
include("remote_model_patch");
;
include.set_path('jmvc/plugins/patches/model/jsonp');
MVC.Model.JsonP.add_standard_params=function(_1,_2){
if(!_1.referer){
_1.referer=window.location.href;
}
if(!_1.action){
_1.action=_2;
}
};
;
include.set_path('engines/mTangoDeviceProxy/resources');
var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(_1){
var _2="";
var _3,_4,_5,_6,_7,_8,_9;
var i=0;
_1=Base64._utf8_encode(_1);
while(i<_1.length){
_3=_1.charCodeAt(i++);
_4=_1.charCodeAt(i++);
_5=_1.charCodeAt(i++);
_6=_3>>2;
_7=((_3&3)<<4)|(_4>>4);
_8=((_4&15)<<2)|(_5>>6);
_9=_5&63;
if(isNaN(_4)){
_8=_9=64;
}else{
if(isNaN(_5)){
_9=64;
}
}
_2=_2+this._keyStr.charAt(_6)+this._keyStr.charAt(_7)+this._keyStr.charAt(_8)+this._keyStr.charAt(_9);
}
return _2;
},decode:function(_b){
var _c="";
var _d,_e,_f;
var _10,_11,_12,_13;
var i=0;
_b=_b.replace(/[^A-Za-z0-9\+\/\=]/g,"");
while(i<_b.length){
_10=this._keyStr.indexOf(_b.charAt(i++));
_11=this._keyStr.indexOf(_b.charAt(i++));
_12=this._keyStr.indexOf(_b.charAt(i++));
_13=this._keyStr.indexOf(_b.charAt(i++));
_d=(_10<<2)|(_11>>4);
_e=((_11&15)<<4)|(_12>>2);
_f=((_12&3)<<6)|_13;
_c=_c+String.fromCharCode(_d);
if(_12!=64){
_c=_c+String.fromCharCode(_e);
}
if(_13!=64){
_c=_c+String.fromCharCode(_f);
}
}
_c=Base64._utf8_decode(_c);
return _c;
},_utf8_encode:function(_15){
_15=_15.replace(/\r\n/g,"\n");
var _16="";
for(var n=0;n<_15.length;n++){
var c=_15.charCodeAt(n);
if(c<128){
_16+=String.fromCharCode(c);
}else{
if((c>127)&&(c<2048)){
_16+=String.fromCharCode((c>>6)|192);
_16+=String.fromCharCode((c&63)|128);
}else{
_16+=String.fromCharCode((c>>12)|224);
_16+=String.fromCharCode(((c>>6)&63)|128);
_16+=String.fromCharCode((c&63)|128);
}
}
}
return _16;
},_utf8_decode:function(_19){
var _1a="";
var i=0;
var c=c1=c2=0;
while(i<_19.length){
c=_19.charCodeAt(i);
if(c<128){
_1a+=String.fromCharCode(c);
i++;
}else{
if((c>191)&&(c<224)){
c2=_19.charCodeAt(i+1);
_1a+=String.fromCharCode(((c&31)<<6)|(c2&63));
i+=2;
}else{
c2=_19.charCodeAt(i+1);
c3=_19.charCodeAt(i+2);
_1a+=String.fromCharCode(((c&15)<<12)|((c2&63)<<6)|(c3&63));
i+=3;
}
}
}
return _1a;
}};
;
include.next_function();
include.set_path('engines/mTangoDeviceProxy/models');
DeviceProxy=MVC.Model.extend("DeviceProxy",{onComplete:function(_1){
},onFailure:function(_2){
alert(_2.error);
}},{url:null,set_url:function(v){
this.url=v;
},_sendRequest:function(_4,_5){
var _6=_5.onComplete||this.Class.onComplete;
var _7=_5.onFailure||this.Class.onFailure;
var _8=this.url;
var _5={};
_5.cmd=Base64.encode(_4.toJson());
var me=this;
new MVC.JsonP(_8,{parameters:_5,onComplete:function(_a){
if(_a.error){
_7(_a);
}else{
_6(_a);
}
},onFailure:_7,method:"get"});
},readAttribute:function(_b,_c){
var _d=Action.createReadAction(_b);
this._sendRequest(_d,_c);
},writeAttribute:function(_e,_f){
var cmd=Action.createWriteAction(_e,_f.argin);
this._sendRequest(cmd,_f);
},executeCommand:function(_11,_12){
var cmd=Action.createExecAction(_11,_12.argin||null);
this._sendRequest(cmd,_12);
},state:function(_14){
return this.readAttribute("State",_14);
}});
;
include.set_path('engines/mTangoDeviceProxy/models');
Action=MVC.Model.extend("Action",{attributes:{type:"string",target:"string",argin:"object"},createReadAction:function(_1){
return new Action({type:"read",target:_1,argin:null});
},createWriteAction:function(_2,_3){
return new Action({type:"write",target:_2,argin:_3});
},createExecAction:function(_4,_5){
return new Action({type:"execute",target:_4,argin:_5});
}},{toJson:function(){
return JSON.stringify(this.attributes());
}});
;
include.set_path('engines/mTangoUI/apps');
include.resources();
include.engines();
include.plugins("view","view/helpers","model");
include(function(){
include.models("Page");
include.controllers();
include.views("views/empty_page");
});
;
include.set_path('jmvc/plugins/view');
include.plugins("lang");
include("view");
if(include.get_env()=="development"){
include("fulljslint");
}
if(MVC.Controller){
include.plugins("controller/view");
}
;
include.set_path('jmvc/plugins/view');
MVC.View=function(_1){
this.set_options(_1);
if(_1.precompiled){
this.template={};
this.template.process=_1.precompiled;
MVC.View.update(this.name,this);
return;
}
if(_1.url||_1.absolute_url||_1.view_url){
this.name=this.name?this.name:_1.url||_1.absolute_url||"views/"+_1.view_url;
var _2=_1.absolute_url||(_1.url?MVC.root.join(_1.url+(_1.url.match(this.extMatch)?"":this.ext)):MVC.root.join("views/"+_1.view_url+(_1.view_url.match(this.extMatch)?"":this.ext)));
var _3=MVC.View.get(this.name,this.cache);
if(_3){
return _3;
}
if(_3==MVC.View.INVALID_PATH){
return null;
}
this.text=include.request(_2+(this.cache||window._rhino?"":"?"+Math.random()));
if(this.text==null){
if(window._rhino){
print("Exception: "+"There is no template at "+_2);
}
throw ({type:"JMVC",message:"There is no template at "+_2});
}
}else{
if(_1.hasOwnProperty("element")){
if(typeof _1.element=="string"){
var _4=_1.element;
_1.element=MVC.$E(_1.element);
if(_1.element==null){
throw _4+"does not exist!";
}
}
if(_1.element.value){
this.text=_1.element.value;
}else{
this.text=_1.element.innerHTML;
}
this.name=_1.element.id;
this.type="[";
}
}
var _3=new MVC.View.Compiler(this.text,this.type);
_3.compile(_1);
MVC.View.update(this.name,this);
this.template=_3;
};
MVC.View.prototype={render:function(_5,_6){
_5=_5||{};
var v=new MVC.View.Helpers(_5);
MVC.Object.extend(v,_6||{});
return this.template.process.call(_5,_5,v);
},out:function(){
return this.template.out;
},set_options:function(_8){
this.type=_8.type!=null?_8.type:MVC.View.type;
this.cache=_8.cache!=null?_8.cache:MVC.View.cache;
this.text=_8.text!=null?_8.text:null;
this.name=_8.name!=null?_8.name:null;
this.ext=_8.ext!=null?_8.ext:MVC.View.ext;
this.extMatch=new RegExp(this.ext.replace(/\./,"."));
},update:function(_9,_a){
if(typeof _9=="string"){
_9=MVC.$E(_9);
}
if(_a==null){
_template=this;
return function(_b){
MVC.View.prototype.update.call(_template,_9,_b);
};
}
if(typeof _a=="string"){
params={};
params.url=_a;
_template=this;
params.onComplete=function(_c){
var _d=eval("("+_c.responseText+")");
MVC.View.prototype.update.call(_template,_9,_d);
};
if(!MVC.Ajax){
alert("You must include the Ajax plugin to use this feature");
}
new MVC.Ajax(params.url,params);
}else{
_9.innerHTML=this.render(_a);
}
}};
MVC.View.Scanner=function(_e,_f,_10){
this.left_delimiter=_f+"%";
this.right_delimiter="%"+_10;
this.double_left=_f+"%%";
this.double_right="%%"+_10;
this.left_equal=_f+"%=";
this.left_comment=_f+"%#";
if(_f=="["){
this.SplitRegexp=/(\[%%)|(%%\])|(\[%=)|(\[%#)|(\[%)|(%\]\n)|(%\])|(\n)/;
}else{
this.SplitRegexp=new RegExp("("+this.double_left+")|(%%"+this.double_right+")|("+this.left_equal+")|("+this.left_comment+")|("+this.left_delimiter+")|("+this.right_delimiter+"\n)|("+this.right_delimiter+")|(\n)");
}
this.source=_e;
this.stag=null;
this.lines=0;
};
MVC.View.Scanner.to_text=function(_11){
if(_11==null||_11===undefined){
return "";
}
if(_11 instanceof Date){
return _11.toDateString();
}
if(_11.toString){
return _11.toString();
}
return "";
};
MVC.View.Scanner.prototype={scan:function(_12){
scanline=this.scanline;
regex=this.SplitRegexp;
if(!this.source==""){
var _13=MVC.String.rsplit(this.source,/\n/);
for(var i=0;i<_13.length;i++){
var _15=_13[i];
this.scanline(_15,regex,_12);
}
}
},scanline:function(_16,_17,_18){
this.lines++;
var _19=MVC.String.rsplit(_16,_17);
for(var i=0;i<_19.length;i++){
var _1b=_19[i];
if(_1b!=null){
try{
_18(_1b,this);
}
catch(e){
throw {type:"MVC.View.Scanner",line:this.lines};
}
}
}
}};
MVC.View.Buffer=function(_1c,_1d){
this.line=new Array();
this.script="";
this.pre_cmd=_1c;
this.post_cmd=_1d;
for(var i=0;i<this.pre_cmd.length;i++){
this.push(_1c[i]);
}
};
MVC.View.Buffer.prototype={push:function(cmd){
this.line.push(cmd);
},cr:function(){
this.script=this.script+this.line.join("; ");
this.line=new Array();
this.script=this.script+"\n";
},close:function(){
if(this.line.length>0){
for(var i=0;i<this.post_cmd.length;i++){
this.push(pre_cmd[i]);
}
this.script=this.script+this.line.join("; ");
line=null;
}
}};
MVC.View.Compiler=function(_21,_22){
this.pre_cmd=["var ___ViewO = [];"];
this.post_cmd=new Array();
this.source=" ";
if(_21!=null){
if(typeof _21=="string"){
_21=_21.replace(/\r\n/g,"\n");
_21=_21.replace(/\r/g,"\n");
this.source=_21;
}else{
if(_21.innerHTML){
this.source=_21.innerHTML;
}
}
if(typeof this.source!="string"){
this.source="";
}
}
_22=_22||"<";
var _23=">";
switch(_22){
case "[":
_23="]";
break;
case "<":
break;
default:
throw _22+" is not a supported deliminator";
break;
}
this.scanner=new MVC.View.Scanner(this.source,_22,_23);
this.out="";
};
MVC.View.Compiler.prototype={compile:function(_24){
_24=_24||{};
this.out="";
var _25="___ViewO.push(";
var _26=_25;
var _27=new MVC.View.Buffer(this.pre_cmd,this.post_cmd);
var _28="";
var _29=function(_2a){
_2a=_2a.replace(/\\/g,"\\\\");
_2a=_2a.replace(/\n/g,"\\n");
_2a=_2a.replace(/"/g,"\\\"");
return _2a;
};
this.scanner.scan(function(_2b,_2c){
if(_2c.stag==null){
switch(_2b){
case "\n":
_28=_28+"\n";
_27.push(_25+"\""+_29(_28)+"\");");
_27.cr();
_28="";
break;
case _2c.left_delimiter:
case _2c.left_equal:
case _2c.left_comment:
_2c.stag=_2b;
if(_28.length>0){
_27.push(_25+"\""+_29(_28)+"\")");
}
_28="";
break;
case _2c.double_left:
_28=_28+_2c.left_delimiter;
break;
default:
_28=_28+_2b;
break;
}
}else{
switch(_2b){
case _2c.right_delimiter:
switch(_2c.stag){
case _2c.left_delimiter:
if(_28[_28.length-1]=="\n"){
_28=MVC.String.chop(_28);
_27.push(_28);
_27.cr();
}else{
_27.push(_28);
}
break;
case _2c.left_equal:
_27.push(_26+"(MVC.View.Scanner.to_text("+_28+")))");
break;
}
_2c.stag=null;
_28="";
break;
case _2c.double_right:
_28=_28+_2c.right_delimiter;
break;
default:
_28=_28+_2b;
break;
}
}
});
if(_28.length>0){
_27.push(_25+"\""+_29(_28)+"\")");
}
_27.close();
this.out=_27.script+";";
var _2d="this.process = function(_CONTEXT,_VIEW) { try { with(_VIEW) { with (_CONTEXT) {"+this.out+" return ___ViewO.join('');}}}catch(e){e.lineNumber=null;throw e;}};";
try{
eval(_2d);
}
catch(e){
if(typeof JSLINT!="undefined"){
JSLINT(this.out);
for(var i=0;i<JSLINT.errors.length;i++){
var _30=JSLINT.errors[i];
if(_30.reason!="Unnecessary semicolon."){
_30.line++;
var e=new Error();
e.lineNumber=_30.line;
e.message=_30.reason;
if(_24.url){
e.fileName=_24.url;
}
throw e;
}
}
}else{
throw e;
}
}
}};
MVC.View.config=function(_31){
MVC.View.cache=_31.cache!=null?_31.cache:MVC.View.cache;
MVC.View.type=_31.type!=null?_31.type:MVC.View.type;
MVC.View.ext=_31.ext!=null?_31.ext:MVC.View.ext;
var _32={};
MVC.View.templates_directory=_32;
MVC.View.get=function(_33,_34){
if(_34==false){
return null;
}
if(_32[_33]){
return _32[_33];
}
return null;
};
MVC.View.update=function(_35,_36){
if(_35==null){
return;
}
_32[_35]=_36;
};
MVC.View.INVALID_PATH=-1;
};
MVC.View.config({cache:include.get_env()=="production",type:"<",ext:".ejs"});
MVC.View.PreCompiledFunction=function(_37,_38,f){
new MVC.View({name:_38,precompiled:f});
};
MVC.View.Helpers=function(_3a){
this.data=_3a;
};
MVC.View.Helpers.prototype={partial:function(_3b,_3c){
if(!_3c){
_3c=this.data;
}
return new MVC.View(_3b).render(_3c);
},to_text:function(_3d,_3e){
if(_3d==null||_3d===undefined){
return _3e||"";
}
if(_3d instanceof Date){
return _3d.toDateString();
}
if(_3d.toString){
return _3d.toString().replace(/\n/g,"<br />").replace(/''/g,"'");
}
return "";
}};
include.view=function(_3f){
if(include.get_env()=="development"){
new MVC.View({url:new MVC.File("../"+_3f).join_current()});
}else{
if(include.get_env()=="compress"){
include({path:"../"+_3f,process:MVC.View.process_include,ignore:true});
new MVC.View({url:new MVC.File("../"+_3f).join_current()});
}else{
}
}
};
include.views=function(){
for(var i=0;i<arguments.length;i++){
include.view(arguments[i]+MVC.View.ext);
}
};
MVC.View.process_include=function(_41){
var _42=new MVC.View({text:_41.text});
return "MVC.View.PreCompiledFunction(\""+_41.original_path+"\", \""+_41.path+"\",function(_CONTEXT,_VIEW) { try { with(_VIEW) { with (_CONTEXT) {"+_42.out()+" return ___ViewO.join('');}}}catch(e){e.lineNumber=null;throw e;}})";
};
if(!MVC._no_conflict){
View=MVC.View;
}
MVC.Native.extend("String",{rsplit:function(_43,_44){
var _45=_44.exec(_43);
var _46=new Array();
while(_45!=null){
var _47=_45.index;
var _48=_44.lastIndex;
if((_47)!=0){
var _49=_43.substring(0,_47);
_46.push(_43.substring(0,_47));
_43=_43.slice(_47);
}
_46.push(_45[0]);
_43=_43.slice(_45[0].length);
_45=_44.exec(_43);
}
if(!_43==""){
_46.push(_43);
}
return _46;
},chop:function(_4a){
return _4a.substr(0,_4a.length-1);
}});
;
include.set_path('jmvc/plugins/controller/view');
include.plugins("view","controller");
include("controller_view");
;
include.set_path('jmvc/plugins/controller/view');
MVC.Controller.prototype.render=function(_1){
var _2,_3=MVC.RENDER_TO,_4;
var _5=this.Class.className;
var _6=this.action_name;
if(!_1){
_1={};
}
var _7={};
if(_1.helpers){
for(var h=0;h<_1.helpers.length;h++){
var n=MVC.String.classize(_1.helpers[h]);
MVC.Object.extend(_7,window[n]?window[n].View().helpers:{});
}
}
if(typeof _1=="string"){
_2=new MVC.View({url:_1}).render(this,_7);
}else{
if(_1.text){
_2=_1.text;
}else{
var _a=function(_b){
var _b=MVC.String.include(_b,"/")?_b.split("/").join("/_"):_5+"/"+_b;
var _b=_b+MVC.View.ext;
return _b;
};
if(_1.plugin){
_4="../jmvc/plugins/"+_1.plugin;
}
if(_1.action){
var _c="../views/"+_a(_1.action);
}else{
if(_1.partial){
var _c="../views/"+_a(_1.partial);
}else{
var _c="../views/"+_5+"/"+_6.replace(/\.|#/g,"").replace(/ /g,"_")+MVC.View.ext;
}
}
var _d=_1.using||this;
if(_1.locals){
for(var _e in _1.locals){
_d[_e]=_1.locals[_e];
}
}
var _f;
if(!_4){
_f=new MVC.View({url:new MVC.File(_c).join_from(this.Class._path)});
}else{
try{
var _f=new MVC.View({url:MVC.View.get(_4)?_4:_c});
}
catch(e){
if(e.type!="JMVC"){
throw e;
}
var _f=new MVC.View({url:_4});
}
}
_2=_f.render(_d,_7);
}
}
var _10=["to","before","after","top","bottom","replace"];
var _11=null;
for(var l=0;l<_10.length;l++){
if(typeof _1[_10[l]]=="string"){
var id=_1[_10[l]];
_1[_10[l]]=MVC.$E(id);
if(!_1[_10[l]]){
throw {message:"Can't find element with id: "+id,name:"ControllerView: Missing Element"};
}
}
if(_1[_10[l]]){
_11=_1[_10[l]];
if(_10[l]=="to"){
if(MVC.$E.update){
MVC.$E.update(_1.to,_2);
}else{
_1.to.innerHTML=_2;
}
}else{
if(_10[l]=="replace"){
MVC.$E.replace(_1.replace,_2);
}else{
if(!MVC.$E.insert){
throw {message:"Include can't insert "+_10[l]+" without the element plugin.",name:"ControllerView: Missing Plugin"};
}
var opt={};
opt[_10[l]]=_2;
MVC.$E.insert(_11,opt);
}
}
}
}
return _2;
};
;
include.set_path('jmvc/plugins/view/helpers');
include.plugins("view");
include("view_helpers");
;
include.set_path('jmvc/plugins/view/helpers');
MVC.Object.extend(MVC.View.Helpers.prototype,{check_box_tag:function(_1,_2,_3,_4){
_3=_3||{};
if(_4){
_3.checked="checked";
}
return this.input_field_tag(_1,_2,"checkbox",_3);
},date_tag:function(_5,_6,_7){
if(!(_6 instanceof Date)){
_6=new Date();
}
var _8=[],_9=[],_a=[];
var _b=_6.getFullYear(),_c=_6.getMonth(),_d=_6.getDate();
for(var y=_b-15;y<_b+15;y++){
_8.push({value:y,text:y});
}
for(var m=0;m<12;m++){
_9.push({value:(m),text:MVC.Date.month_names[m]});
}
for(var d=0;d<31;d++){
_a.push({value:(d+1),text:(d+1)});
}
var _11=this.select_tag(_5+"[year]",_b,_8,{id:_5+"[year]"});
var _12=this.select_tag(_5+"[month]",_c,_9,{id:_5+"[month]"});
var _13=this.select_tag(_5+"[day]",_d,_a,{id:_5+"[day]"});
return _11+_12+_13;
},time_tag:function(_14,_15,_16,_17){
var _18=[];
if(_17==null||_17==0){
_17=60;
}
for(var h=0;h<24;h++){
for(var m=0;m<60;m+=_17){
var _1b=(h<10?"0":"")+h+":"+(m<10?"0":"")+m;
_18.push({text:_1b,value:_1b});
}
}
return this.select_tag(_14,_15,_18,_16);
},file_tag:function(_1c,_1d,_1e){
return this.input_field_tag(_1c+"[file]",_1d,"file",_1e);
},form_tag:function(_1f,_20){
_20=_20||{};
if(_20.multipart==true){
_20.method="post";
_20.enctype="multipart/form-data";
}
_20.action=_1f;
return this.start_tag_for("form",_20);
},form_tag_end:function(){
return this.tag_end("form");
},hidden_field_tag:function(_21,_22,_23){
return this.input_field_tag(_21,_22,"hidden",_23);
},input_field_tag:function(_24,_25,_26,_27){
_27=_27||{};
_27.id=_27.id||_24;
_27.value=_25||"";
_27.type=_26||"text";
_27.name=_24;
return this.single_tag_for("input",_27);
},label_tag:function(_28,_29){
_29=_29||{};
return this.start_tag_for("label",_29)+_28+this.tag_end("label");
},link_to:function(_2a,url,_2c){
if(!_2a){
var _2a="null";
}
if(!_2c){
var _2c={};
}
this.set_confirm(_2c);
_2c.href=url;
return this.start_tag_for("a",_2c)+_2a+this.tag_end("a");
},link_to_if:function(_2d,_2e,url,_30){
return this.link_to_unless((!_2d),_2e,url,_30);
},link_to_unless:function(_31,_32,url,_34){
if(_31){
return _32;
}
return this.link_to(_32,url,_34);
},set_confirm:function(_35){
if(_35.confirm){
_35.onclick=_35.onclick||"";
_35.onclick=_35.onclick+"; var ret_confirm = confirm(\""+_35.confirm+"\"); if(!ret_confirm){ return false;} ";
_35.confirm=null;
}
},submit_link_to:function(_36,_37,_38,_39){
if(!_36){
var _36="null";
}
if(!_38){
_38={};
}
_38.type="submit";
_38.value=_36;
this.set_confirm(_38);
_38.onclick=_38.onclick+";window.location=\""+_37+"\"; return false;";
return this.single_tag_for("input",_38);
},password_field_tag:function(_3a,_3b,_3c){
return this.input_field_tag(_3a,_3b,"password",_3c);
},select_tag:function(_3d,_3e,_3f,_40){
_40=_40||{};
_40.id=_40.id||_3d;
_40.name=_3d;
var txt="";
txt+=this.start_tag_for("select",_40);
for(var i=0;i<_3f.length;i++){
var _43=_3f[i];
if(typeof _43=="string"){
_43={value:_43};
}
if(!_43.text){
_43.text=_43.value;
}
if(!_43.value){
_43.text=_43.text;
}
var _44={value:_43.value};
if(_43.value==_3e){
_44.selected="selected";
}
txt+=this.start_tag_for("option",_44)+_43.text+this.tag_end("option");
}
txt+=this.tag_end("select");
return txt;
},single_tag_for:function(tag,_46){
return this.tag(tag,_46,"/>");
},start_tag_for:function(tag,_48){
return this.tag(tag,_48);
},submit_tag:function(_49,_4a){
_4a=_4a||{};
_4a.type=_4a.type||"submit";
_4a.value=_49||"Submit";
return this.single_tag_for("input",_4a);
},tag:function(tag,_4c,end){
end=end||">";
var txt=" ";
for(var _4f in _4c){
if(_4c.hasOwnProperty(_4f)){
value=_4c[_4f]!=null?_4c[_4f].toString():"";
if(_4f=="Class"||_4f=="klass"){
_4f="class";
}
if(value.indexOf("'")!=-1){
txt+=_4f+"=\""+value+"\" ";
}else{
txt+=_4f+"='"+value+"' ";
}
}
}
return "<"+tag+txt+end;
},tag_end:function(tag){
return "</"+tag+">";
},text_area_tag:function(_51,_52,_53){
_53=_53||{};
_53.id=_53.id||_51;
_53.name=_53.name||_51;
_52=_52||"";
if(_53.size){
_53.cols=_53.size.split("x")[0];
_53.rows=_53.size.split("x")[1];
delete _53.size;
}
_53.cols=_53.cols||50;
_53.rows=_53.rows||4;
return this.start_tag_for("textarea",_53)+_52+this.tag_end("textarea");
},text_field_tag:function(_54,_55,_56){
return this.input_field_tag(_54,_55,"text",_56);
},img_tag:function(_57,_58){
_58=_58||{};
_58.src="resources/images/"+_57;
return this.single_tag_for("img",_58);
}});
MVC.View.Helpers.prototype.text_tag=MVC.View.Helpers.prototype.text_area_tag;
(function(){
var _59={};
var _5a=0;
MVC.View.Helpers.link_data=function(_5b){
var _5c=_5a++;
_59[_5c]=_5b;
return "_data='"+_5c+"'";
};
MVC.View.Helpers.get_data=function(el){
if(!el){
return null;
}
var _5e=el.getAttribute("_data");
if(!_5e){
return null;
}
return _59[parseInt(_5e)];
};
MVC.View.Helpers.prototype.link_data=function(_5f){
return MVC.View.Helpers.link_data(_5f);
};
MVC.View.Helpers.prototype.get_data=function(el){
return MVC.View.Helpers.get_data(el);
};
})();
;
include.next_function();
include.set_path('engines/mTangoUI/models');
Page=MVC.Model.extend("Page",{},{$page:null,header:null,content:null,footer:null,set_$page:function(v){
this.$page=v;
},set_header:function(v){
this.header=v;
},set_content:function(v){
this.content=v;
},set_footer:function(v){
this.footer=v;
},init:function(_5){
var _6=$(new View({url:"engines/mTangoUI/views/empty_page.ejs"}).render({pageId:_5})).appendTo(document.body);
var _7={$page:_6,header:new View({url:"views/"+_5+"/header.ejs"}),content:new View({url:"views/"+_5+"/content.ejs"}),footer:new View({url:"views/"+_5+"/footer.ejs"})};
this._super(_7);
},load:function(_8){
$(".header",this.$page).html(this.header.render(_8));
$(".content",this.$page).html(this.content.render(_8));
$(".footer",this.$page).html(this.footer.render());
this.$page.page();
}});
;
include.set_path('engines/mTangoUI/views');
MVC.View.PreCompiledFunction("../views/empty_page.ejs","engines/mTangoUI/views/empty_page.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div id=\"");
_3.push((MVC.View.Scanner.to_text(pageId)));
_3.push("\" data-role=\"page\">\n");
_3.push("    <div class=\"header\" data-role=\"header\">\n");
_3.push("\n");
_3.push("    </div>\n");
_3.push("    <div class=\"content\" data-role=\"content\">\n");
_3.push("\n");
_3.push("    </div>\n");
_3.push("    <div class=\"footer\" data-role=\"footer\" data-position=\"fixed\">\n");
_3.push("    </div>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('jmvc/plugins/controller/stateful');
include.plugins("controller");
include("stateful_controller");
;
include.set_path('jmvc/plugins/controller/stateful');
MVC.Controller.Stateful=MVC.Controller.extend({_should_attach_actions:false,_events:null,_element:null},{init:function(_1){
MVC.Delegator.jmvc(_1);
this._actions=[];
for(var _2 in this){
val=this[_2];
if(typeof val=="function"&&_2!="Class"){
for(var a=0;a<MVC.Controller.actions.length;a++){
act=MVC.Controller.actions[a];
if(act.matches(_2)){
var _4=this.dispatch_closure(_2);
this._actions.push(new act(_2,_4,this.Class.className,_1));
}
}
}
}
this._children=[];
this.action_name="init";
this.element=_1;
},destroy:function(){
if(this._destroyed){
throw this.Class.className+" controller instance has already been deleted";
}
for(var i=0;i<this._actions.length;i++){
this._actions[i].destroy();
}
var _6=MVC.Dom.data(this.element).delegation_events;
if(this.element&&_6){
for(var _7 in _6){
var _8=_6[_7];
for(var i=0;i<_8.length;i++){
_8[i].destroy();
}
}
}
if(this._parent){
this._parent.remove(this);
}
if(this.element&&this.element.parentNode){
this.element.parentNode.removeChild(this.element);
}
this._destroyed=true;
},dispatch_closure:function(_9){
return MVC.Function.bind(function(_a){
if(!MVC.Dom.data(this.element).responding){
return;
}
_a=_a||{};
_a.action=_9;
_a.controller=this.Class;
_a=_a.constructor==MVC.Controller.Params?_a:new MVC.Controller.Params(_a);
this.action_name=_9;
return this[_9](_a);
},this);
},query:function(_b){
return MVC.Query.descendant(this.element,_b);
},respond:function(_c){
MVC.Dom.data(this.element).responding=_c;
},add_child:function(_d){
_d._parent=this;
this._children.push(_d);
return _d;
},remove_child:function(_e){
for(var i=0;i<this._children.length;i++){
if(this._children[i]===_e){
this._children[i].splice(i,1);
break;
}
}
}});
;
include.set_path('jmvc/plugins/dom/element');
include.plugins("lang/vector");
include("element");
;
include.set_path('jmvc/plugins/lang/vector');
include.plugins("lang","dom/event");
include("vector");
;
include.set_path('jmvc/plugins/lang/vector');
MVC.Vector=function(){
this.update(MVC.Array.from(arguments));
};
MVC.Vector.prototype={app:function(f){
var _2=[];
for(var i=0;i<this.array.length;i++){
_2.push(f(this.array[i]));
}
var _4=new MVC.Vector();
return _4.update(_2);
},plus:function(){
var _5=arguments[0] instanceof MVC.Vector?arguments[0].array:MVC.Array.from(arguments),_6=this.array.slice(0),_7=new MVC.Vector();
for(var i=0;i<_5.length;i++){
_6[i]=(_6[i]?_6[i]:0)+_5[i];
}
return _7.update(_6);
},minus:function(){
var _9=arguments[0] instanceof MVC.Vector?arguments[0].array:MVC.Array.from(arguments),_a=this.array.slice(0),_b=new MVC.Vector();
for(var i=0;i<_9.length;i++){
_a[i]=(_a[i]?_a[i]:0)-_9[i];
}
return _b.update(_a);
},equals:function(){
var _d=arguments[0] instanceof MVC.Vector?arguments[0].array:MVC.Array.from(arguments),_e=this.array.slice(0),_f=new MVC.Vector();
for(var i=0;i<_d.length;i++){
if(_e[i]!=_d[i]){
return null;
}
}
return _f.update(_e);
},x:function(){
return this.array[0];
},width:function(){
return this.array[0];
},y:function(){
return this.array[1];
},height:function(){
return this.array[1];
},top:function(){
return this.array[1];
},left:function(){
return this.array[0];
},toString:function(){
return "("+this.array[0]+","+this.array[1]+")";
},update:function(_11){
if(this.array){
for(var i=0;i<this.array.length;i++){
delete this.array[i];
}
}
this.array=_11;
for(var i=0;i<_11.length;i++){
this[i]=this.array[i];
}
return this;
}};
MVC.Event.pointer=function(_13){
return new MVC.Vector((_13.clientX+(document.documentElement.scrollLeft||document.body.scrollLeft)),(_13.clientY+(document.documentElement.scrollTop||document.body.scrollTop)));
};
;
include.set_path('jmvc/plugins/dom/element');
MVC.Element=function(_1){
if(typeof _1=="string"){
_1=document.getElementById(_1);
}
if(!_1){
return _1;
}
return _1._mvcextend?_1:MVC.Element.extend(_1);
};
MVC.Object.extend(MVC.Element,{insert:function(_2,_3){
_2=MVC.$E(_2);
if(typeof _3=="string"){
_3={bottom:_3};
}
var _4,_5,_6,_7;
for(position in _3){
if(!_3.hasOwnProperty(position)){
continue;
}
_4=_3[position];
position=position.toLowerCase();
_5=MVC.$E._insertionTranslations[position];
if(_4&&_4.nodeType==1){
_5(_2,_4);
continue;
}
_6=((position=="before"||position=="after")?_2.parentNode:_2).tagName.toUpperCase();
_7=MVC.$E._getContentFromAnonymousElement(_6,_4);
if(position=="top"||position=="after"){
_7.reverse();
}
for(var c=0;c<_7.length;c++){
_5(_2,_7[c]);
}
}
return _2;
},_insertionTranslations:{before:function(_9,_a){
_9.parentNode.insertBefore(_a,_9);
},top:function(_b,_c){
_b.insertBefore(_c,_b.firstChild);
},bottom:function(_d,_e){
_d.appendChild(_e);
},after:function(_f,_10){
_f.parentNode.insertBefore(_10,_f.nextSibling);
},tags:{TABLE:["<table>","</table>",1],TBODY:["<table><tbody>","</tbody></table>",2],TR:["<table><tbody><tr>","</tr></tbody></table>",3],TD:["<table><tbody><tr><td>","</td></tr></tbody></table>",4],SELECT:["<select>","</select>",1]}},replace:function(_11,_12){
var _11=MVC.$E(_11);
if(_12.nodeType==1){
_11.parentNode.replaceChild(_12,_11);
return _11;
}
if(_11.outerHTML){
var _13=_11.parentNode,_14=_13.tagName.toUpperCase();
if(MVC.Element._insertionTranslations.tags[_14]){
var _15=_11.next();
var _16=MVC.Element._getContentFromAnonymousElement(_14,_12);
_13.removeChild(_11);
if(_15){
for(var i=0;i<_16.length;i++){
_13.insertBefore(_16[i],_15);
}
}else{
for(var i=0;i<_16.length;i++){
_13.appendChild(_16[i]);
}
}
}else{
_11.outerHTML=_12;
}
return _11;
}else{
if(_12.nodeType!=1){
var _18=_11.ownerDocument.createRange();
_18.selectNode(_11);
_12=_18.createContextualFragment(_12);
}
_11.parentNode.replaceChild(_12,_11);
return _11;
}
},_getContentFromAnonymousElement:function(_19,_1a){
var div=document.createElement("div"),t=MVC.$E._insertionTranslations.tags[_19];
if(t){
div.innerHTML=t[0]+_1a+t[1];
for(var i=0;i<t[2];i++){
div=div.firstChild;
}
}else{
div.innerHTML=_1a;
}
return MVC.Array.from(div.childNodes);
},get_children:function(_1e){
var els=[];
var el=_1e.first();
while(el){
els.push(el);
el=el.next();
}
return els;
},first:function(_21,_22){
_22=_22||function(){
return true;
};
var _23=_21.firstChild;
while(_23&&_23.nodeType!=1||(_23&&!_22(_23))){
_23=_23.nextSibling;
}
return MVC.$E(_23);
},last:function(_24,_25){
_25=_25||function(){
return true;
};
var _26=_24.lastChild;
while(_26&&_26.nodeType!=1||(_26&&!_25(_26))){
_26=_26.previousSibling;
}
return MVC.$E(_26);
},next:function(_27,_28,_29){
_29=_29||function(){
return true;
};
var _2a=_27.nextSibling;
while(_2a&&_2a.nodeType!=1||(_2a&&!_29(_2a))){
_2a=_2a.nextSibling;
}
if(!_2a&&_28){
return MVC.$E(_27.parentNode).first(_29);
}
return MVC.$E(_2a);
},previous:function(_2b,_2c,_2d){
_2d=_2d||function(){
return true;
};
var _2e=_2b.previousSibling;
while(_2e&&_2e.nodeType!=1||(_2e&&!_2d(_2e))){
_2e=_2e.previousSibling;
}
if(!_2e&&_2c){
return MVC.$E(_2b.parentNode).last(_2d);
}
return MVC.$E(_2e);
},toggle:function(_2f){
return _2f.style.display=="none"?_2f.style.display="":_2f.style.display="none";
},make_positioned:function(_30){
_30=MVC.$E(_30);
var pos=MVC.Element.get_style(_30,"position");
if(pos=="static"||!pos){
_30._madePositioned=true;
_30.style.position="relative";
if(window.opera){
_30.style.top=0;
_30.style.left=0;
}
}
return _30;
},get_style:function(_32,_33){
_32=MVC.$E(_32);
_33=_33=="float"?"cssFloat":MVC.String.camelize(_33);
var _34;
if(_32.currentStyle){
var _34=_32.currentStyle[_33];
}else{
var css=document.defaultView.getComputedStyle(_32,null);
_34=css?css[_33]:null;
}
if(_33=="opacity"){
return _34?parseFloat(_34):1;
}
return _34=="auto"?null:_34;
},has:function(_36,b){
if(!b){
return false;
}
if(typeof b=="string"){
b=MVC.$E(b);
}
return _36.contains?_36!=b&&_36.contains(b):!!(_36.compareDocumentPosition(b)&16);
},update:function(_38,_39){
_38=MVC.$E(_38);
var _3a=_38.tagName.toUpperCase();
if((!MVC.Browser.IE&&!MVC.Browser.Opera)||!(_3a in MVC.$E._insertionTranslations.tags)){
_38.innerHTML=_39;
}else{
var _3b;
while((_3b=_38.childNodes[0])){
_38.removeChild(_3b);
}
var _3c=MVC.$E._getContentFromAnonymousElement(_3a,_39);
for(var c=0;c<_3c.length;c++){
_38.appendChild(_3c[c]);
}
}
return _38;
},remove:function(_3e){
return _3e.parentNode.removeChild(_3e);
},dimensions:function(_3f){
if(_3f===window){
return new MVC.Vector(window.innerWidth?window.innerWidth:document.documentElement.clientWidth,window.innerHeight?window.innerHeight:document.documentElement.clientHeight);
}
if(!MVC.Element.has(document.body,_3f)){
return new MVC.Vector(parseInt(_3f.get_style("width")),parseInt(_3f.get_style("height")));
}
var _40=_3f.style.display;
if(_40!="none"&&_40!=null){
return new MVC.Vector(_3f.offsetWidth,_3f.offsetHeight);
}
var els=_3f.style;
var _42=els.visibility;
var _43=els.position;
var _44=els.display;
els.visibility="hidden";
els.position="absolute";
els.display="block";
var _45=_3f.clientWidth;
var _46=_3f.clientHeight;
els.display=_44;
els.position=_43;
els.visibility=_42;
return new MVC.Vector(_45,_46);
},add_class:function(_47,_48){
var cns=this.class_names(_47);
if(MVC.Array.include(cns,_48)){
return;
}
cns.push(_48);
_47.className=cns.join(" ");
return _47;
},remove_class:function(_4a,_4b){
var cns=this.class_names(_4a);
var _4d=[];
for(var i=0;i<cns.length;i++){
if(cns[i]!=_4b){
_4d.push(cns[i]);
}
}
_4a.className=_4d.join(" ");
return _4a;
},class_names:function(_4f){
return _4f.className.split(MVC.Element._class_name_split);
},_class_name_split:/\s+/,has_class:function(_50,_51){
var cns=this.class_names(_50);
var _53;
for(var i=0;i<cns.length;i++){
if((_53=cns[i].match(_51))){
return _53;
}
}
}});
MVC.Element.extend=function(el){
for(var f in MVC.Element){
if(!MVC.Element.hasOwnProperty(f)){
continue;
}
var _57=MVC.Element[f];
if(typeof _57=="function"){
if(f[0]!="_"){
MVC.Element._extend(_57,f,el);
}
}
}
el._mvcextend=true;
return el;
};
MVC.Element._extend=function(f,_59,el){
el[_59]=function(){
var arg=MVC.Array.from(arguments);
arg.unshift(el);
return f.apply(el,arg);
};
};
MVC.$E=MVC.Element;
if(!MVC._no_conflict){
$E=MVC.$E;
}
;
include.next_function();
include.set_path('models');
TangoTest=MVC.Model.extend("TangoTest",{attributes:{ampli:{label:"ampli",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},boolean_scalar:{label:"boolean_scalar",description:"A boolean scalar attribute",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_BOOLEAN",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},double_scalar:{label:"double_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},double_scalar_rww:{label:"double_scalar_rww",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},double_scalar_w:{label:"double_scalar_w",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},float_scalar:{label:"float_scalar",description:"A float attribute",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_FLOAT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long64_scalar:{label:"long64_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG64",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long_scalar:{label:"long_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long_scalar_rww:{label:"long_scalar_rww",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long_scalar_w:{label:"long_scalar_w",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},no_value:{label:"no_value",description:"No description",isReadOnly:true,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_scalar:{label:"short_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_scalar_ro:{label:"short_scalar_ro",description:"No description",isReadOnly:true,dataFormat:"SCALAR",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_scalar_rww:{label:"short_scalar_rww",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_scalar_w:{label:"short_scalar_w",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},string_scalar:{label:"string_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},throw_exception:{label:"throw_exception",description:"No description",isReadOnly:true,dataFormat:"SCALAR",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},uchar_scalar:{label:"uchar_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_UCHAR",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ulong64_scalar:{label:"ulong64_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_ULONG64",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ushort_scalar:{label:"ushort_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_USHORT",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ulong_scalar:{label:"ulong_scalar",description:"No description",isReadOnly:false,dataFormat:"SCALAR",dataType:"Tango_DEV_ULONG",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},boolean_spectrum:{label:"boolean_spectrum",description:"No description",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_BOOLEAN",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},boolean_spectrum_ro:{label:"boolean_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_BOOLEAN",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},double_spectrum:{label:"double_spectrum",description:"No description",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},double_spectrum_ro:{label:"double_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},float_spectrum:{label:"float_spectrum",description:"A float spectrum attribute",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_FLOAT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},float_spectrum_ro:{label:"float_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_FLOAT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long64_spectrum_ro:{label:"long64_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_LONG64",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long_spectrum:{label:"long_spectrum",description:"No description",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},long_spectrum_ro:{label:"long_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_spectrum:{label:"short_spectrum",description:"No description",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},short_spectrum_ro:{label:"short_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},string_spectrum:{label:"string_spectrum",description:"No description",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:256,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},string_spectrum_ro:{label:"string_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:256,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},uchar_spectrum:{label:"uchar_spectrum",description:"An unsigned char spectrum attribute",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_UCHAR",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"255",minValue:"0"},uchar_spectrum_ro:{label:"uchar_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_UCHAR",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ulong64_spectrum_ro:{label:"ulong64_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_ULONG64",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ulong_spectrum_ro:{label:"ulong_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_ULONG",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ushort_spectrum:{label:"ushort_spectrum",description:"An unsigned short spectrum attribute",isReadOnly:false,dataFormat:"SPECTRUM",dataType:"Tango_DEV_USHORT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},ushort_spectrum_ro:{label:"ushort_spectrum_ro",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_USHORT",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},wave:{label:"wave",description:"No description",isReadOnly:true,dataFormat:"SPECTRUM",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:4096,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},boolean_image:{label:"boolean_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_BOOLEAN",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},boolean_image_ro:{label:"boolean_image",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_BOOLEAN",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"1",minValue:"0"},double_image:{label:"double_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"4096",minValue:"Not specified"},double_image_ro:{label:"double_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_DOUBLE",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},float_image:{label:"float_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_FLOAT",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},float_image_ro:{label:"float_image",description:"A float image attribute",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_FLOAT",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"255",minValue:"0"},long64_image_ro:{label:"long64_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_LONG64",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},long_image:{label:"long_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},long_image_ro:{label:"long_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_LONG",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},short_image:{label:"short_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},short_image_ro:{label:"short_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_SHORT",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},string_image:{label:"string_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:256,maxDimY:256,maxValue:"Not specified",minValue:"Not specified"},string_image_ro:{label:"string_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:256,maxDimY:256,maxValue:"Not specified",minValue:"Not specified"},uchar_image:{label:"uchar_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_UCHAR",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},uchar_image_ro:{label:"uchar_image",description:"An unsigned char image attribute",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_UCHAR",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"255",minValue:"0"},ulong64_image_ro:{label:"ulong64_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_ULONG64",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},ulong_image_ro:{label:"ulong_image_ro",description:"No description",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_ULONG",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},ushort_image:{label:"ushort_image",description:"No description",isReadOnly:false,dataFormat:"IMAGE",dataType:"Tango_DEV_USHORT",unit:"No unit",displayUnit:"No unit",maxDimX:251,maxDimY:251,maxValue:"Not specified",minValue:"Not specified"},ushort_image_ro:{label:"ushort_image_ro",description:"An unsigned short image attribute",isReadOnly:true,dataFormat:"IMAGE",dataType:"Tango_DEV_USHORT",unit:"No unit",displayUnit:"No unit",maxDimX:8192,maxDimY:8192,maxValue:"255",minValue:"0"},State:{label:"State",description:"No description",isReadOnly:true,dataFormat:"SCALAR",dataType:"Tango_DEV_STATE",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"},Status:{label:"Status",description:"No description",isReadOnly:true,dataFormat:"SCALAR",dataType:"Tango_DEV_STRING",unit:"No unit",displayUnit:"No unit",maxDimX:1,maxDimY:0,maxValue:"Not specified",minValue:"Not specified"}},commands:{CrashFromDevelopperThread:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_VOID",outputDescription:"Uninitialised"},CrashFromOmniThread:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_VOID",outputDescription:"Uninitialised"},DevBoolean:{inputType:"Tango_DEV_BOOLEAN",inputDescription:"Any boolean value",outputType:"Tango_DEV_BOOLEAN",outputDescription:"Echo of the argin value"},DevDouble:{inputType:"Tango_DEV_DOUBLE",inputDescription:"Any DevDouble value",outputType:"Tango_DEV_DOUBLE",outputDescription:"Echo of the argin value"},DevFloat:{inputType:"Tango_DEV_FLOAT",inputDescription:"Any DevFloat value",outputType:"Tango_DEV_FLOAT",outputDescription:"Echo of the argin value"},DevLong:{inputType:"Tango_DEV_LONG",inputDescription:"Any DevLong value",outputType:"Tango_DEV_LONG",outputDescription:"Echo of the argin value"},DevLong64:{inputType:"Tango_DEV_LONG64",inputDescription:"Any DevLong64 value",outputType:"Tango_DEV_LONG64",outputDescription:"Echo of the argin value"},DevShort:{inputType:"Tango_DEV_SHORT",inputDescription:"Any DevShort value",outputType:"Tango_DEV_SHORT",outputDescription:"Echo of the argin value"},DevString:{inputType:"Tango_DEV_STRING",inputDescription:"-",outputType:"Tango_DEV_STRING",outputDescription:"-"},DevULong:{inputType:"Tango_DEV_ULONG",inputDescription:"Any DevULong",outputType:"Tango_DEV_ULONG",outputDescription:"Echo of the argin value"},DevULong64:{inputType:"Tango_DEV_ULONG64",inputDescription:"Any DevULong64 value",outputType:"Tango_DEV_ULONG64",outputDescription:"Echo of the argin value"},DevUShort:{inputType:"Tango_DEV_USHORT",inputDescription:"Any DevUShort value",outputType:"Tango_DEV_USHORT",outputDescription:"Echo of the argin value"},DevVarCharArray:{inputType:"Tango_DEVVAR_CHARARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_CHARARRAY",outputDescription:"-"},DevVarDoubleArray:{inputType:"Tango_DEVVAR_DOUBLEARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_DOUBLEARRAY",outputDescription:"-"},DevVarDoubleStringArray:{inputType:"Tango_DEVVAR_DOUBLESTRINGARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_DOUBLESTRINGARRAY",outputDescription:"-"},DevVarFloatArray:{inputType:"Tango_DEVVAR_FLOATARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_FLOATARRAY",outputDescription:"-"},DevVarLong64Array:{inputType:"Tango_DEVVAR_LONG64ARRAY",inputDescription:"Uninitialised",outputType:"Tango_DEVVAR_LONG64ARRAY",outputDescription:"Uninitialised"},DevVarLongArray:{inputType:"Tango_DEVVAR_LONGARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_LONGARRAY",outputDescription:"-"},DevVarLongStringArray:{inputType:"Tango_DEVVAR_LONGSTRINGARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_LONGSTRINGARRAY",outputDescription:"-"},DevVarShortArray:{inputType:"Tango_DEVVAR_SHORTARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_SHORTARRAY",outputDescription:"-"},DevVarStringArray:{inputType:"Tango_DEVVAR_STRINGARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_STRINGARRAY",outputDescription:"-"},DevVarULong64Array:{inputType:"Tango_DEVVAR_ULONG64ARRAY",inputDescription:"Uninitialised",outputType:"Tango_DEVVAR_ULONG64ARRAY",outputDescription:"Uninitialised"},DevVarULongArray:{inputType:"Tango_DEVVAR_ULONGARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_ULONGARRAY",outputDescription:"-"},DevVarUShortArray:{inputType:"Tango_DEVVAR_USHORTARRAY",inputDescription:"-",outputType:"Tango_DEVVAR_USHORTARRAY",outputDescription:"-"},DevVoid:{inputType:"Tango_DEV_VOID",inputDescription:"N/A",outputType:"Tango_DEV_VOID",outputDescription:"N/A"},DumpExecutionState:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_VOID",outputDescription:"Uninitialised"},Init:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_VOID",outputDescription:"Uninitialised"},State:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_STATE",outputDescription:"Device state"},Status:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_STRING",outputDescription:"Device status"},SwitchStates:{inputType:"Tango_DEV_VOID",inputDescription:"Uninitialised",outputType:"Tango_DEV_VOID",outputDescription:"Uninitialised"}}},{deviceProxy:null,set_deviceProxy:function(v){
this.deviceProxy=v;
},init:function(_2){
this._super({deviceProxy:new DeviceProxy({url:_2})});
},executeCrashFromDevelopperThread:function(_3){
this.deviceProxy.executeCommand("CrashFromDevelopperThread",_3);
},executeCrashFromOmniThread:function(_4){
this.deviceProxy.executeCommand("CrashFromOmniThread",_4);
},executeDevBoolean:function(_5){
this.deviceProxy.executeCommand("DevBoolean",_5);
},executeDevDouble:function(_6){
this.deviceProxy.executeCommand("DevDouble",_6);
},executeDevFloat:function(_7){
this.deviceProxy.executeCommand("DevFloat",_7);
},executeDevLong:function(_8){
this.deviceProxy.executeCommand("DevLong",_8);
},executeDevLong64:function(_9){
this.deviceProxy.executeCommand("DevLong64",_9);
},executeDevShort:function(_a){
this.deviceProxy.executeCommand("DevShort",_a);
},executeDevString:function(_b){
this.deviceProxy.executeCommand("DevString",_b);
},executeDevULong:function(_c){
this.deviceProxy.executeCommand("DevULong",_c);
},executeDevULong64:function(_d){
this.deviceProxy.executeCommand("DevULong64",_d);
},executeDevUShort:function(_e){
this.deviceProxy.executeCommand("DevUShort",_e);
},executeDevVarCharArray:function(_f){
this.deviceProxy.executeCommand("DevVarCharArray",_f);
},executeDevVarDoubleArray:function(_10){
this.deviceProxy.executeCommand("DevVarDoubleArray",_10);
},executeDevVarDoubleStringArray:function(_11){
this.deviceProxy.executeCommand("DevVarDoubleStringArray",_11);
},executeDevVarFloatArray:function(_12){
this.deviceProxy.executeCommand("DevVarFloatArray",_12);
},executeDevVarLong64Array:function(_13){
this.deviceProxy.executeCommand("DevVarLong64Array",_13);
},executeDevVarLongArray:function(_14){
this.deviceProxy.executeCommand("DevVarLongArray",_14);
},executeDevVarLongStringArray:function(_15){
this.deviceProxy.executeCommand("DevVarLongStringArray",_15);
},executeDevVarShortArray:function(_16){
this.deviceProxy.executeCommand("DevVarShortArray",_16);
},executeDevVarStringArray:function(_17){
this.deviceProxy.executeCommand("DevVarStringArray",_17);
},executeDevVarULong64Array:function(_18){
this.deviceProxy.executeCommand("DevVarULong64Array",_18);
},executeDevVarULongArray:function(_19){
this.deviceProxy.executeCommand("DevVarULongArray",_19);
},executeDevVarUShortArray:function(_1a){
this.deviceProxy.executeCommand("DevVarUShortArray",_1a);
},executeDevVoid:function(_1b){
this.deviceProxy.executeCommand("DevVoid",_1b);
},executeDumpExecutionState:function(_1c){
this.deviceProxy.executeCommand("DumpExecutionState",_1c);
},executeInit:function(_1d){
this.deviceProxy.executeCommand("Init",_1d);
},executeState:function(_1e){
this.deviceProxy.executeCommand("State",_1e);
},executeStatus:function(_1f){
this.deviceProxy.executeCommand("Status",_1f);
},executeSwitchStates:function(_20){
this.deviceProxy.executeCommand("SwitchStates",_20);
},get_ampli:function(_21){
this.deviceProxy.readAttribute("ampli",_21);
},set_ampli:function(_22){
this.deviceProxy.writeAttribute("ampli",_22);
},get_boolean_scalar:function(_23){
this.deviceProxy.readAttribute("boolean_scalar",_23);
},set_boolean_scalar:function(_24){
this.deviceProxy.writeAttribute("boolean_scalar",_24);
},get_double_scalar:function(_25){
this.deviceProxy.readAttribute("double_scalar",_25);
},set_double_scalar:function(_26){
this.deviceProxy.writeAttribute("double_scalar",_26);
},get_double_scalar_rww:function(_27){
this.deviceProxy.readAttribute("double_scalar_rww",_27);
},set_double_scalar_rww:function(_28){
this.deviceProxy.writeAttribute("double_scalar_rww",_28);
},get_double_scalar_w:function(_29){
this.deviceProxy.readAttribute("double_scalar_w",_29);
},set_double_scalar_w:function(_2a){
this.deviceProxy.writeAttribute("double_scalar_w",_2a);
},get_float_scalar:function(_2b){
this.deviceProxy.readAttribute("float_scalar",_2b);
},set_float_scalar:function(_2c){
this.deviceProxy.writeAttribute("float_scalar",_2c);
},get_long64_scalar:function(_2d){
this.deviceProxy.readAttribute("long64_scalar",_2d);
},set_long64_scalar:function(_2e){
this.deviceProxy.writeAttribute("long64_scalar",_2e);
},get_long_scalar:function(_2f){
this.deviceProxy.readAttribute("long_scalar",_2f);
},set_long_scalar:function(_30){
this.deviceProxy.writeAttribute("long_scalar",_30);
},get_long_scalar_rww:function(_31){
this.deviceProxy.readAttribute("long_scalar_rww",_31);
},set_long_scalar_rww:function(_32){
this.deviceProxy.writeAttribute("long_scalar_rww",_32);
},get_long_scalar_w:function(_33){
this.deviceProxy.readAttribute("long_scalar_w",_33);
},set_long_scalar_w:function(_34){
this.deviceProxy.writeAttribute("long_scalar_w",_34);
},get_no_value:function(_35){
this.deviceProxy.readAttribute("no_value",_35);
},get_short_scalar:function(_36){
this.deviceProxy.readAttribute("short_scalar",_36);
},set_short_scalar:function(_37){
this.deviceProxy.writeAttribute("short_scalar",_37);
},get_short_scalar_ro:function(_38){
this.deviceProxy.readAttribute("short_scalar_ro",_38);
},get_short_scalar_rww:function(_39){
this.deviceProxy.readAttribute("short_scalar_rww",_39);
},set_short_scalar_rww:function(_3a){
this.deviceProxy.writeAttribute("short_scalar_rww",_3a);
},get_short_scalar_w:function(_3b){
this.deviceProxy.readAttribute("short_scalar_w",_3b);
},set_short_scalar_w:function(_3c){
this.deviceProxy.writeAttribute("short_scalar_w",_3c);
},get_string_scalar:function(_3d){
this.deviceProxy.readAttribute("string_scalar",_3d);
},set_string_scalar:function(_3e){
this.deviceProxy.writeAttribute("string_scalar",_3e);
},get_throw_exception:function(_3f){
this.deviceProxy.readAttribute("throw_exception",_3f);
},get_uchar_scalar:function(_40){
this.deviceProxy.readAttribute("uchar_scalar",_40);
},set_uchar_scalar:function(_41){
this.deviceProxy.writeAttribute("uchar_scalar",_41);
},get_ulong64_scalar:function(_42){
this.deviceProxy.readAttribute("ulong64_scalar",_42);
},set_ulong64_scalar:function(_43){
this.deviceProxy.writeAttribute("ulong64_scalar",_43);
},get_ushort_scalar:function(_44){
this.deviceProxy.readAttribute("ushort_scalar",_44);
},set_ushort_scalar:function(_45){
this.deviceProxy.writeAttribute("ushort_scalar",_45);
},get_ulong_scalar:function(_46){
this.deviceProxy.readAttribute("ulong_scalar",_46);
},set_ulong_scalar:function(_47){
this.deviceProxy.writeAttribute("ulong_scalar",_47);
},get_boolean_spectrum:function(_48){
this.deviceProxy.readAttribute("boolean_spectrum",_48);
},set_boolean_spectrum:function(_49){
this.deviceProxy.writeAttribute("boolean_spectrum",_49);
},get_boolean_spectrum_ro:function(_4a){
this.deviceProxy.readAttribute("boolean_spectrum_ro",_4a);
},get_double_spectrum:function(_4b){
this.deviceProxy.readAttribute("double_spectrum",_4b);
},set_double_spectrum:function(_4c){
this.deviceProxy.writeAttribute("double_spectrum",_4c);
},get_double_spectrum_ro:function(_4d){
this.deviceProxy.readAttribute("double_spectrum_ro",_4d);
},get_float_spectrum:function(_4e){
this.deviceProxy.readAttribute("float_spectrum",_4e);
},set_float_spectrum:function(_4f){
this.deviceProxy.writeAttribute("float_spectrum",_4f);
},get_float_spectrum_ro:function(_50){
this.deviceProxy.readAttribute("float_spectrum_ro",_50);
},get_long64_spectrum_ro:function(_51){
this.deviceProxy.readAttribute("long64_spectrum_ro",_51);
},get_long_spectrum:function(_52){
this.deviceProxy.readAttribute("long_spectrum",_52);
},set_long_spectrum:function(_53){
this.deviceProxy.writeAttribute("long_spectrum",_53);
},get_long_spectrum_ro:function(_54){
this.deviceProxy.readAttribute("long_spectrum_ro",_54);
},get_short_spectrum:function(_55){
this.deviceProxy.readAttribute("short_spectrum",_55);
},set_short_spectrum:function(_56){
this.deviceProxy.writeAttribute("short_spectrum",_56);
},get_short_spectrum_ro:function(_57){
this.deviceProxy.readAttribute("short_spectrum_ro",_57);
},get_string_spectrum:function(_58){
this.deviceProxy.readAttribute("string_spectrum",_58);
},set_string_spectrum:function(_59){
this.deviceProxy.writeAttribute("string_spectrum",_59);
},get_string_spectrum_ro:function(_5a){
this.deviceProxy.readAttribute("string_spectrum_ro",_5a);
},get_uchar_spectrum:function(_5b){
this.deviceProxy.readAttribute("uchar_spectrum",_5b);
},set_uchar_spectrum:function(_5c){
this.deviceProxy.writeAttribute("uchar_spectrum",_5c);
},get_uchar_spectrum_ro:function(_5d){
this.deviceProxy.readAttribute("uchar_spectrum_ro",_5d);
},get_ulong64_spectrum_ro:function(_5e){
this.deviceProxy.readAttribute("ulong64_spectrum_ro",_5e);
},get_ulong_spectrum_ro:function(_5f){
this.deviceProxy.readAttribute("ulong_spectrum_ro",_5f);
},get_ushort_spectrum:function(_60){
this.deviceProxy.readAttribute("ushort_spectrum",_60);
},set_ushort_spectrum:function(_61){
this.deviceProxy.writeAttribute("ushort_spectrum",_61);
},get_ushort_spectrum_ro:function(_62){
this.deviceProxy.readAttribute("ushort_spectrum_ro",_62);
},get_wave:function(_63){
this.deviceProxy.readAttribute("wave",_63);
},get_boolean_image:function(_64){
this.deviceProxy.readAttribute("boolean_image",_64);
},set_boolean_image:function(_65){
this.deviceProxy.writeAttribute("boolean_image",_65);
},get_boolean_image_ro:function(_66){
this.deviceProxy.readAttribute("boolean_image_ro",_66);
},get_double_image:function(_67){
this.deviceProxy.readAttribute("double_image",_67);
},set_double_image:function(_68){
this.deviceProxy.writeAttribute("double_image",_68);
},get_double_image_ro:function(_69){
this.deviceProxy.readAttribute("double_image_ro",_69);
},get_float_image:function(_6a){
this.deviceProxy.readAttribute("float_image",_6a);
},set_float_image:function(_6b){
this.deviceProxy.writeAttribute("float_image",_6b);
},get_float_image_ro:function(_6c){
this.deviceProxy.readAttribute("float_image_ro",_6c);
},get_long64_image_ro:function(_6d){
this.deviceProxy.readAttribute("long64_image_ro",_6d);
},get_long_image:function(_6e){
this.deviceProxy.readAttribute("long_image",_6e);
},set_long_image:function(_6f){
this.deviceProxy.writeAttribute("long_image",_6f);
},get_long_image_ro:function(_70){
this.deviceProxy.readAttribute("long_image_ro",_70);
},get_short_image:function(_71){
this.deviceProxy.readAttribute("short_image",_71);
},set_short_image:function(_72){
this.deviceProxy.writeAttribute("short_image",_72);
},get_short_image_ro:function(_73){
this.deviceProxy.readAttribute("short_image_ro",_73);
},get_string_image:function(_74){
this.deviceProxy.readAttribute("string_image",_74);
},set_string_image:function(_75){
this.deviceProxy.writeAttribute("string_image",_75);
},get_string_image_ro:function(_76){
this.deviceProxy.readAttribute("string_image_ro",_76);
},get_uchar_image:function(_77){
this.deviceProxy.readAttribute("uchar_image",_77);
},set_uchar_image:function(_78){
this.deviceProxy.writeAttribute("uchar_image",_78);
},get_uchar_image_ro:function(_79){
this.deviceProxy.readAttribute("uchar_image_ro",_79);
},get_ulong64_image_ro:function(_7a){
this.deviceProxy.readAttribute("ulong64_image_ro",_7a);
},get_ulong_image_ro:function(_7b){
this.deviceProxy.readAttribute("ulong_image_ro",_7b);
},get_ushort_image:function(_7c){
this.deviceProxy.readAttribute("ushort_image",_7c);
},set_ushort_image:function(_7d){
this.deviceProxy.writeAttribute("ushort_image",_7d);
},get_ushort_image_ro:function(_7e){
this.deviceProxy.readAttribute("ushort_image_ro",_7e);
},get_State:function(_7f){
this.deviceProxy.readAttribute("State",_7f);
},get_Status:function(_80){
this.deviceProxy.readAttribute("Status",_80);
},attributes:function(){
return this.Class.attributes;
},commands:function(){
return this.Class.commands;
}});
;
include.set_path('controllers');
var GlobalContext={proxy:null,readWriteController:null,executeController:null,sampleAppController:null};
mTangoWebController=MVC.Controller.extend("main",{},{load:function(_1){
var _2=$("#settings-tango-url").val();
GlobalContext.proxy=new TangoTest(_2);
var _3=new Page("data_read_write");
_3.load();
var _4=new Page("exec_cmd");
_4.load();
var _5=new Page("smpl_app");
_5.load();
var _6=new ReadWriteController("data_read_write",GlobalContext);
GlobalContext.readWriteController=_6;
var _7=new ExecuteCommandController("exec_cmd",GlobalContext);
GlobalContext.executeController=_7;
var _8=new SampleApplicationController("smpl_app",GlobalContext);
GlobalContext.sampleAppController=_8;
},".submit click":function(_9){
var _a=$(_9.element);
var _b=_a.parents("form");
var _c={};
$.each(_b.serializeArray(),function(i,_e){
_c[_e.name]=_e.value.indexOf(",")>-1?_e.value.split(","):_e.value;
});
this.publish(_a.attr("id")+" clicked",{data:_c});
},"#btnSettingsApply click":function(_f){
$("#settings").popup("close");
var _10=$("#settings-tango-url").val();
GlobalContext.proxy=new TangoTest(_10);
}});
;
include.set_path('controllers');
ReadWriteController=MVC.Controller.Stateful.extend("DataReadWrite",{},{tasks:{},ctx:null,init:function(_1,_2){
this.ctx=_2;
this._super(MVC.$E(_1));
},close:function(){
for(var _3 in this.tasks){
clearInterval(_3);
delete this.tasks[_3];
}
},"#select-attr change":function(_4){
var _5=$(_4.element).val();
this.addAttributePolling({attr:_5});
},addAttributePolling:function(_6){
var me=this;
var _8=setInterval(function(){
me.ctx.proxy["get_"+_6.attr]({onComplete:function(_9){
$("#"+_6.attr+"-"+_8+"-val").html(_9.argout);
}});
},200);
this.tasks[_8]=_6.attr;
this.render({action:"poll-attribute",bottom:"list-attr",using:{attr:_6.attr,taskId:_8}});
$("#list-attr").listview();
$("#list-attr").listview("refresh");
},".removeAttributePolling click":function(_a){
var _b=$(_a.element).attr("id");
var _c=this.tasks[_b];
var _d=$(_a.element).parent();
this.removeAttributePolling({$li:_d,attr:_c,taskId:_b});
},removeAttributePolling:function(_e){
_e.$li.remove();
clearInterval(_e.taskId);
delete _e.tasks[_e.taskId];
},"btnWriteSend clicked subscribe":function(_f){
var _10={};
_10.attr=_f.data["select-attr"];
_10.argin=_f.data["new-value"];
$("#dlgWrite").popup("close");
this.writeAttribute(_10);
},writeAttribute:function(_11){
this.ctx.proxy["set_"+_11.attr]({argin:_11.argin});
}});
;
include.set_path('controllers');
ExecuteCommandController=MVC.Controller.Stateful.extend("ExecuteCommand",{},{ctx:null,init:function(_1,_2){
this.ctx=_2;
this._super(MVC.$E(_1));
},"btnDlgExecSubmit clicked subscribe":function(_3){
var _4={};
_4.cmd=_3.data["select-cmd"];
_4.argin=_3.data["new-value"];
$("#dlgExec").popup("close");
this.executeCommand(_4);
},executeCommand:function(_5){
var me=this;
this.ctx.proxy["execute"+_5.cmd]({argin:_5.argin,onComplete:function(_7){
var _8=MVC.Object.extend({},_7);
_8.cmd=_5.cmd;
_8.argin=_5.argin;
me.addExecuteCommandResult(_8);
}});
},addExecuteCommandResult:function(_9){
this.render({action:"execute-cmd",bottom:"list-cmd",using:_9});
$("#list-cmd").listview();
$("#list-cmd").listview("refresh");
}});
;
include.set_path('controllers');
SampleApplicationController=MVC.Controller.Stateful.extend("SampleApplication",{},{plotData:[],plotOptions:{},isRunning:false,taskId:-1,ctx:null,init:function(_1,_2){
this.ctx=_2;
this._super(MVC.$E(_1));
this.plotOptions={lines:{show:true},points:{show:false},xaxis:{tickDecimals:0,tickSize:1}};
$.plot($("#placeholder"),this.plotData,this.plotOptions);
},"#btnStart click":function(_3){
if(this.isRunning){
this.stopPolling();
this.isRunning=false;
}else{
this.startPolling();
this.isRunning=true;
}
},startPolling:function(){
var _4=0;
var me=this;
function _6(){
me.ctx.proxy.get_double_scalar({onComplete:function(_7){
$("#double_scalar").html(_7.argout);
if(me.plotData.length-1>-1&&me.plotData[me.plotData.length-1][1]==_7.argout){
return;
}
var _8={};
_8["label"]="double_scalar";
me.plotData.push([_4++,_7.argout]);
_8["data"]=me.plotData;
$.plot($("#placeholder"),[_8],me.plotOptions);
}});
};
this.taskId=setInterval(_6,200);
},stopPolling:function(){
clearInterval(this.taskId);
this.plotData=[];
$.plot($("#placeholder"),this.plotData,this.plotOptions);
}});
;
include.set_path('views/data_read_write');
MVC.View.PreCompiledFunction("../views/data_read_write/header.ejs","views/data_read_write/header.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<h1>Data Read/Write</h1>\n");
_3.push("<div data-role=\"navbar\">\n");
_3.push("    <ul>\n");
_3.push("        <li><a href=\"#dlgWrite\" id=\"btnWrite\" data-rel=\"popup\">Write Attribute...</a></li>\n");
_3.push("    </ul>\n");
_3.push("</div><!-- /navbar -->\n");
_3.push("<div data-role=\"popup\" id=\"dlgWrite\" data-theme=\"d\">\n");
_3.push("    <form>\n");
_3.push("        <div style=\"padding:10px 20px;\">\n");
_3.push("            <h4>Write attribute:</h4>\n");
_3.push("            <select name=\"select-attr\" id=\"select-attr\" placeholder=\"Select attribute\">\n");
_3.push("                ");
for(var _4 in GlobalContext.proxy.attributes()){
_3.push("\n");
_3.push("                <option value=\"");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("\">");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("</option>\n");
_3.push("                ");
}
_3.push("\n");
_3.push("            </select>\n");
_3.push("            <input type=\"text\" id=\"dlgWrite_newVal\" name=\"new-value\" size=\"15\" value=\"\" placeholder=\"Enter value...\"/>\n");
_3.push("            <a href=\"#data_read_write\" class=\"submit\" id=\"btnWriteSend\" data-role=\"button\">Send to Tango</a>\n");
_3.push("        </div>\n");
_3.push("    </form>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/data_read_write');
MVC.View.PreCompiledFunction("../views/data_read_write/content.ejs","views/data_read_write/content.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div id=\"DataReadWrite\">\n");
_3.push("        <select name=\"select-attr\" id=\"select-attr\">\n");
_3.push("            <option  data-placeholder=\"true\">Select attribute...</option>\n");
_3.push("            ");
for(var _4 in GlobalContext.proxy.attributes()){
_3.push("\n");
_3.push("            <option value=\"");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("\">");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("</option>\n");
_3.push("            ");
}
_3.push("\n");
_3.push("        </select>\n");
_3.push("    <ul data-role=\"listview\" data-inset=\"true\" data-filter=\"false\" id=\"list-attr\">\n");
_3.push("    </ul>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/data_read_write');
MVC.View.PreCompiledFunction("../views/data_read_write/footer.ejs","views/data_read_write/footer.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div data-role=\"navbar\">\n");
_3.push("    <ul>\n");
_3.push("        <li><a href=\"#data_read_write\" data-role=\"tab\" data-icon=\"grid\">Data Read/Write</a></li>\n");
_3.push("        <li><a href=\"#exec_cmd\" data-role=\"tab\" data-icon=\"grid\">Execute Command</a></li>\n");
_3.push("        <li><a href=\"#smpl_app\" data-role=\"tab\" data-icon=\"grid\">Sample App</a></li>\n");
_3.push("    </ul>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/exec_cmd');
MVC.View.PreCompiledFunction("../views/exec_cmd/header.ejs","views/exec_cmd/header.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<h1>Execute Command</h1>\n");
_3.push("<div data-role=\"navbar\">\n");
_3.push("    <ul>\n");
_3.push("        <li><a href=\"#dlgExec\" id=\"btnWrite\" data-rel=\"popup\">Execute command...</a></li>\n");
_3.push("    </ul>\n");
_3.push("</div><!-- /navbar -->\n");
_3.push("<div data-role=\"popup\" id=\"dlgExec\" data-theme=\"d\">\n");
_3.push("    <form>\n");
_3.push("        <div style=\"padding:10px 20px;\">\n");
_3.push("            <h4>Write attribute:</h4>\n");
_3.push("            <select name=\"select-cmd\" id=\"select-cmd\">\n");
_3.push("                <option data-placeholder=\"true\">Select command...</option>\n");
_3.push("                ");
for(var _4 in GlobalContext.proxy.commands()){
_3.push("\n");
_3.push("                <option value=\"");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("\">");
_3.push((MVC.View.Scanner.to_text(_4)));
_3.push("</option>\n");
_3.push("                ");
}
_3.push("\n");
_3.push("            </select>\n");
_3.push("            <label for=\"dlgExec-arg\">Tip: arrays - comma separated values, i.e. Hello,World,!!!</label>\n");
_3.push("            <input type=\"text\" id=\"dlgExec-arg\" name=\"new-value\" size=\"15\" value=\"\" placeholder=\"Enter value...\"/>\n");
_3.push("            <a href=\"#exec_cmd\" class=\"submit\" id=\"btnDlgExecSubmit\" data-role=\"button\">Execute</a>\n");
_3.push("        </div>\n");
_3.push("    </form>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/exec_cmd');
MVC.View.PreCompiledFunction("../views/exec_cmd/content.ejs","views/exec_cmd/content.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div id=\"ExecuteCommand\">\n");
_3.push("    <ul data-role=\"listview\" data-inset=\"true\" data-filter=\"false\" id=\"list-cmd\">\n");
_3.push("    </ul>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/exec_cmd');
MVC.View.PreCompiledFunction("../views/exec_cmd/footer.ejs","views/exec_cmd/footer.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div data-role=\"navbar\">\n");
_3.push("    <ul>\n");
_3.push("        <li><a href=\"#data_read_write\" data-role=\"tab\" data-icon=\"grid\">Data Read/Write</a></li>\n");
_3.push("        <li><a href=\"#exec_cmd\" data-role=\"tab\" data-icon=\"grid\">Execute Command</a></li>\n");
_3.push("        <li><a href=\"#smpl_app\" data-role=\"tab\" data-icon=\"grid\">Sample App</a></li>\n");
_3.push("    </ul>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/smpl_app');
MVC.View.PreCompiledFunction("../views/smpl_app/header.ejs","views/smpl_app/header.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<h1>Sample Application</h1>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/smpl_app');
MVC.View.PreCompiledFunction("../views/smpl_app/content.ejs","views/smpl_app/content.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div id=\"SampleApplication\">\n");
_3.push("<button id=\"btnStart\">Start/Stop</button>\n");
_3.push("<h4 id=\"double_scalar\"></h4>\n");
_3.push("<div id=\"placeholder\" style=\"width:100%;height:100%;\"></div>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/smpl_app');
MVC.View.PreCompiledFunction("../views/smpl_app/footer.ejs","views/smpl_app/footer.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<div data-role=\"navbar\">\n");
_3.push("    <ul>\n");
_3.push("        <li><a href=\"#data_read_write\" data-role=\"tab\" data-icon=\"grid\">Data Read/Write</a></li>\n");
_3.push("        <li><a href=\"#exec_cmd\" data-role=\"tab\" data-icon=\"grid\">Execute Command</a></li>\n");
_3.push("        <li><a href=\"#smpl_app\" data-role=\"tab\" data-icon=\"grid\">Sample App</a></li>\n");
_3.push("    </ul>\n");
_3.push("</div>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/DataReadWrite');
MVC.View.PreCompiledFunction("../views/DataReadWrite/poll-attribute.ejs","views/DataReadWrite/poll-attribute.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<li><a href=\"#\">");
_3.push((MVC.View.Scanner.to_text(attr)));
_3.push("=<span class=\"ui-title float\" id=\"");
_3.push((MVC.View.Scanner.to_text(attr)));
_3.push("-");
_3.push((MVC.View.Scanner.to_text(taskId)));
_3.push("-val\"></span></a><a href=\"#\" class=\"removeAttributePolling\" data-role=\"button\" data-mini=\"true\" data-icon=\"delete\" id=\"");
_3.push((MVC.View.Scanner.to_text(taskId)));
_3.push("\"></a></li>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.set_path('views/ExecuteCommand');
MVC.View.PreCompiledFunction("../views/ExecuteCommand/execute-cmd.ejs","views/ExecuteCommand/execute-cmd.ejs",function(_1,_2){
try{
with(_2){
with(_1){
var _3=[];
_3.push("<li><a href=\"#\">");
_3.push((MVC.View.Scanner.to_text(cmd)));
_3.push("(");
_3.push((MVC.View.Scanner.to_text(argin)));
_3.push(")=");
_3.push((MVC.View.Scanner.to_text(argout)));
_3.push("</a></li>");
return _3.join("");
}
}
}
catch(e){
e.lineNumber=null;
throw e;
}
});
;
include.end_of_production();