(function () {
	'use strict';
	angular.module('app.largeFileupload', []);
})();

(function(module) {
	try {
		module = angular.module('app.largeFileupload');
	} catch (e) {
		module = angular.module('app.largeFileupload', []);
	}
	module
		.factory("filesUploadService", ['$rootScope','dialogService', function($rootScope,dialogService) {
			
			var factory = {};
			
			var isShowProgress = false;			
			var progressArray = [];
			// Store total file size of all files
			var allFileTotalSize = 0;
			// Store total uploaded size
			var totalUploadedSize = 0;
			// Store all AJAX requests
			var allRequests = [];
			// Set cancel flag
			var cancel = false;
			
			factory.toUploadFiles = function(scope,files, filePath) {
				var hostName = "ct Server";
				var destPath = filePath;
				var filelist = files;
				
				var handlefilelist = filterFile(filelist);
				if (!isShowProgress) {
					showProgressBar();
				}
				for(var i = 0; i < handlefilelist.length; i++) {
					optimizeFileSize2Upload(scope,handlefilelist[i], handlefilelist.length, i);
				}
				return false;
			}
			
			/**
			 * filter validate file
			 * 
			 */
			var filterFile = function(filelist) {
				var filterfiles = [];
				if (filelist != null && filelist.length > 0) {
					for(var i = 0; i < filelist.length; i++) {
						try {
							// File has been deleted in Firefox
							if (typeof(filelist[i].lastModifiedDate) == 'undefined') {
								continue;
							}
							// File has been deleted in Chrome
							if (filelist[i].size == 0 && filelist[i].type == '') {
								continue;
							}
						} catch(e) {
							continue;
						}
						filterfiles[filterfiles.length] = filelist[i];
						progressArray[progressArray.length] = {
								"id": filterfiles[i].name,
								"totalSize": filterfiles[i].size,
								"uploadedSize": 0,
								"value": 0
							};
					}
				}
				return filterfiles
			}
			
			/**
			 * Split file into 5, 10, 20, 40, 80, 160, 256MB chunks and upload them
			 * 
			 */
			var optimizeFileSize2Upload = function(scope,file, totalFileNumber, currentFileIndex) {
				// const CHUNK_SIZE = 1024 * 1024 * 256;
				var SIZE = file.size;
				var CHUNK_SIZE = getChunkSize(SIZE);
				var num_chunk = Math.floor(SIZE / CHUNK_SIZE);
				if ((SIZE % CHUNK_SIZE) != 0) {
					num_chunk += 1;
				}
				var filename = file.name;
				// fileProcessor[filename].total = num_chunk;
				var start = 0;
				var end = CHUNK_SIZE;
				var chunkIndex = 0;
				
				if (0 == SIZE) SIZE = 1;
				
				while (start < SIZE) {
					var chunk;
					if ('mozSlice' in file) {
						chunk = file.mozSlice(start, end);
					} else {
						chunk = file.slice(start, end);
					}
					uploadchunkFile(chunk, chunkIndex, num_chunk, filename, SIZE, totalFileNumber, currentFileIndex,scope);
					start = end;
					end = start + CHUNK_SIZE;
					chunkIndex++;
				}
				
				return false;
			};
			
			/**
			 * By file size, decide the chunk size
			 */
			var getChunkSize = function(fileSize) {
				var MB_SIZE = 1024 * 1024;
				// Default chunk size is 5M
				var chunkSize = MB_SIZE * 5;
				
				if (fileSize > MB_SIZE * 1600) {
					// 256 M
					chunkSize = MB_SIZE * 256;
				} else if (fileSize > MB_SIZE * 800) {
					// 160 M
					chunkSize = MB_SIZE * 160;
				} else if (fileSize > MB_SIZE * 400) {
					// 80 M
					chunkSize = MB_SIZE * 80;
				} else if (fileSize > MB_SIZE * 200) {
					// 40 M
					chunkSize = MB_SIZE * 40;
				} else if (fileSize > MB_SIZE * 100) {
					// 20 M
					chunkSize = MB_SIZE * 20;
				} else if (fileSize > MB_SIZE * 50) {
					// file size > 50M, chunk size is 10M
					chunkSize = MB_SIZE * 10;
				}
				return chunkSize;
			};
			
			/**
			 * Upload one file or chunk of one file
			 */
			var uploadchunkFile = function(file2upload, chunkFileIndex, chunkFileLength, filename, currentFileTotalSize, totalFileNumber, currentFileIndex,scope) {
				var fsize = file2upload.size;
				var xhr = new XMLHttpRequest();
				xhr.open('POST', "/api/uploadfiles?user=ylyang", true);
				xhr.onerror = function() {
					// Show error message
					console.log("failed to connect server");
					hiddenProgressBar();
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
						try {
							// Calculating progress
							calcProgressValue(file2upload.size, filename, xhr.responseText, chunkFileLength);
						} catch (e) {
							hiddenProgressBar();
							// show message
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
					formData.append('hostName', 'ct server');
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
								hiddenProgressBar();
							} else if (status == 'UPLOADING') {
								uploadedSize = fileProgress.uploadedSize + uploadingSize;
								progressValue = Math.floor(( uploadedSize/ fileProgress.totalSize) * 100);
								if (progressValue >= 100) {
									progressValue = 99;
									if(!!!cancel) {
										// Send request to merge files in server
										handleChunkfile2Merge(filename, chunkFileLength);
									}
								}
								fileProgress.value = progressValue;
								fileProgress.uploadedSize = uploadedSize;
							} else {
								// Has some error message
								hiddenProgressBar();
								showServiceMessage(status);
								//hard code to fix this issue ssssssssssssssss
								return false;
							}
							break;
						}
					}
				}
				
				return false;
			};
			
			var showProgressBar = function() {
				$rootScope.$broadcast("upload.show.loading", totalUploadedSize);
			};
			
			var hiddenProgressBar = function() {
				$rootScope.$broadcast("upload.hidden.loading", totalUploadedSize);
			};
			
			
			var showServiceMessage = function(message) {
				$rootScope.$broadcast("upload.service.message", message);
			};
			
			/**
			 * Check whether or not all files are uploaded
			 */
			var checkProgress = function() {
				var completed = true;
				if (progressArray != null) {
					for (var i = 0; i < progressArray.length; i++) {
						var fileProgress = progressArray[i];
						// If value is not equal 100, don't complete uploading.
						if (fileProgress.value != 100) {
							completed = false;
							break;
						}
					}
				}
				if(!!isShowProgress) {
					// For progress bar
					var progressValue = Math.floor(( totalUploadedSize/ allFileTotalSize) * 100);
					if (progressValue >= 100 && !!!completed) {
						progressValue = 99;
					}
					
					_this.safeApply(scope, function(){
						$rootScope.fileUploadProgress = progressValue;
					})
					
				}
				
				return completed;
			};
			
			var handleChunkfile2Merge = function(filename, chunkFileLength) {
				var xhr = new XMLHttpRequest();
				xhr.open('POST', "/api/mergeChunkfiles?user=ylyang", true);
				xhr.onreadystatechange = function(e) {
					if (this.readyState == 4 && this.status == 200) {
						try {
							calcProgressValue(0, filename, xhr.responseText, chunkFileLength);
						} catch (e) {
							console.log(e);
						}
					}
				};
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
			
			
			return factory;
		}
		]);
})();

