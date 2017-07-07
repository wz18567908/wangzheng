(function() {
	

app.controller('testCtrl', testCtrl);

testCtrl.$inject = ['$rootScope', '$scope', 'fileService','FileUploader','filesUploadService'];

function testCtrl ($rootScope, $scope, fileService,FileUploader, filesUploadService) {
	
	$scope.upload2files = [];
	
	$scope.openRemotefile = function() {
		var args = {
            path: "/home/chdeng", 
            myhome: "/home/chdeng",
            jobdir: "/home/chdeng/work",
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
    
    $scope.$watch('fileUploadProgress',function(n, o){
		if (n == undefined) {
			n = 0;
		}
			$scope.progressbar = n;
		});
    
    $scope.getFileList = function(e) {
    	$scope.$apply(function () {
        	for (var i = 0; i < e.files.length; i++) {
                $scope.upload2files.push(e.files[i]);
            }

        });
    }
    
        
    
	$scope.startUpload = function() {
		/*var queue = $scope.uploader.queue;
		angular.forEach(queue, function(item){
			if (item.file.host != 'server') {
				filesUploadService.toUploadFiles([item.file],"");
			}
		}); */
		//filesUploadService.test();
		angular.forEach($scope.upload2files, function(item){
			if (item.host != 'server') {
				filesUploadService.toUploadFiles($scope,[item],"");
			}
		});
	};
	$scope.startUpload1 = function() {
		var file = $scope.upload2files[0]; 
		var fsize = file.size;
		var filename =file.name;
		var hostName="server";
		var destPath="";
		var appName="genric";
		var xhr = new XMLHttpRequest();
        xhr.open('POST', "/api/uploadfiles?user=ylyang", true);
        xhr.onerror = function() {
        	console.log("failed to connect server");
        };
        xhr.upload.onloadstart = function(event) {
        	
        	console.log('starting to uploading...');
        };
        xhr.onreadystatechange = function(e) {

        };
        // prepare FormData
        var formData = new FormData();
        formData.append('file', file);
        formData.append('chunkIndex', 0);
        formData.append('chunkLength', fsize );
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
	}
}
        
})();
