Cordova location background service
===================================

Plugin for get the current user location information and then send to server or a particular API based on fixed time interval which will define in config at the time of service call.


## Supported Platforms
- Android


## Installation
The plugin can be installed via [Cordova-CLI][CLI] and is publicly available on [NPM][npm].

Execute from the projects root folder:

    $ cordova plugin add cordova-plugin-locationbgservice

Or install a specific version:

    $ cordova plugin add cordova-plugin-locationbgservice@VERSION


### Start
Start the location tracking service

```js
let options = {
	clientId: "", // Can be a userId/clientId/uniqueId for identity.
	url: '', // API url which is recevied the location.
	interval: 5000 // Fixed time interval
}
window.LocationBGService.start(function (reponse) {
	console.log(reponse);
}, function (error) {
	console.log(error);
}, options);
```

### Stop
Stop the service

```js
window.LocationBGService.stop();
```

### Enabled
Check the status of the service

```js
window.LocationBGService.isRunning(function(response){
    console.log(response);
});
```

**********************
API can receive the object like an example
**********************
```
{
	"clientId": "1234",
	"provider": "network",
	"bearing": "0.0",
	"latitude": "28.6613927",
	"accuracy": "22.209999084472656",
	"speed": "0.0",
	"longitude": "77.3849333"
}
```

Report issues to `mail <mailto:admin@dkrock.com>`