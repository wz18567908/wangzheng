(function(e){"function"===typeof define&&define.amd?define(["jquery","./grid.base"],e):"object"===typeof exports?e(require("jquery")):e(jQuery)})(function(e){e.jgrid=e.jgrid||{};var p=e.jgrid,v=p.getMethod("getGridRes"),y=e.fn.jqGrid;e.fmatter=e.fmatter||{};var q=e.fmatter,x=function(a,b){var c=a.formatoptions||{};return c.hasOwnProperty(b)?c[b]:(a.editoptions||{})[b]},r=function(a){return String(a).replace(/\'/g,"&#39;")},z=function(a){var b=a.colModel||a.cm,c,e=!1!==b.title?" title='"+r(a.colName||
b.name)+"'":"";a=x(b,"checkedClass");c=x(b,"uncheckedClass");var d=x(b,"value"),f="string"===typeof d?d.split(":")[0]||"Yes":"Yes",d="string"===typeof d?d.split(":")[1]||"No":"No",k=function(a){return"<i class='"+r(a)+"'"+e+"></i>"},b=x(b,"disabled");void 0===b&&(b=p.formatter.checkbox.disabled);!0===b&&y.isInCommonIconClass.call(this,"fa")?(a=a||"fa fa-check-square-o fa-lg",b=k(a),c=k(c||"fa fa-square-o fa-lg")):!0===b&&y.isInCommonIconClass.call(this,"glyphicon")?(a=a||"glyphicon glyphicon-check",
b=k(a),c=k(c||"glyphicon glyphicon-unchecked")):(a="",e+=!0===b?" disabled='disabled'":"",b="<input type='checkbox' checked='checked'"+e+" />",c="<input type='checkbox'"+e+" />");return{checkedClasses:a,checked:b,unchecked:c,yes:f,no:d}},D={1:1,x:1,"true":1,yes:1,on:1},F={0:1,"false":1,no:1,off:1};e.extend(!0,p,{formatter:{date:{parseRe:/[#%\\\/:_;.,\t\s\-]/,masks:{ISO8601Long:"Y-m-d H:i:s",ISO8601Short:"Y-m-d",SortableDateTime:"Y-m-d\\TH:i:s",UniversalSortableDateTime:"Y-m-d H:i:sO"},reformatAfterEdit:!0,
userLocalTime:!1},baseLinkUrl:"",showAction:"",target:"",checkbox:{disabled:!0},idName:"id"},cmTemplate:{integerStr:{formatter:"integer",align:"right",sorttype:"integer",searchoptions:{sopt:"eq ne lt le gt ge".split(" ")}},integer:{formatter:"integer",align:"right",sorttype:"integer",convertOnSave:function(a){a=a.newValue;return isNaN(a)?a:parseInt(a,10)},searchoptions:{sopt:"eq ne lt le gt ge".split(" ")}},numberStr:{formatter:"number",align:"right",sorttype:"number",searchoptions:{sopt:"eq ne lt le gt ge".split(" ")}},
number:{formatter:"number",align:"right",sorttype:"number",convertOnSave:function(a){a=a.newValue;return isNaN(a)?a:parseFloat(a)},searchoptions:{sopt:"eq ne lt le gt ge".split(" ")}},booleanCheckbox:{align:"center",formatter:"checkbox",edittype:"checkbox",editoptions:{value:"true:false",defaultValue:"false"},convertOnSave:function(a){var b=a.newValue;a=z.call(this,a);var c=String(b).toLowerCase();if(D[c]||c===a.yes.toLowerCase())b=!0;else if(F[c]||c===a.no.toLowerCase())b=!1;return b},stype:"select",
searchoptions:{sopt:["eq","ne"],value:"true:Yes;false:No",noFilterText:"Any"}},actions:function(){return{formatter:"actions",width:(null!=this.p&&(y.isInCommonIconClass.call(this,"fa")||y.isInCommonIconClass.call(this,"glyphicon"))?e(this).jqGrid("isBootstrapGuiStyle")?45:39:40)+(p.cellWidth()?5:0),align:"center",label:"",autoResizable:!1,frozen:!0,fixed:!0,hidedlg:!0,resizable:!1,sortable:!1,search:!1,editable:!1,viewable:!1}}}});p.cmTemplate.booleanCheckboxFa=p.cmTemplate.booleanCheckbox;e.extend(q,
{isObject:function(a){return a&&("object"===typeof a||e.isFunction(a))||!1},isNumber:function(a){return"number"===typeof a&&isFinite(a)},isValue:function(a){return this.isObject(a)||"string"===typeof a||this.isNumber(a)||"boolean"===typeof a},isEmpty:function(a){if("string"!==typeof a&&this.isValue(a))return!1;if(!this.isValue(a))return!0;a=e.trim(a).replace(/&nbsp;/ig,"").replace(/&#160;/ig,"");return""===a},NumberFormat:function(a,b){var c=q.isNumber;c(a)||(a*=1);if(c(a)){var e=0>a,d=String(a),
f=b.decimalSeparator||".";if(c(b.decimalPlaces)){var k=b.decimalPlaces,d=Math.pow(10,k),d=String(Math.round(a*d)/d),c=d.lastIndexOf(".");if(0<k)for(0>c?(d+=f,c=d.length-1):"."!==f&&(d=d.replace(".",f));d.length-1-c<k;)d+="0"}if(b.thousandsSeparator){var k=b.thousandsSeparator,c=d.lastIndexOf(f),c=-1<c?c:d.length,f=void 0===b.decimalSeparator?"":d.substring(c),m=-1,n;for(n=c;0<n;n--)m++,0===m%3&&n!==c&&(!e||1<n)&&(f=k+f),f=d.charAt(n-1)+f;d=f}return d}return a}});var l=function(a,b,c,g,d){var f=b;
c=e.extend({},v.call(e(this),"formatter"),c);try{f=e.fn.fmatter[a].call(this,b,c,g,d)}catch(k){}return f};e.fn.fmatter=l;l.getCellBuilder=function(a,b,c){a=null!=e.fn.fmatter[a]?e.fn.fmatter[a].getCellBuilder:null;return e.isFunction(a)?a.call(this,e.extend({},v.call(e(this),"formatter"),b),c):null};l.defaultFormat=function(a,b){return q.isValue(a)&&""!==a?a:b.defaultValue||"&#160;"};var t=l.defaultFormat,E=function(a,b,c){if(void 0===a||q.isEmpty(a))a=x(c,"defaultValue");a=String(a).toLowerCase();
return D[a]||a===b.yes.toLowerCase()?b.checked:b.unchecked};l.email=function(a,b){return q.isEmpty(a)?t(a,b):"<a href='mailto:"+r(a)+"'>"+a+"</a>"};l.checkbox=function(a,b){var c=z.call(this,b);return E(a,c,b.colModel)};l.checkbox.getCellBuilder=function(a){var b,c=a.colModel;a.colName=a.colName||this.p.colNames[a.pos];b=z.call(this,a);return function(a){return E(a,b,c)}};l.checkbox.unformat=function(a,b,c){a=z.call(this,b);c=e(c);return(a.checkedClasses?p.hasAllClasses(c.children("i"),a.checkedClasses):
c.children("input").is(":checked"))?a.yes:a.no};l.checkboxFontAwesome4=l.checkbox;l.checkboxFontAwesome4.getCellBuilder=l.checkbox.getCellBuilder;l.checkboxFontAwesome4.unformat=l.checkbox.unformat;l.link=function(a,b){var c=b.colModel,g="",d={target:b.target};null!=c&&(d=e.extend({},d,c.formatoptions||{}));d.target&&(g="target="+d.target);return q.isEmpty(a)?t(a,d):"<a "+g+" href='"+r(a)+"'>"+a+"</a>"};l.showlink=function(a,b,c){var g=this,d=b.colModel,f={baseLinkUrl:b.baseLinkUrl,showAction:b.showAction,
addParam:b.addParam||"",target:b.target,idName:b.idName,hrefDefaultValue:"#"},k="",m,n,h=function(d){return e.isFunction(d)?d.call(g,{cellValue:a,rowid:b.rowId,rowData:c,options:f}):d||""};null!=d&&(f=e.extend({},f,d.formatoptions||{}));f.target&&(k="target="+h(f.target));d=h(f.baseLinkUrl)+h(f.showAction);m=f.idName?encodeURIComponent(h(f.idName))+"="+encodeURIComponent(h(f.rowId)||b.rowId):"";n=h(f.addParam);"object"===typeof n&&null!==n&&(n=(""!==m?"&":"")+e.param(n));d+=m||n?"?"+m+n:"";""===d&&
(d=h(f.hrefDefaultValue));return"string"===typeof a||q.isNumber(a)||e.isFunction(f.cellValue)?"<a "+k+" href='"+r(d)+"'>"+(e.isFunction(f.cellValue)?h(f.cellValue):a)+"</a>":t(a,f)};l.showlink.getCellBuilder=function(a){var b={baseLinkUrl:a.baseLinkUrl,showAction:a.showAction,addParam:a.addParam||"",target:a.target,idName:a.idName,hrefDefaultValue:"#"};a=a.colModel;null!=a&&(b=e.extend({},b,a.formatoptions||{}));return function(a,g,d){var f=this,k=g.rowId,m="",n,h,l=function(g){return e.isFunction(g)?
g.call(f,{cellValue:a,rowid:k,rowData:d,options:b}):g||""};b.target&&(m="target="+l(b.target));n=l(b.baseLinkUrl)+l(b.showAction);g=b.idName?encodeURIComponent(l(b.idName))+"="+encodeURIComponent(l(k)||g.rowId):"";h=l(b.addParam);"object"===typeof h&&null!==h&&(h=(""!==g?"&":"")+e.param(h));n+=g||h?"?"+g+h:"";""===n&&(n=l(b.hrefDefaultValue));return"string"===typeof a||q.isNumber(a)||e.isFunction(b.cellValue)?"<a "+m+" href='"+r(n)+"'>"+(e.isFunction(b.cellValue)?l(b.cellValue):a)+"</a>":t(a,b)}};
l.showlink.pageFinalization=function(a){var b=e(this),c=this.p,g=c.colModel[a],d,f=this.rows,k=f.length,m,n=function(c){var d=e(this).closest(".jqgrow");if(0<d.length)return g.formatoptions.onClick.call(b[0],{iCol:a,iRow:d[0].rowIndex,rowid:d.attr("id"),cm:g,cmName:g.name,cellValue:e(this).text(),a:this,event:c})};if(null!=g.formatoptions&&e.isFunction(g.formatoptions.onClick))for(d=0;d<k;d++)m=f[d],e(m).hasClass("jqgrow")&&(m=m.cells[a],g.autoResizable&&null!=m&&e(m.firstChild).hasClass(c.autoResizing.wrapperClassName)&&
(m=m.firstChild),null!=m&&e(m.firstChild).bind("click",n))};var A=function(a,b){a=b.prefix?b.prefix+a:a;return b.suffix?a+b.suffix:a},B=function(a,b,c){var g=b.colModel;b=e.extend({},b[c]);null!=g&&(b=e.extend({},b,g.formatoptions||{}));return q.isEmpty(a)?A(b.defaultValue,b):A(q.NumberFormat(a,b),b)};l.integer=function(a,b){return B(a,b,"integer")};l.number=function(a,b){return B(a,b,"number")};l.currency=function(a,b){return B(a,b,"currency")};var C=function(a,b){var c=a.colModel,g=e.extend({},
a[b]);null!=c&&(g=e.extend({},g,c.formatoptions||{}));var d=q.NumberFormat,f=g.defaultValue?A(g.defaultValue,g):"";return function(a){return q.isEmpty(a)?f:A(d(a,g),g)}};l.integer.getCellBuilder=function(a){return C(a,"integer")};l.number.getCellBuilder=function(a){return C(a,"number")};l.currency.getCellBuilder=function(a){return C(a,"currency")};l.date=function(a,b,c,g){c=b.colModel;b=e.extend({},b.date);null!=c&&(b=e.extend({},b,c.formatoptions||{}));return b.reformatAfterEdit||"edit"!==g?q.isEmpty(a)?
t(a,b):p.parseDate.call(this,b.srcformat,a,b.newformat,b):t(a,b)};l.date.getCellBuilder=function(a,b){var c=e.extend({},a.date);null!=a.colModel&&(c=e.extend({},c,a.colModel.formatoptions||{}));var g=p.parseDate,d=c.srcformat,f=c.newformat;return c.reformatAfterEdit||"edit"!==b?function(a){return q.isEmpty(a)?t(a,c):g.call(this,d,a,f,c)}:function(a){return t(a,c)}};l.select=function(a,b){var c=[],g=b.colModel,d,g=e.extend({},g.editoptions||{},g.formatoptions||{}),f=g.value,k=g.separator||":",m=g.delimiter||
";";if(f){var n=!0===g.multiple?!0:!1,h=[],l=function(a,b){if(0<b)return a};n&&(h=e.map(String(a).split(","),function(a){return e.trim(a)}));if("string"===typeof f){var u=f.split(m),w,p;for(w=0;w<u.length;w++)if(m=u[w].split(k),2<m.length&&(m[1]=e.map(m,l).join(k)),p=e.trim(m[0]),g.defaultValue===p&&(d=m[1]),n)-1<e.inArray(p,h)&&c.push(m[1]);else if(p===e.trim(a)){c=[m[1]];break}}else q.isObject(f)&&(d=f[g.defaultValue],c=n?e.map(h,function(a){return f[a]}):[void 0===f[a]?"":f[a]])}a=c.join(", ");
return""!==a?a:void 0!==g.defaultValue?d:t(a,g)};l.select.getCellBuilder=function(a){a=a.colModel;var b=l.defaultFormat,c=e.extend({},a.editoptions||{},a.formatoptions||{}),g=c.value;a=c.separator||":";var d=c.delimiter||";",f,k=void 0!==c.defaultValue,m=!0===c.multiple?!0:!1,n,h={},p=function(a,b){if(0<b)return a};if("string"===typeof g)for(g=g.split(d),d=g.length,n=d-1;0<=n;n--)d=g[n].split(a),2<d.length&&(d[1]=e.map(d,p).join(a)),h[e.trim(d[0])]=d[1];else if(q.isObject(g))h=g;else return function(a){return a?
String(a):b(a,c)};k&&(f=h[c.defaultValue]);return m?function(a){var d=[],g,n=e.map(String(a).split(","),function(a){return e.trim(a)});for(g=0;g<n.length;g++)a=n[g],h.hasOwnProperty(a)&&d.push(h[a]);a=d.join(", ");return""!==a?a:k?f:b(a,c)}:function(a){var d=h[String(a)];return""!==d&&void 0!==d?d:k?f:b(a,c)}};l.rowactions=function(a,b){var c=e(this).closest("tr.jqgrow"),g=c.attr("id"),d=e(this).closest("table.ui-jqgrid-btable").attr("id").replace(/_frozen([^_]*)$/,"$1"),f=e("#"+p.jqID(d)),d=f[0],
k=d.p,m,n;m=function(){var a=c[0],b=f.closest(".ui-jqgrid")[0];return null!=a.getBoundingClientRect&&null!=b.getBoundingClientRect?a.getBoundingClientRect().top+c.outerHeight()-b.getBoundingClientRect().top:c.offset().top+c.outerHeight()-e(b).offset().top};var h=k.colModel[p.getCellIndex(this)],h=e.extend(!0,{extraparam:{}},p.actionsNav||{},k.actionsNavOptions||{},h.formatoptions||{});void 0!==k.editOptions&&(h.editOptions=e.extend(!0,h.editOptions||{},k.editOptions));void 0!==k.delOptions&&(h.delOptions=
k.delOptions);c.hasClass("jqgrid-new-row")&&(h.extraparam[k.prmNames.oper]=k.prmNames.addoper);n={keys:h.keys,oneditfunc:h.onEdit,successfunc:h.onSuccess,url:h.url,extraparam:h.extraparam,aftersavefunc:h.afterSave,errorfunc:h.onError,afterrestorefunc:h.afterRestore,restoreAfterError:h.restoreAfterError,mtype:h.mtype};!k.multiselect&&g!==k.selrow||k.multiselect&&0>e.inArray(g,k.selarrrow)?f.jqGrid("setSelection",g,!0,a):p.fullBoolFeedback.call(d,"onSelectRow","jqGridSelectRow",g,!0,a);switch(b){case "edit":f.jqGrid("editRow",
g,n);break;case "save":f.jqGrid("saveRow",g,n);break;case "cancel":f.jqGrid("restoreRow",g,h.afterRestore);break;case "del":h.delOptions=h.delOptions||{};void 0===h.delOptions.top&&(h.delOptions.top=m());f.jqGrid("delGridRow",g,h.delOptions);break;case "formedit":h.editOptions=h.editOptions||{};void 0===h.editOptions.top&&(h.editOptions.top=m(),h.editOptions.recreateForm=!0);f.jqGrid("editGridRow",g,h.editOptions);break;default:if(null!=h.custom&&0<h.custom.length)for(m=h.custom.length,k=0;k<m;k++)n=
h.custom[k],n.action===b&&e.isFunction(n.onClick)&&n.onClick.call(d,{rowid:g,event:a,action:b,options:n})}a.stopPropagation&&a.stopPropagation();return!1};l.actions=function(a,b,c,g){a=b.rowId;var d="",f=this.p,k=e(this),m={},n=v.call(k,"edit")||{},f=e.extend({editbutton:!0,delbutton:!0,editformbutton:!1,commonIconClass:"ui-icon",editicon:"ui-icon-pencil",delicon:"ui-icon-trash",saveicon:"ui-icon-disk",cancelicon:"ui-icon-cancel",savetitle:n.bSubmit||"",canceltitle:n.bCancel||""},v.call(k,"nav")||
{},p.nav||{},f.navOptions||{},v.call(k,"actionsNav")||{},p.actionsNav||{},f.actionsNavOptions||{},b.colModel.formatoptions||{}),n=k.jqGrid("getGuiStyles","states.hover"),n="onmouseover=\"jQuery(this).addClass('"+n+"');\" onmouseout=\"jQuery(this).removeClass('"+n+"');\"",h=[{action:"edit",actionName:"formedit",display:f.editformbutton},{action:"edit",display:!f.editformbutton&&f.editbutton},{action:"del",idPrefix:"Delete",display:f.delbutton},{action:"save",display:f.editformbutton||f.editbutton,
hidden:!0},{action:"cancel",display:f.editformbutton||f.editbutton,hidden:!0}],l=null!=f.custom?f.custom.length-1:-1;if(void 0===a||q.isEmpty(a))return"";if(e.isFunction(f.isDisplayButtons))try{m=f.isDisplayButtons.call(this,b,c,g)||{}}catch(t){}for(;0<=l;)b=f.custom[l--],h["first"===b.position?"unshift":"push"](b);b=0;for(l=h.length;b<l;b++)if(c=e.extend({},h[b],m[h[b].action]||{}),!1!==c.display){g=c.action;var u=c.actionName||g,w=void 0!==c.idPrefix?c.idPrefix:g.charAt(0).toUpperCase()+g.substring(1);
c="<div title='"+r(f[g+"title"])+(c.hidden?"' style='display:none;":"")+"' class='"+r(k.jqGrid("getGuiStyles","actionsButton","ui-pg-div ui-inline-"+g))+"' "+(null!==w?"id='j"+r(w+"Button_"+a):"")+"' onclick=\"return jQuery.fn.fmatter.rowactions.call(this,event,'"+u+"');\" "+(c.noHovering?"":n)+"><span class='"+r(p.mergeCssClasses(f.commonIconClass,f[g+"icon"]))+"'></span></div>";d+=c}return"<div class='"+r(k.jqGrid("getGuiStyles","actionsDiv","ui-jqgrid-actions"))+"'>"+d+"</div>"};l.actions.pageFinalization=
function(a){var b=e(this),c=this.p,g=c.colModel,d=g[a],f=function(f,h){var k=0,l,m;l=g.length;for(m=0;m<l&&!0===g[m].frozen;m++)k=m;l=b.jqGrid("getGridRowById",h);null!=l&&null!=l.cells&&(a=c.iColByName[d.name],m=e(l.cells[a]).children(".ui-jqgrid-actions"),d.frozen&&c.frozenColumns&&a<=k&&(m=m.add(e(b[0].grid.fbRows[l.rowIndex].cells[a]).children(".ui-jqgrid-actions"))),f?(m.find(">.ui-inline-edit,>.ui-inline-del").show(),m.find(">.ui-inline-save,>.ui-inline-cancel").hide()):(m.find(">.ui-inline-edit,>.ui-inline-del").hide(),
m.find(">.ui-inline-save,>.ui-inline-cancel").show()))},k=function(a,b){f(!0,b);return!1},l=function(a,b){f(!1,b);return!1};null!=d.formatoptions&&d.formatoptions.editformbutton||(b.unbind("jqGridInlineAfterRestoreRow.jqGridFormatter jqGridInlineAfterSaveRow.jqGridFormatter",k),b.bind("jqGridInlineAfterRestoreRow.jqGridFormatter jqGridInlineAfterSaveRow.jqGridFormatter",k),b.unbind("jqGridInlineEditRow.jqGridFormatter",l),b.bind("jqGridInlineEditRow.jqGridFormatter",l))};e.unformat=function(a,b,c,
g){var d,f=b.colModel,k=f.formatter,m=this.p,n=f.formatoptions||{},h=f.unformat||l[k]&&l[k].unformat;a instanceof jQuery&&0<a.length&&(a=a[0]);m.treeGrid&&null!=a&&e(a.firstChild).hasClass("tree-wrap")&&(e(a.lastChild).hasClass("cell-wrapper")||e(a.lastChild).hasClass("cell-wrapperleaf"))&&(a=a.lastChild);f.autoResizable&&null!=a&&e(a.firstChild).hasClass(m.autoResizing.wrapperClassName)&&(a=a.firstChild);if(void 0!==h&&e.isFunction(h))d=h.call(this,e(a).text(),b,a);else if(void 0!==k&&"string"===
typeof k){var q=e(this),u=function(a,b){return void 0!==n[b]?n[b]:v.call(q,"formatter."+a+"."+b)},f=function(a,b){var c=u(a,"thousandsSeparator").replace(/([\.\*\_\'\(\)\{\}\+\?\\])/g,"\\$1");return b.replace(new RegExp(c,"g"),"")};switch(k){case "integer":d=f("integer",e(a).text());break;case "number":d=f("number",e(a).text()).replace(u("number","decimalSeparator"),".");break;case "currency":d=e(a).text();b=u("currency","prefix");c=u("currency","suffix");b&&b.length&&(d=d.substr(b.length));c&&c.length&&
(d=d.substr(0,d.length-c.length));d=f("number",d).replace(u("number","decimalSeparator"),".");break;case "checkbox":d=l.checkbox.unformat(a,b,a);break;case "select":d=e.unformat.select(a,b,c,g);break;case "actions":return"";default:d=e(a).text()}}return d=void 0!==d?d:!0===g?e(a).text():p.htmlDecode(e(a).html())};e.unformat.select=function(a,b,c,g){c=[];a=e(a).text();b=b.colModel;if(!0===g)return a;b=e.extend({},b.editoptions||{},b.formatoptions||{});g=void 0===b.separator?":":b.separator;var d=void 0===
b.delimiter?";":b.delimiter;if(b.value){var f=b.value;b=!0===b.multiple?!0:!1;var k=[],l=function(a,b){if(0<b)return a};b&&(k=a.split(","),k=e.map(k,function(a){return e.trim(a)}));if("string"===typeof f){var n=f.split(d),h=0,p;for(p=0;p<n.length;p++)if(d=n[p].split(g),2<d.length&&(d[1]=e.map(d,l).join(g)),b)-1<e.inArray(e.trim(d[1]),k)&&(c[h]=d[0],h++);else if(e.trim(d[1])===e.trim(a)){c[0]=d[0];break}}else if(q.isObject(f)||e.isArray(f))b||(k[0]=a),c=e.map(k,function(a){var b;e.each(f,function(c,
d){if(d===a)return b=c,!1});if(void 0!==b)return b});return c.join(", ")}return a||""};e.unformat.date=function(a,b){var c=e.extend(!0,{},v.call(e(this),"formatter.date"),p.formatter.date||{},b.formatoptions||{});return q.isEmpty(a)?"":p.parseDate.call(this,c.newformat,a,c.srcformat,c)}});
//# sourceMappingURL=jquery.fmatter.map
