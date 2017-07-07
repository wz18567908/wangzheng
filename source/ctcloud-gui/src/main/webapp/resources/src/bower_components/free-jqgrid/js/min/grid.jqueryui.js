(function(b){"function"===typeof define&&define.amd?define("jquery ./grid.base jquery-ui/dialog jquery-ui/draggable jquery-ui/droppable jquery-ui/resizable jquery-ui/sortable".split(" "),b):"object"===typeof exports?b(require("jquery")):b(jQuery)})(function(b){var l=b.jgrid,t=l.jqID,h=null!=b.ui?b.ui.multiselect:null;l.msie&&8===l.msiever()&&(b.expr[":"].hidden=function(b){return 0===b.offsetWidth||0===b.offsetHeight||"none"===b.style.display});l._multiselect=!1;if(h){if(h.prototype._setSelected){var B=
h.prototype._setSelected;h.prototype._setSelected=function(a,g){var e=this,c=B.call(e,a,g);if(g&&e.selectedList){var f=e.element;e.selectedList.find("li").each(function(){b(e).data("optionLink")&&b(e).data("optionLink").remove().appendTo(f)})}return c}}h.prototype.destroy&&(h.prototype.destroy=function(){this.element.show();this.container.remove();void 0===b.Widget?b.widget.prototype.destroy.apply(this,arguments):b.Widget.prototype.destroy.apply(this,arguments)});l._multiselect=!0}l.extend({sortableColumns:function(a){return this.each(function(){function g(){c.disableClick=
!0}var e=this,c=e.p,f=t(c.id),f={tolerance:"pointer",axis:"x",scrollSensitivity:"1",items:">th:not(:has(#jqgh_"+f+"_cb,#jqgh_"+f+"_rn,#jqgh_"+f+"_subgrid),:hidden)",placeholder:{element:function(a){return b(document.createElement(a[0].nodeName)).addClass(a[0].className+" ui-sortable-placeholder ui-state-highlight").removeClass("ui-sortable-helper")[0]},update:function(b,a){a.height(b.currentItem.innerHeight()-parseInt(b.currentItem.css("paddingTop")||0,10)-parseInt(b.currentItem.css("paddingBottom")||
0,10));a.width(b.currentItem.innerWidth()-parseInt(b.currentItem.css("paddingLeft")||0,10)-parseInt(b.currentItem.css("paddingRight")||0,10))}},update:function(a,k){var d=b(">th",b(k.item).parent()),f=c.id+"_",g=[];d.each(function(){var a=b(">div",this).get(0).id.replace(/^jqgh_/,"").replace(f,""),a=c.iColByName[a];void 0!==a&&g.push(a)});b(e).jqGrid("remapColumns",g,!0,!0);b.isFunction(c.sortable.update)&&c.sortable.update(g);setTimeout(function(){c.disableClick=!1},50)}};c.sortable.options?b.extend(f,
c.sortable.options):b.isFunction(c.sortable)&&(c.sortable={update:c.sortable});if(f.start){var k=f.start;f.start=function(b,a){g();k.call(this,b,a)}}else f.start=g;c.sortable.exclude&&(f.items+=":not("+c.sortable.exclude+")");f=a.sortable(f);f=f.data("sortable")||f.data("uiSortable")||f.data("ui-sortable");null!=f&&(f.floating=!0)})},columnChooser:function(a){function g(a,c){a&&("string"===typeof a?b.fn[a]&&b.fn[a].apply(c,b.makeArray(arguments).slice(2)):b.isFunction(a)&&a.apply(c,b.makeArray(arguments).slice(2)))}
var e=this,c=e[0].p,f,k,v={},u=[],d,p,n=c.colModel;d=n.length;p=c.colNames;var z=function(b){return h&&h.prototype&&b.data(h.prototype.widgetFullName||h.prototype.widgetName)||b.data("ui-multiselect")||b.data("multiselect")};if(!b("#colchooser_"+t(c.id)).length){f=b('<div id="colchooser_'+c.id+'" style="position:relative;overflow:hidden"><div><select multiple="multiple"></select></div></div>');k=b("select",f);a=b.extend({width:400,height:240,classname:null,done:function(b){b&&null==c.groupHeader&&
e.jqGrid("remapColumns",b,!0)},msel:"multiselect",dlog:"dialog",dialog_opts:{minWidth:470,dialogClass:"ui-jqdialog"},dlog_opts:function(a){var c={};c[a.bSubmit]=function(){a.apply_perm();a.cleanup(!1)};c[a.bCancel]=function(){a.cleanup(!0)};return b.extend(!0,{buttons:c,close:function(){a.cleanup(!0)},modal:a.modal||!1,resizable:a.resizable||!0,width:a.width+70,resize:function(){var b=z(k),a=b.container.closest(".ui-dialog-content");0<a.length&&"object"===typeof a[0].style?a[0].style.width="":a.css("width",
"");b.selectedList.height(Math.max(b.selectedContainer.height()-b.selectedActions.outerHeight()-1,1));b.availableList.height(Math.max(b.availableContainer.height()-b.availableActions.outerHeight()-1,1))}},a.dialog_opts||{})},apply_perm:function(){var d=[],f={skipSetGridWidth:!0,skipSetGroupHeaders:!0};b("option",k).each(function(){b(this).is(":selected")?e.jqGrid("showCol",n[this.value].name,f):e.jqGrid("hideCol",n[this.value].name,f)});if(c.groupHeader&&("object"===typeof c.groupHeader||b.isFunction(c.groupHeader)))if(e.jqGrid("destroyGroupHeader",
!1),null!=c.pivotOptions&&null!=c.pivotOptions.colHeaders&&1<c.pivotOptions.colHeaders.length){var g,u=c.pivotOptions.colHeaders;for(g=0;g<u.length;g++)u[g]&&u[g].groupHeaders.length&&e.jqGrid("setGroupHeaders",u[g])}else e.jqGrid("setGroupHeaders",c.groupHeader);b("option",k).filter(":selected").each(function(){d.push(parseInt(this.value,10))});b.each(d,function(){delete v[n[parseInt(this,10)].name]});b.each(v,function(){var b=parseInt(this,10);var a=d,c=b,k,e;0<=c?(k=a.slice(),e=k.splice(c,Math.max(a.length-
c,c)),c>a.length&&(c=a.length),k[c]=b,d=k.concat(e)):d=a});a.done&&a.done.call(e,d);e.jqGrid("setGridWidth",c.autowidth||void 0!==c.widthOrg&&"auto"!==c.widthOrg&&"100%"!==c.widthOrg?c.width:c.tblwidth,c.shrinkToFit)},cleanup:function(b){g(a.dlog,f,"destroy");g(a.msel,k,"destroy");f.remove();b&&a.done&&a.done.call(e)},msel_opts:{}},e.jqGrid("getGridRes","col"),l.col,a||{});if(b.ui&&h&&h.defaults){if(!l._multiselect){alert("Multiselect plugin loaded after jqGrid. Please load the plugin before the jqGrid!");
return}a.msel_opts=b.extend(h.defaults,a.msel_opts)}a.caption&&f.attr("title",a.caption);a.classname&&(f.addClass(a.classname),k.addClass(a.classname));a.width&&(b(">div",f).css({width:a.width,margin:"0 auto"}),k.css("width",a.width));a.height&&(b(">div",f).css("height",a.height),k.css("height",a.height-10));k.empty();var y=c.groupHeader,r={},q,w,A,m,x;if(null!=y&&null!=y.groupHeaders)for(q=0,A=y.groupHeaders.length;q<A;q++)for(x=y.groupHeaders[q],w=0;w<x.numberOfColumns;w++)m=c.iColByName[x.startColumnName]+
w,r[m]=b.isFunction(a.buildItemText)?a.buildItemText.call(e[0],{iCol:m,cm:n[m],cmName:n[m].name,colName:p[m],groupTitleText:x.titleText}):b.jgrid.stripHtml(x.titleText)+": "+b.jgrid.stripHtml(""===p[m]?n[m].name:p[m]);for(m=0;m<d;m++)void 0===r[m]&&(r[m]=b.isFunction(a.buildItemText)?a.buildItemText.call(e[0],{iCol:m,cm:n[m],cmName:n[m].name,colName:p[m],groupTitleText:null}):b.jgrid.stripHtml(p[m]));b.each(n,function(b){v[this.name]=b;this.hidedlg?this.hidden||u.push(b):k.append("<option value='"+
b+"'"+(c.headertitles||this.headerTitle?" title='"+l.stripHtml("string"===typeof this.headerTitle?this.headerTitle:r[b])+"'":"")+(this.hidden?"":" selected='selected'")+">"+r[b]+"</option>")});d=b.isFunction(a.dlog_opts)?a.dlog_opts.call(e,a):a.dlog_opts;g(a.dlog,f,d);d=b.isFunction(a.msel_opts)?a.msel_opts.call(e,a):a.msel_opts;g(a.msel,k,d);d=b("#colchooser_"+t(c.id));d.css({margin:"auto"});d.find(">div").css({width:"100%",height:"100%",margin:"auto"});if(d=z(k))d.container.css({width:"100%",height:"100%",
margin:"auto"}),d.selectedContainer.css({width:100*d.options.dividerLocation+"%",height:"100%",margin:"auto",boxSizing:"border-box"}),d.availableContainer.css({width:100-100*d.options.dividerLocation+"%",height:"100%",margin:"auto",boxSizing:"border-box"}),d.selectedList.css("height","auto"),d.availableList.css("height","auto"),p=Math.max(d.selectedList.height(),d.availableList.height()),p=Math.min(p,b(window).height()),d.selectedList.css("height",p),d.availableList.css("height",p)}},sortableRows:function(a){return this.each(function(){var g=
this,e=g.grid,c=g.p;e&&!c.treeGrid&&b.fn.sortable&&(a=b.extend({cursor:"move",axis:"y",items:">.jqgrow"},a||{}),a.start&&b.isFunction(a.start)?(a._start_=a.start,delete a.start):a._start_=!1,a.update&&b.isFunction(a.update)?(a._update_=a.update,delete a.update):a._update_=!1,a.start=function(f,k){b(k.item).css("border-width","0");b("td",k.item).each(function(b){this.style.width=e.cols[b].style.width});if(c.subGrid){var v=b(k.item).attr("id");try{b(g).jqGrid("collapseSubGridRow",v)}catch(u){}}a._start_&&
a._start_.apply(this,[f,k])},a.update=function(e,k){b(k.item).css("border-width","");!0===c.rownumbers&&b("td.jqgrid-rownum",g.rows).each(function(a){b(this).html(a+1+(parseInt(c.page,10)-1)*parseInt(c.rowNum,10))});a._update_&&a._update_.apply(this,[e,k])},b(g.tBodies[0]).sortable(a),b.isFunction(b.fn.disableSelection)&&b(g.tBodies[0]).children("tr.jqgrow").disableSelection())})},gridDnD:function(a){return this.each(function(){function g(){var a=b.data(e,"dnd");b("tr.jqgrow:not(.ui-draggable)",e).draggable(b.isFunction(a.drag)?
a.drag.call(b(e),a):a.drag)}var e=this,c,f;if(e.grid&&!e.p.treeGrid&&b.fn.draggable&&b.fn.droppable)if(void 0===b("#jqgrid_dnd")[0]&&b("body").append("<table id='jqgrid_dnd' class='ui-jqgrid-dnd'></table>"),"string"===typeof a&&"updateDnD"===a&&!0===e.p.jqgdnd)g();else if(a=b.extend({drag:function(a){return b.extend({start:function(c,f){var d;if(e.p.subGrid){d=b(f.helper).attr("id");try{b(e).jqGrid("collapseSubGridRow",d)}catch(g){}}for(d=0;d<b.data(e,"dnd").connectWith.length;d++)0===b(b.data(e,
"dnd").connectWith[d]).jqGrid("getGridParam","reccount")&&b(b.data(e,"dnd").connectWith[d]).jqGrid("addRowData","jqg_empty_row",{});f.helper.addClass("ui-state-highlight");b("td",f.helper).each(function(b){this.style.width=e.grid.headers[b].width+"px"});a.onstart&&b.isFunction(a.onstart)&&a.onstart.call(b(e),c,f)},stop:function(c,f){var d;f.helper.dropped&&!a.dragcopy&&(d=b(f.helper).attr("id"),void 0===d&&(d=b(this).attr("id")),b(e).jqGrid("delRowData",d));for(d=0;d<b.data(e,"dnd").connectWith.length;d++)b(b.data(e,
"dnd").connectWith[d]).jqGrid("delRowData","jqg_empty_row");a.onstop&&b.isFunction(a.onstop)&&a.onstop.call(b(e),c,f)}},a.drag_opts||{})},drop:function(a){return b.extend({accept:function(a){if(!b(a).hasClass("jqgrow"))return a;a=b(a).closest("table.ui-jqgrid-btable");return 0<a.length&&void 0!==b.data(a[0],"dnd")?(a=b.data(a[0],"dnd").connectWith,-1!==b.inArray("#"+t(this.id),a)?!0:!1):!1},drop:function(c,f){if(b(f.draggable).hasClass("jqgrow")){var d=b(f.draggable).attr("id"),d=f.draggable.parent().parent().jqGrid("getRowData",
d);if(!a.dropbyname){var g=0,n={},h,l,r=b("#"+t(this.id)).jqGrid("getGridParam","colModel");try{for(l in d)d.hasOwnProperty(l)&&(h=r[g].name,"cb"!==h&&"rn"!==h&&"subgrid"!==h&&d.hasOwnProperty(l)&&r[g]&&(n[h]=d[l]),g++);d=n}catch(w){}}f.helper.dropped=!0;a.beforedrop&&b.isFunction(a.beforedrop)&&(h=a.beforedrop.call(this,c,f,d,b("#"+t(e.p.id)),b(this)),void 0!==h&&null!==h&&"object"===typeof h&&(d=h));if(f.helper.dropped){var q;a.autoid&&(b.isFunction(a.autoid)?q=a.autoid.call(this,d):(q=Math.ceil(1E3*
Math.random()),q=a.autoidprefix+q));b("#"+t(this.id)).jqGrid("addRowData",q,d,a.droppos);d[e.p.localReader.id]=q}a.ondrop&&b.isFunction(a.ondrop)&&a.ondrop.call(this,c,f,d)}}},a.drop_opts||{})},onstart:null,onstop:null,beforedrop:null,ondrop:null,drop_opts:{},drag_opts:{revert:"invalid",helper:"clone",cursor:"move",appendTo:"#jqgrid_dnd",zIndex:5E3},dragcopy:!1,dropbyname:!1,droppos:"first",autoid:!0,autoidprefix:"dnd_"},a||{}),a.connectWith)for(a.connectWith=a.connectWith.split(","),a.connectWith=
b.map(a.connectWith,function(a){return b.trim(a)}),b.data(e,"dnd",a),0===e.p.reccount||e.p.jqgdnd||g(),e.p.jqgdnd=!0,c=0;c<a.connectWith.length;c++)f=a.connectWith[c],b(f).droppable(b.isFunction(a.drop)?a.drop.call(b(e),a):a.drop)})},gridResize:function(a){return this.each(function(){var g=this,e=g.grid,c=g.p,f=c.gView+">.ui-jqgrid-bdiv",k=!1,h,l=c.height;if(e&&b.fn.resizable){a=b.extend({},a||{});a.alsoResize?(a._alsoResize_=a.alsoResize,delete a.alsoResize):a._alsoResize_=!1;a.stop&&b.isFunction(a.stop)?
(a._stop_=a.stop,delete a.stop):a._stop_=!1;a.stop=function(d,n){b(g).jqGrid("setGridWidth",n.size.width,a.shrinkToFit);b(c.gView+">.ui-jqgrid-titlebar").css("width","");k?(b(h).each(function(){b(this).css("height","")}),"auto"!==l&&"100%"!==l||b(e.bDiv).css("height",l)):b(g).jqGrid("setGridParam",{height:b(f).height()});g.fixScrollOffsetAndhBoxPadding&&g.fixScrollOffsetAndhBoxPadding();a._stop_&&a._stop_.call(g,d,n)};h=f;"auto"!==l&&"100%"!==l||void 0!==a.handles||(a.handles="e,w");if(a.handles){var d=
b.map(String(a.handles).split(","),function(a){return b.trim(a)});2===d.length&&("e"===d[0]&&"w"===d[1]||"e"===d[1]&&"w"===d[1])&&(h=c.gView+">div:not(.frozen-div)",k=!0,c.pager&&(h+=","+c.pager))}a.alsoResize=a._alsoResize_?h+","+a._alsoResize_:h;delete a._alsoResize_;b(c.gBox).resizable(a)}})}})});
//# sourceMappingURL=grid.jqueryui.map
