(function() {
	function add0(m){return m<10?'0'+m:m }
  function format(timeStamp)
 {

var timeStamp = parseInt(timeStamp);
var time = new Date(timeStamp);
var y = time.getFullYear();
var m = time.getMonth()+1;
var d = time.getDate();
var h = time.getHours();
var mm = time.getMinutes();
var s = time.getSeconds();
return y+'-'+add0(m)+'-'+add0(d)+' '+add0(h)+':'+add0(mm)+':'+add0(s);
}


	app.controller('JobsCtrl', JobsCtrl);

	JobsCtrl.$inject = ['$compile','$window','$scope','$http','DTOptionsBuilder', 'DTColumnBuilder', '$resource', 'DTInstances','$modal','$translate'];

	function JobsCtrl($compile,$window,$scope, $http, DTOptionsBuilder, DTColumnBuilder, $resource, DTInstances,$modal,$translate) {
		var vm = this;
		vm.selected = {};
		vm.selectAll = false;
		vm.toggleAll = toggleAll;
		vm.toggleOne = toggleOne;
		$scope.ctrlBtnDisabled = true;
		$scope.errorMessage = null;
		$scope.successMessage =null;


		vm.dtOptions = DTOptionsBuilder.fromFnPromise(function () {
        var promise = $http({url: "/ctcloud/job/control/jobList", params: {order:'jobId'},method: "GET"}).then(function (response) {
        	for(var i=0;i<response.data.length;i++){
        		response.data[i].submitTime = format(response.data[i].submitTime);
        		response.data[i].startTime = format(response.data[i].startTime);
        		response.data[i].endTime = format(response.data[i].endTime);
        	}
          return response.data;
        }, function (response) {
          return [];
        });
        return promise;
      }).withOption('createdRow', function (row, data, dataIndex) {
        $compile(angular.element(row).contents())($scope);
      }).withOption('headerCallback', function (header) {
        $compile(angular.element(header).contents())($scope);
      }).withPaginationType('full_numbers')
      .withOption('drawCallback', function(nRow) {
        $compile(angular.element('a.paginate_button.first').empty().append('<span class="" translate="content.pages.FIRST"></span>'))($scope);
        $compile(angular.element('a.paginate_button.previous').empty().append('<span class="" translate="content.pages.PREVIOUS"></span>'))($scope);
        $compile(angular.element('a.paginate_button.next').empty().append('<span class="" translate="content.pages.NEXT"></span>'))($scope);
        $compile(angular.element('a.paginate_button.last').empty().append('<span class="" translate="content.pages.LAST"></span>'))($scope);

   });

    var checkboxAll = '<input type="checkbox" ng-model="showCase.selectAll" ng-click="showCase.toggleAll(showCase.selectAll, showCase.selected)"/>';
    vm.dtColumns = [
      DTColumnBuilder.newColumn(null).withTitle(checkboxAll).notSortable().renderWith(function (data, type, full, meta) {
        vm.selected[full.id] = false;
        return '<input id= "'+data.jobId+'" clusterName="'+data.clusterName+'" arrayIndex="'+data.arrayIndex+'" type="checkbox"  ng-model="showCase.selected[' + data.jobId + ']"' + 'ng-click="showCase.toggleOne(' + data.jobId + ', showCase.selected);$event.stopPropagation();"/>';
      }).withOption('width', '1%'),
      DTColumnBuilder.newColumn('jobId').withTitle('<span class="" translate="content.job.jobList.ID"></span>').renderWith(function (data, type, full, meta) {
        var retValue = '<a ng-click="showJob(' + data+ ')"  class="aTag">' + data + '</a>';
        return retValue;
      }),
      DTColumnBuilder.newColumn('workLoadType').withTitle('<span class="" translate="content.job.jobList.TYPE"></span>'),
      DTColumnBuilder.newColumn('jobName').withTitle('<span class="" translate="content.job.jobList.NAME"></span>'),
      DTColumnBuilder.newColumn('jobStatus').withTitle('<span class="" translate="content.job.jobList.STATUS"></span>').notSortable().renderWith(function (data, type, full, meta) {
        var retValue = '<span title="Active" class="label bg-success"><span class="" translate="content.job.jobList.ACTIVE"></span></span>';
        if (data == 'USUSP') {
          retValue = '<span title="Suspended" class="label bg-warning"><span class="" translate="content.job.jobList.SUSPENDED"></span></span>'
        } else if (data.indexOf('DONE') != -1) {
          retValue = '<span title="Done" class="label bg-light"><span class="" translate="content.job.jobList.DONE"></span></span>'
        } else if (data == 'EXIT') {
          retValue = '<span title="Done" class="label" style="background-color: #dc0000;"><span class="" translate="content.job.jobList.EXIT"></span></span>'
        } else if (data == 'PSUSP') {
          retValue = '<span title="PSUSP" class="label bg-light"><span class="" translate="content.job.jobList.PSUSP"></span></span>'
        }
        return retValue
      }),
      DTColumnBuilder.newColumn('queue').withTitle('<span class="" translate="content.job.jobList.QUEUE"></span>'),
      DTColumnBuilder.newColumn('submitTime').withTitle('<span class="" translate="content.job.jobList.SUBMIT_TIME"></span>'),
      DTColumnBuilder.newColumn('startTime').withTitle('<span class="" translate="content.job.jobList.START_TIME"></span>'),
      DTColumnBuilder.newColumn('endTime').withTitle('<span class="" translate="content.job.jobList.END_TIME"></span>'),
      DTColumnBuilder.newColumn('userName').withTitle('<span class="" translate="content.job.jobList.USER"></span>')
    ];


		//get instance
		DTInstances.getLast().then(function(dtInstance) {
			vm.dtInstance = dtInstance;
		});

		$scope.createJob = function (){
			$http({url: "/ctcloud/job/control/queues", method: "GET"}).then(function(response){
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
			var clusterName = angular.element('#'+jobId).attr('clusterName');
			var arrayIndex = angular.element('#'+jobId).attr('arrayIndex');
			var jobParam = {
				jobId : jobId,
				clusterName : clusterName,
				arrayIndex : arrayIndex
			}
			$('#jobSummary').addClass('active');
			$scope.$broadcast("jobdetail.show", jobParam);
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
	app.controller('CreateJobCtrl', ['$scope','$http','$window','$q','$modalInstance','queues','fileService','FileUploader','filesUploadService',
		function($scope, $http,$window,$q, $modalInstance,queues,fileService,FileUploader,filesUploadService) {
			$scope.job ={};
			$scope.upload2files = [];
			$scope.queues = queues;
			$scope.job.userName = sessionStorage.ctUsername;




			var retMessage = {message :""};

			var defaulePath = "/home/" +  $scope.job.user;
			$scope.openRemotefile = function() {
				var args = {
					path: defaulePath,
					myhome: defaulePath,
					fileTypes: [{"key":"Text", "value":"txt"},{"key":"ZIP", "value":"zip"}],
					onlyFolder: false
				};
				fileService.open(args).then(
					function(result) {
					},
					function(error) {
					}
				);
			};

		    $scope.removeFile = function(item) {
		    	angular.forEach($scope.upload2files, function(fm){
		    		if (item.name  == fm.name) {
		    			$scope.upload2files.splice(fm,1);
		    		}
		    	});
		    };

		    $scope.$on('remote.file.selected',function(event,selectedFiles){
		    	angular.forEach(selectedFiles, function(fs){
		    		$scope.upload2files.push(fs);
		    	});
				console.log('remote.file.selected event')
			});

		    $scope.getFileList = function(e) {
		    	$scope.$apply(function () {
		        	for (var i = 0; i < e.files.length; i++) {
		                $scope.upload2files.push(e.files[i]);
		            }

		        });
		    }

			var startHandleUploadfiles = function() {
				var fileArray = [];
				angular.forEach($scope.upload2files, function(item){
					fileArray.push(_getJsonFile(item));
					if (item.host != 'server') {
						filesUploadService.toUploadFiles($scope,[item],"");
					}
				});
				$scope.jsonFile = {};
				jsonFile.fileItems = fileArray;
			};

			var _getJsonFile = function(item) {
				var jFile ={};
				jFile.host = item.host;
				jFile.name = item.name;
				jFile.size = item.size;
				jFile.absolutePath  = item.absolutePath;
				return jFile;
			};

			$scope.ok = function () {
        var reqUrl = "/ctcloud/job/control/submit";
        var objectToSerialize = JSON.stringify(this.job);
        var promise = $http.post(reqUrl, objectToSerialize, {
          headers: {
            'Content-Type': 'application/json'
          }
        }).then(function (response) {
          $modalInstance.close(response.data);
        }, function (response) {
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
			$scope.$on('jobdetail.show',function(event,jobParam){
				$scope.jobid = jobParam.jobId;
				$http.get('/ctcloud/job/control/detail' ,{ params: {
            jobId: jobParam.jobId,
            arrayIndex : jobParam.arrayIndex,
            clusterName: jobParam.clusterName
          }
					}).then(function(response) {
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
				var reqUrl =  "/ctcloud/job/control/" + jobAction;
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

