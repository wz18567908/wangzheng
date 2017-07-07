(function () {
	'use strict';
	angular.module('app.fileBrowser', []);
})();



(function(module) {
try {
	module = angular.module('app.fileBrowser');
} catch (e) {
	module = angular.module('app.fileBrowser', ['datatables']);
}
module
	.controller("fileBrowserCtrl", ['$rootScope', '$scope','$compile','$resource', 'DTOptionsBuilder', 'DTColumnBuilder','DTInstances','$translate', '$q', '$http', 'fileService',
			function($rootScope, $scope,$compile,$resource,DTOptionsBuilder, DTColumnBuilder,DTInstances, $translate, $q, $http, fileService) {
			var vm = this;
			vm.selected = {};
			$scope.selectedItem=[];
			vm.toggleOne = toggleOne;
			$scope.path = fileService.filePath;
			$scope.myhome = fileService.myhome;
			$scope.fileTypes = fileService.fileTypes;
			$scope.selectedType = "";

			$scope.message = "";
			if (!$scope.path) {
				$scope.path = "/";
			}
			
			$scope.reqUrl = 'api/filelist.json';
			
/*			vm.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
				var promise = $http({url: "/api/jobs", method: "GET"}).then(function(response){
					return response.data.records;
				},function(response){
					return [];
				});
				return promise;
			}).withOption('createdRow', function(row, data, dataIndex) {
				$compile(angular.element(row).contents())($scope);
			}).withOption('headerCallback', function(header) {
				$compile(angular.element(header).contents())($scope);
			})
			.withPaginationType('full_numbers');*/

			vm.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
				/*var promise = $http({url: "/api/jobs", method: "GET"}).then(function(response){
					return response.data.records;
				},function(response){
					return [];
				});
				return promise;*/
				 return $resource($scope.reqUrl).query().$promise;
			}).withOption('createdRow', function(row, data, dataIndex) {
				$compile(angular.element(row).contents())($scope);
			}).withOption('headerCallback', function(header) {
				$compile(angular.element(header).contents())($scope);
			}).withOption("paging", false)
			.withOption('order',[1, 'desc'])
			.withOption("bAutoWidth", false).withDOM("tr");
			
			
			vm.dtColumns = [
				DTColumnBuilder.newColumn(null).notSortable().renderWith(function(data, type, full, meta) {
					vm.selected[full.id] = false;
					return '<input type="checkbox" ng-model="fBCtrl.selected[' + data.id + ']"' + 'ng-click="fBCtrl.toggleOne(' + data.id  +',fBCtrl.selected);$event.stopPropagation();"/>';
				}).withOption('width', '1%'),
				DTColumnBuilder.newColumn('name').withTitle('Name').renderWith(function(data, type, full, meta){
					var retValue = '<span>' + data + '</span>';
					if (full.folder) {
						retValue = '<a href="javascript:void(0)"  class="filelink" ng-click="gotoFolder(\''+ full.absolutePath + '\')"><i class="fa fa-folder" style="width:20px; height:20px;"></i><span>' + data + '</span>  </a>';
					} 
					return retValue
				}),
				DTColumnBuilder.newColumn('size').withTitle('Size').renderWith(function(data, type, full, meta){
					if (isNaN(data)) {
						return "-";
					}
					return data + "KB";
				}),
				DTColumnBuilder.newColumn('type').withTitle('Type'),
				DTColumnBuilder.newColumn('absolutePath').withTitle('').withClass("hiddenColumn"),
				DTColumnBuilder.newColumn('modifyTime').withTitle('Modify Date').renderWith(function(data, type, full, meta){
					if (isNaN(data)) {
						return "-";
					}
					var date = new Date(data);
					return date.getFullYear() + "-" + timeHandle(date.getMonth()+1) + "-" + timeHandle(date.getDate()) + " " 
						+ timeHandle(date.getHours()) + ":" + timeHandle(date.getMinutes()) + ":" + timeHandle(date.getSeconds());
					
				})
			];
			
			//get instance
			DTInstances.getLast().then(function(dtInstance) {
				vm.dtInstance = dtInstance;
			});
			
			//var remoteFile = {'file':{'name':'xxx','size':'2222'},'host':'server','progress':'12'};
			function toggleOne (rid,selectedItems) {
				$scope.selectedItem=[];
				var myDataTable = vm.dtInstance.dataTable;
				var rows = $("#remoteBrowser tbody tr");
				var remotefile ={};
				for (var i = 0; i < rows.length; i++) {
					var rowData = myDataTable.fnGetData(rows[i]);
					if (!!rowData && selectedItems[rowData.id]) {
						remotefile.host = 'server';
						remotefile.name = rowData.name;
						remotefile.size = rowData.size;
						remotefile.path = rowData.absolutePath;
						$scope.selectedItem.push(remotefile);
					 }
				}
				return false;
			}
				
				
			function timeHandle(num) {
				return ("00" + num).substr(-2);
			};
			
			$scope.gotoFolder = function (fPath) {
				$scope.reqUrl = 'api/filelist2.json';
				$scope.path = 'sss';
				vm.dtInstance.rerender();
				console.log('xxxxxxxxxx');
				return false;
			};
			

			$scope.cancelClick = function() {
				fileService.cancel(null);
			};
			
			$scope.okClick = function() {
				$rootScope.$broadcast("remote.file.selected", $scope.selectedItem);
				fileService.close(null, null);
            };
            
		}
	]);
})();



(function(module) {
try {
	module = angular.module('app.fileBrowser');
} catch (e) {
	module = angular.module('app.fileBrowser', []);
}
module
	.service("fileService", ['$rootScope', '$q', '$compile', '$templateCache', '$http', '$location', '$translate', '$window',
		function($rootScope, $q, $compile, $templateCache, $http, $location, $translate, $window) {
			var self = this;
			self.dialogs = {};
			self.filePath = null;
			self.fileTypes = null;
			self.onlyFolder = false;
			self.user = null;
			self.myhome = null;

			var options = {
				autoOpen: false,
				modal: true,
				width: 800,
				minWidth:330,
				height: 562,
				minHeight: 260,
				resizable: false,
				close: function(event, ui) {
				}
			};
			var headers = {
				'Accept': 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
			};
			this.open = function(args) {
				var dialogOptions = {};
				if(!args.id){
					args.id = "fileBrowser";
					args.template = "fileBrowser.html";
				}
				angular.extend(dialogOptions, options);
				angular.extend(dialogOptions, args);
				if(args.path!=null && args.path!=""){
					self.filePath = args.path;
				}
				if(args.myhome!=null && args.myhome!=""){
					self.myhome = args.myhome;
				}

				if(args.fileTypes!=null && args.fileTypes!=""){
					self.fileTypes = args.fileTypes;
				}
				self.onlyFolder = args.onlyFolder;

				console.log("Open modal, id = " + args.id + ", path = " + self.filePath + ", onlyFolder = " + self.onlyFolder);
				// Initialize our dialog structure
				var dialog = {scope: null, ref: null, deferred: $q.defer()};
				var html = $templateCache.get(args.template);

				// Create a new scope, inherited from the parent.
				dialog.scope = $rootScope.$new();
				var dialogLinker = $compile(html);
				dialog.ref = $(dialogLinker(dialog.scope));

				var customCloseFn = dialogOptions.close;
				dialogOptions.close = function(event, ui) {
					if (customCloseFn) {
						customCloseFn(event, ui);
					}
					cleanup(args.id);
				};

				// Initialize the dialog and open it
				dialog.ref.dialog(dialogOptions);
				dialog.ref.dialog("open");
				self.dialogs[args.id] = dialog;
				return dialog.deferred.promise;
			};

			this.close = function(id, result) {
				if(!id){
					id = "fileBrowser";
				}
				var dialog = getExistingDialog(id);
				dialog.deferred.resolve(result);
				dialog.ref.dialog("close");
			};

			this.cancel = function(id) {
				if(!id){
					id = "fileBrowser";
				}
				var dialog = getExistingDialog(id);
				dialog.deferred.reject();
				dialog.ref.dialog("close");
			};

			function cleanup (id) {
				var dialog = getExistingDialog(id);
				dialog.deferred.reject();
				dialog.scope.$destroy();
				dialog.ref.remove();
				delete self.dialogs[id];
			};

			function getExistingDialog(id) {
				var dialog = self.dialogs[id];
				if (!angular.isDefined(dialog)) {
					throw "Does not have a reference to dialog id " + id;
				}
				return dialog;
			};

			this.endWith = function(str, endstr) {
				if(str==null || str=="" || str.length==0 || endstr.length>str.length){
					return false;
				}
				if(str.substring(str.length - endstr.length).toLowerCase()==endstr.toLowerCase()){
					return true;
				}
				return false;
			};
		}
	]);
})();


(function(module) {
	
try {
	module = angular.module('app.fileBrowser');
} catch (e) {
	module = angular.module('app.fileBrowser', []);
}
module.run(['$templateCache', '$translate', function($templateCache, $translate) {
	var i_title = 'browser....';
	var fileBrowser = `
	<div class="panel panel-default" ng-controller="fileBrowserCtrl as fBCtrl" >
	<div class="panel-heading font-bold">${i_title}</div>
	<div id="headArea" class="headArea">
		<div class="form-group">
				<label class="col-sm-2 control-label" style="text-align:right">Path:</label>
				<div class="col-sm-7">
					<input type="text"  required class="form-control" style="height: 28px;" id="input-id-1"  ng-model="path">
				</div>
			</div>
	</div>
	<div id="navArea" class="navArea">
	<ul style="padding:0;">
		<li class="favItem homedir">
			<a href="javascript:void(0)" ng-click="gotoFolder(myhome)">
				<i style="width:20px; height:20px;" class="fa fa-home"></i>My Home
			</a>
		</li>
	</ul>
	</div>
	<div id="dataArea" class="dataArea">
	  <div>
		<div id="infoArea" class="alert warning" style="display:none;padding:5px 10px;margin-bottom:0;">
		<svg style="float:left;" class="state-icon"><use xlink:href="#warning_16"/></svg><span>&nbsp;{{message}}</span>
		</div>

	  </div>
	  <div id="fileList" class="fileList">
	  <div class="table-responsive">
			<table id="remoteBrowser"  datatable="" dt-options="fBCtrl.dtOptions" dt-columns="fBCtrl.dtColumns" class="table table-striped  table-hover"></table>
		</div>
	  </div>
	</div>
	<div id="footArea" class="footArea">
		<!--div id="fileFilter" class="fileFilter">File of Types: 
		  <select ng-model="selectedType" aria-label="Type Select" ng-init="selectedType='all'" ng-change="doFilter(selectedType)">
		  <option value="all">All</option>
		  <option ng-repeat="item in fileTypes" value="{{item.value}}">{{item.key}}</option>
		</select>
		</div-->
		<div id="formButton" class="formButton">
		  <button class="btn btn-success" ng-click="okClick()">OK</button>
		  <button class="btn btn-default" ng-click="cancelClick()">Cancel</button>
		</div>
	</div>
	</div>
	`;
	$templateCache.put('fileBrowser.html', fileBrowser);
}]);
})();



