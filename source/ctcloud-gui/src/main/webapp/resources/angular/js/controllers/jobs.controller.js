(function() {

	
	app.controller('JobsCtrl', JobsCtrl);

	JobsCtrl.$inject = ['$compile','$window','$scope','$http','DTOptionsBuilder', 'DTColumnBuilder', '$resource', 'DTInstances','$modal'];

	function JobsCtrl($compile,$window,$scope, $http, DTOptionsBuilder, DTColumnBuilder, $resource, DTInstances,$modal) {
		var vm = this;
		vm.selected = {};
		vm.selectAll = false;
		vm.toggleAll = toggleAll;
		vm.toggleOne = toggleOne;
		$scope.ctrlBtnDisabled = true;
		$scope.errorMessage = null;
		$scope.successMessage =null;

		
		vm.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
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
		.withPaginationType('full_numbers');

		var checkboxAll = '<input type="checkbox" ng-model="showCase.selectAll" ng-click="showCase.toggleAll(showCase.selectAll, showCase.selected)"/>';
		vm.dtColumns = [
			DTColumnBuilder.newColumn(null).withTitle(checkboxAll).notSortable().renderWith(function(data, type, full, meta) {
				vm.selected[full.id] = false;
				return '<input type="checkbox" ng-model="showCase.selected[' + data.jobId + ']"' + 'ng-click="showCase.toggleOne(' + data.jobId + ', showCase.selected);$event.stopPropagation();"/>';
			}).withOption('width', '1%'),
			DTColumnBuilder.newColumn('jobId').withTitle('ID').renderWith(function(data, type, full, meta) {
				var retValue ='<a ng-click="showJob(' + data + ')"  class="aTag">' + data + '</a>';
				return retValue;
			}),
			DTColumnBuilder.newColumn('jobType').withTitle('Type'),
			DTColumnBuilder.newColumn('jobName').withTitle('Name'),
			DTColumnBuilder.newColumn('status').withTitle('State').notSortable().renderWith(function(data, type, full, meta) {
				var retValue = '<span title="Active" class="label bg-success">Active</span>';
				if (data == 'USUSP') {
					retValue ='<span title="Suspended" class="label bg-warning">Suspended</span>'
				} else if (data.indexOf('DONE') != -1) {
					retValue ='<span title="Done" class="label bg-light">Done</span>'
				} else if (data == 'EXIT') {
					retValue ='<span title="Done" class="label" style="background-color: #dc0000;">Exit</span>'
				} else if (data == 'PSUSP') {
					retValue ='<span title="PSUSP" class="label bg-light">Psusp</span>'
				}
				return retValue
						}),
			DTColumnBuilder.newColumn('submitToQueue').withTitle('Queue'),
			DTColumnBuilder.newColumn('submitTimeStr').withTitle('Submit Time'),
			DTColumnBuilder.newColumn('startTimeStr').withTitle('Start Time'),
			DTColumnBuilder.newColumn('endTimeStr').withTitle('End Time'),
			DTColumnBuilder.newColumn('user').withTitle('User')
		];

		//get instance
		DTInstances.getLast().then(function(dtInstance) {
			vm.dtInstance = dtInstance;
		});
		
		$scope.createJob = function (){
			$http({url: "/api/queues", method: "GET"}).then(function(response){
				$scope.queues = response.data;
				var modalInstance = $modal.open({
					templateUrl: 'tpl/jobs/createJob.html',
					controller: 'CreateJobCtrl',
					resolve: {
						queues: function() {
							return $scope.queues;
						},
						userName: function() {
							return $scope.ctUserName;
						}
					}

				});
				modalInstance.opened.then(function() {
					console.log('modal is opened');
				});
				modalInstance.result.then(function(data) {
					vm.selectAll = false;
					$scope.errorMessage = null;
					$scope.successMessage =null;
					vm.dtInstance.rerender();
					if (data.failure.length >0) {
						$scope.errorMessage = data.failure[0];
					}
					if (data.success.length >0) {
						$scope.successMessage = data.success[0];
					}
					
					console.log("operate success at: " + new Date());
				}, function() {
					console.log('modal dismissed at: ' + new Date());
				});
			},function(response){
				$scope.queues = [];
				var modalInstance = $modal.open({
					templateUrl: 'tpl/jobs/createJob.html',
					controller: 'CreateJobCtrl',
					resolve: {
						queues: function() {
							return $scope.queues;
						},
						userName: function() {
							return $scope.ctUserName;
						}
					}

				});
				modalInstance.opened.then(function() {
					console.log('modal is opened');
				});
				modalInstance.result.then(function(data) {
					vm.selectAll = false;
					$scope.errorMessage = null;
					$scope.successMessage =null;
					vm.dtInstance.rerender();
					if (data.failure.length >0) {
						$scope.errorMessage = data.failure[0];
					}
					if (data.success.length >0) {
						$scope.successMessage = data.success[0];
					}
					
					console.log("operate success at: " + new Date());
				}, function() {
					console.log('modal dismissed at: ' + new Date());
				});
			});
			
			
		};
		
		$scope.templateUrl = 'tpl/jobs/jobInfo.html';
	
		$scope.showJob = function (jobId) {
			$('#jobSummary').addClass('active');
			$scope.$broadcast("jobdetail.show", jobId);
		};
		
		
		function toggleOne (rid, selectedItems) {
			var me = this;
			var counter = 0;
			for (var id in selectedItems) {
				if (selectedItems.hasOwnProperty(id)) {
					if (selectedItems[id]) {
						counter++;
					}
				}
			}

			$('#row_counter').text((counter? counter:"None") + " selected");

			for (var id in selectedItems) {
				if (selectedItems.hasOwnProperty(id)) {
					if (selectedItems[id]) {
						$scope.ctrlBtnDisabled = false;
						break;
					} else {
						$scope.ctrlBtnDisabled = true;
					}
				}
			}

			for (var id in selectedItems) {
				if (selectedItems.hasOwnProperty(id)) {
					if(!selectedItems[id]) {
						me.selectAll = false;
						return;
					}
				}
			}
			me.selectAll = true;
		};
		
		function toggleAll (selectAll, selectedItems) {
			var myDataTable = vm.dtInstance.dataTable;
			var rows = $("#demo01 tbody tr");

			if (selectAll) {
				//$('table.dataTable tbody tr').addClass('selected');
				for (var i = 0; i < rows.length; i++) {
					var rowData = myDataTable.fnGetData(rows[i]);
					if (null != rowData)
						selectedItems[rowData.jobId] = true;
				}
			} else {
				//$('table.dataTable tbody tr').removeClass('selected');
				for (var i = 0; i < rows.length; i++) {
					var rowData = myDataTable.fnGetData(rows[i]);
					if (null != rowData)
						selectedItems[rowData.jobId] = false;
				}
			}

			for (var id in selectedItems) {
				if (selectedItems.hasOwnProperty(id)) {
					if (selectedItems[id]) {
						$scope.ctrlBtnDisabled = false;
						break;
					} else {
						$scope.ctrlBtnDisabled = true;
					}
				}
			}

			var counter = 0;
			for (var id in selectedItems) {
				if (selectedItems.hasOwnProperty(id)) {
					if (selectedItems[id]) {
						counter++;
					}
				}
			}
			$('#row_counter').text((counter? counter:"None") + " selected");

		};
		
		$scope.doControlJob = function (jobAction){
			var ids = [];
			if (!!jobAction) {
				for (var item in vm.selected) {
					if (typeof(item) != 'undefined' && item != 'undefined' && !!item) {
						ids.push(item);
					}
				}
				if (ids.length >0) {
					var modalInstance = $modal.open({
						templateUrl: 'tpl/jobs/jobDialog.html',
						controller: 'DialogCtrl',
						size: 'sm',
						resolve: {
							jobAction: function() {
								return jobAction;
							},
							ids: function() {
								return ids;
							}
						}
					});
					
					modalInstance.result.then(function(data) {
						vm.selectAll = false;
						vm.dtInstance.rerender();
						if (data.failure.length >0) {
							$scope.errorMessage = data.failure[0];
						}
						if (data.success.length >0) {
							$scope.successMessage = data.success[0];
						}
					}, function() {
						console.log('modal dismissed at: ' + new Date());
					});
				}
			}
		}
	}


	//  create job ctrl
	app.controller('CreateJobCtrl', ['$scope','$http','$window','$q','$modalInstance','queues',
		function($scope, $http,$window,$q, $modalInstance,queues) {
			$scope.job ={};
			$scope.queues = queues;
			$scope.job.user = sessionStorage.ctUsername;
			var retMessage = {message :""};
			$scope.ok = function() {
				var reqUrl =  "/api/jobs/submit";
				var objectToSerialize = JSON.stringify(this.job);
				var promise = $http.post(reqUrl, objectToSerialize, {headers: {'Content-Type': 'application/json'}}).then(function(response){
					$modalInstance.close(response.data);
				},function(response){
					$modalInstance.close(response.data);
				});
				
			};

			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		}])
		
	//  summary job ctrl
	app.controller('JobDetailsCtrl', ['$scope','$http','$resource','$modal',
		function JobDetailsCtrl($scope, $http,$resource, $modal) {
			console.log('xxxxxxxxxxxxxxx');
			$scope.$on('jobdetail.show',function(event,jobid){
				$scope.jobid = jobid;
				$http.get('/api/jobs/' + jobid).then(function(response) {
					$scope.jobinfo = response.data;
				}, function(response) {
		            console.error(response);
		            if(!response.data){
		            	$scope.onAlert("The request failed.");
		            }else if(typeof(response.data) == 'string'){
		                $scope.onAlert("The request failed: " + response.data);
		            }else {
		                $scope.onAlert("The request failed: " + JSON.stringify(response, null, 2));
		            }
		        });
			});
			
			$scope.closeJobdetails = function () {
				$('#jobSummary').removeClass('active');
			}
	}]);
	
	angular.module('app').filter('formateValue', function () {
	    return function (v) {
	    	if(!v || v == "undefined"){
	    		return "-";
	    	}
	    	return v;
	    };
	})
	
	app.controller('DialogCtrl', ['$scope','$window','$http', '$q', '$modalInstance', 'jobAction', 'ids', 
		function($scope,$window, $http, $q, $modalInstance, jobAction, ids){
			var JobIds = ids.toString();
			$scope.action =jobAction;
			$scope.ok = function() {
				var reqUrl =  "/api/jobs/control/" + jobAction;
				var data = 'jobIds=' + JobIds + '&jobAction=' + jobAction + '&user=' + sessionStorage.ctUsername;
				var promise = $http.post(reqUrl, data, {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(response){
					
					$modalInstance.close(response.data);
				},function(response){
					$modalInstance.close(response.data);
				});
			};
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
	}]);
   
})();
 