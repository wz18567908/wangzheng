// Define 'pac.submissionformdetail' module and its services
angular.module('pac.submissionformdetail').factory('ngSubmissionFormDetailService', ['$rootScope', '$timeout', '$window', '$http', '$translate', 'uiRemotefile', 'JobDataService', 'dialogService', '$q', function ($rootScope, $timeout, $window, $http, $translate, remotefile, JobDataService, dialogService, $q) {

	factory.uploadFiles = function(scope, files, filePath, applicationName) {
		var deferred = $q.defer();
		var _this = this;
    	var checkPermission = function(file, _function){
    		if(filePath){
        		JobDataService.checkPermission(encodeURIComponent(file), "PAC Server").then(function(response) {
        					_function();
        				}, function(response) {
        					//showErrorMsg(response);
        			});
    		}else{
    			_function();
    		}
    	};
    	var handleFileName = function (records) {
    		$(records).each(function(i, val){
    			if (typeof(val.name) != "string") {
    				var path = val.escapeAbsolutePath;
    				var idx = path.lastIndexOf("/");
    				if (idx > 0) {
    					var name = path.substr(idx + 1);
    					if (name) {
    						val.name = name;
    					}
    				}
    			}
    		});
    	}
    	
		var hostName = "PAC Server";
		var destPath = filePath;
		var fileArray = files;
		var appName = applicationName;
		
		// Store all uploading file information
		var progressArray = [];
		// Store total file size of all files
		var allFileTotalSize = 0;
		// Store total uploaded size
		var totalUploadedSize = 0;
		// Store all AJAX requests
		var allRequests = [];
		// Set cancel flag
		var cancel = false;
		
		var isShowProgress = !applicationName;
		
		var latestFileArray = [];
		
		$rootScope.fileUploadProgress = 0;
		
		$rootScope.$on("cancelUpload", function(event) {
			cancel = true;
		});
		

		

	    var uploadFile = function(file2upload, chunkFileIndex, chunkFileLength, filename, currentFileTotalSize, totalFileNumber, currentFileIndex) {
	        var fsize = file2upload.size;
	        var xhr = new XMLHttpRequest();
	        xhr.open('POST', "/platform/dataexplore/filebrowsing/upload-html5-file", true);
	        xhr.onerror = function() {
	        	// Show error message
	        	console.log($translate.instant("details.jobData.message.failConnServer"));
	        };
	        xhr.upload.onloadstart = function(event) {
	        	if (!!!isShowProgress) {
	        		scope.progressValue = 0;  // initial value;
	        		// Show uploading message for submission
	        	}
	        	console.log('starting to uploading...');
	        };
	        xhr.onreadystatechange = function(e) {
	        	if (this.readyState == 4 && this.status == 200) {
	            	// Set progress value
	        		try {
	        			// Calculating progress
	        			calcProgressValue(file2upload.size, filename, xhr.responseText, chunkFileLength);
	        		} catch (e) {
	        			console.log(e);
	        		}
	            }
	        };
	        // prepare FormData
	        var formData = new FormData();
	        formData.append('file', file2upload);
	        formData.append('chunkIndex', chunkFileIndex);
	        formData.append('chunkLength', chunkFileLength);
	        formData.append('fileName', filename);
	        if (typeof(hostName) != 'undefined' && hostName != null) {
	        	formData.append('hostName', hostName);
	        }
	        if (typeof(destPath) != 'undefined' && destPath != null) {
	        	formData.append('dest', destPath);
	        }
	        if (typeof(appName) != 'undefined' && appName != null) {
	        	formData.append('appName', appName);
	        }
	        xhr.send(formData);
			
			allRequests[allRequests.length] = xhr;
	    };
	    
	    /**
	     * Calculating progress values for every files
	     * ((uploadingSize + uploadedSize) / totalSize) * 100
	     */
	    var calcProgressValue = function(uploadingSize, filename, status, chunkFileLength) {
	    	var uploadedSize = null;
	    	var progressValue = null;
	    	totalUploadedSize += uploadingSize;
			if (progressArray != null) {
				for (var i = 0; i < progressArray.length; i++) {
					var fileProgress = progressArray[i];
					if (fileProgress.id == filename) {
						if (status == 'DONE') {
							fileProgress.value = 100;
						} else if (status == 'PROCESSING') {
							uploadedSize = fileProgress.uploadedSize + uploadingSize;
							progressValue = Math.floor(( uploadedSize/ fileProgress.totalSize) * 100);
							if (progressValue >= 100) {
								progressValue = 99;
								
								if(!!!cancel) {
									// Send request to merge files in server
									fileUploadMerge(filename, chunkFileLength);
								}
							}
							fileProgress.value = progressValue;
							fileProgress.uploadedSize = uploadedSize;
						} else {
							// Has some error message
//							upobj.closeDialog("uploadDialog");
//							upobj.displayErrorDialog(status);
							dialogService.cancel("fileUploadProgressDialog");
							clearInterval(timer);
							showMsg(MSG_ERROR, status);
							return false;
						}
						break;
					}
				}
			}
			
			return false;
	    };
	    
	    /**
	     * Large file chunk file merge
	     * Send merge request for one large file
	     */
	    var fileUploadMerge = function(filename, chunkFileLength) {
	    	var xhr = new XMLHttpRequest();
	        xhr.open('POST', "/platform/dataexplore/filebrowsing/upload-merge", true);
	        xhr.onreadystatechange = function(e) {
	        	if (this.readyState == 4 && this.status == 200) {
	        		try {
	        			// Calculating progress
	        			calcProgressValue(0, filename, xhr.responseText, chunkFileLength);
	        			
	        		} catch (e) {
	        			console.log(e);
	        		}
	            }
	        };
	        // prepare FormData
	        var formData = new FormData();
	        formData.append('chunkLength', chunkFileLength);
	        formData.append('fileName', filename);
	        if (typeof(hostName) != 'undefined' && hostName != null) {
	        	formData.append('hostName', hostName);
	        }
	        if (typeof(destPath) != 'undefined' && destPath != null) {
	        	formData.append('dest', destPath);
	        }
	        xhr.send(formData);
	    };
	    
	    /**
	     * Clean uploaded temporary files when
	     * 1. Click 'Cancel' button below progress bar
	     * 2. Leave current page when uploading
	     */
	    var fileUploadClean = function() {
	    	var filenames = "";
	    	if (progressArray != null && progressArray.length > 0) {
				for(var j = 0; j < progressArray.length; j++) {
					if (progressArray[j].value < 100) {
						filenames += progressArray[j].id;
						if (j == progressArray.length -1 ) {
							filenames += ",";
						}
					}
				}
				
				// Send asynchronous request to clean temporary files
				var xhr = new XMLHttpRequest();
				xhr.open('POST', "/platform/dataexplore/filebrowsing/upload-clean", true);
				var formData = new FormData();
				formData.append('fileName', filenames);
				xhr.send(formData);
	    	}
	    };
	    
	    /**
	     * Abort all AJAX requests which we have recorded.
	     */
	    var abortAllRequest = function() {
	    	if (typeof(allRequests) != 'undefined' && allRequests != null) {
	    		for (var i = 0; i < allRequests.length; i++) {
	    			var xhr = allRequests[i];
	    			if (xhr.readyState == 4 && xhr.status == 200) {
	    				// The request has done.
	    				continue;
	    			}
	    			// Abort the request
	    			xhr.abort();
	    		}
	    	}
	    };
		
	    var openFileUploadProgressBarDialog = function(model, index) {
			var template = "file-upload-progress-template-from-script.html";
			// jQuery UI dialog options
			var options = {
				title : $translate.instant("details.jobData.label.progressbar"),
				autoOpen: false,
				modal: true,
				width: 400,
				height: 200,
				resizable: false
			};
			
			// Open the dialog using template from script
			dialogService.open("fileUploadProgressDialog", template, model, options);
		};
		
		checkPermission(destPath, function(){
			if (fileArray != null && fileArray.length > 0) {
				for(var j = 0; j < fileArray.length; j++) {
					try {
						// File has been deleted in Firefox
						if (typeof(fileArray[j].lastModifiedDate) == 'undefined') {
							continue;
						}
						// File has been deleted in Chrome
						if (fileArray[j].size == 0 && fileArray[j].type == '') {
							continue;
						}
					} catch(e) {
						continue;
					}
					
					latestFileArray[latestFileArray.length] = fileArray[j];
					
					allFileTotalSize += fileArray[j].size;
					progressArray[progressArray.length] = {
						"id": fileArray[j].name,
						"totalSize": fileArray[j].size,
						"uploadedSize": 0,
						"value": 0
					};
				}
				
				if (!!isShowProgress) {
					// Display upload progress dialog
					openFileUploadProgressBarDialog();
				}
				
				// Upload files
				for(var i = 0; i < latestFileArray.length; i++) {
					handleFiles(latestFileArray[i], latestFileArray.length, i);
				}
			}
			
			// Define one interval function to check uploading progress
	        var timer = setInterval(function(){
	        	var completed = checkProgress();
	            if(completed){
	            	// Clear interval event
	                clearInterval(timer);
	                deferred.resolve();
//	                if (!!!upobj.isShowProgress && typeof(pacapplet) != 'undefined') {
//	    				// Upload done
//	                	if (upobj.latestFileArray.length > 0) {
//	                		pacapplet.writeProgressText(JOB_SUBMITTING, APPLET_UPLOAD_COMPLETE);
//	                	}
//	    			}
//	                
	                if (!!isShowProgress) {
	                	dialogService.cancel("fileUploadProgressDialog");
	                }
//	                
//	                if(typeof(callback) != 'undefined' && !!callback) {
//	                	// If have call back function, invoke it
//	                	callback();
//	                }
	            }
	            
	            // If cancel uploading
	            // 1. Clear interval check event
	            // 2. Refresh job data list
	            if (typeof(cancel) != 'undefined' && !!cancel) {
	            	// Clear interval event
	                clearInterval(timer);
	                deferred.resolve();
	             // Abort AJAX request
					abortAllRequest();
					
					// Clean temporary files in server side
					fileUploadClean();
	                
//	                if(typeof(callback) != 'undefined' && !!callback) {
//	                	// If have call back function, invoke it
//	                	callback();
//	                }
	            }
	        }, 1000);
		});
		return deferred.promise;
	};
	return factory;
}]);

