
let port = 8081;
let stompClient;

function printResponse(response) {
    let responseObj = JSON.parse(response.body);

    let fileName = document.getElementById('file_name');
    let fileSource = document.getElementById('file_source');
    let progress = document.getElementById('progress-text');
    let percentage = document.getElementById("progress");
    let history = document.getElementById('downloadHistory');
    let cancelButton = document.getElementById('currentDownloadCancel');
    let finishButton = document.getElementById('currentDownloadDone');

    fileName.textContent = responseObj.fileName;
    fileSource.textContent = responseObj.fileSource;
    let percentageValue = getPercentage(responseObj.totalBytes, responseObj.length);
    if (percentageValue == 100) {
        // change resume and cancel to hidden and finished to visible
        finishButton.removeAttribute('hidden');
        cancelButton.setAttribute('hidden');
//        finishButton.style.visibility = 'visible';
//        cancelButton.style.visibility = 'hidden';
    }
    progress.textContent = responseObj.totalBytes + ' / ' + responseObj.length + ' bytes [ ' + percentageValue + '% ]';

    percentage.innerHTML = percentageValue + "%";
    percentage.setAttribute("style","width: " + percentageValue + "%");
    percentage.setAttribute("aria-valuenow", percentageValue);

//    if (responseObj.enabled === false) {
//        let spinnerUpdated = document.getElementById("spinner").className + " visually-hidden";
//        spinner.setAttribute("class", spinnerUpdated);
//        currentFile.setAttribute("class", "text-success");
//    }
}

// calculate percentage value
function getPercentage(current, total) {
       if (current > 0 && total > 0) {
            return Math.round((current / total) * 100);
       } else {
            return 100;
       }
}

    // connect and subscribe to message broker at given address
function connect() {
//    const socket = new SockJS("http://localhost:" + port + "/notifications");
    const socket = new SockJS("/notifications");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe("/user/notification/item", function (response) {
//            console.log(' Got ' + response);
//            mess = response;
            printResponse(response);
        });
        console.info("connected!")
    });
}

//disconnect from websocket
function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
        console.info("disconnected");
    }
}

// calculate percentage value
function getPercentage(current, total) {
       if (current > 0 && total > 0) {
            return Math.round((current / total) * 100);
       } else {
            return 100;
       }
}

// register client
function start() {
    if (stompClient) {
        stompClient.send("/swns/start", {});
    }
}

function runAuto(message) {
    if (stompClient) {
        stompClient.send("/swns/runauto", {message});
    }
}

// unregister client
function stop() {
    if (stompClient) {
        stompClient.send("/swns/stop", {});
    }
}

function connectAndReceive() {
    connect();
    setTimeout(() => {start();}, 1000);
}

function stopAndDisconnect() {
    stop();
    disconnect();
}

window.onload = connectAndReceive();

// on window closing remove client from listeners and disconnect from websocket
window.onbeforeunload = function() {
    stopAndDisconnect();
};