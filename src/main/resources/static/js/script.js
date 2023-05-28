let port = 8081;
let stompClient;

function printProgress(response) {
    let responseObj = JSON.parse(response.body);
    switch (responseObj.downloadStatus) {
        case 'OK': {
            processOk(responseObj);
            break;
        }
        case 'ERROR': {
            processError(responseObj);
            break;
        }
        case 'WAIT': {
            processWait(responseObj);
            break;
        }
    }
//    if (responseObj.downloadStatus == 'OK') processOk(responseObj)
//    else if (responseObj.downloadStatus == 'WAIT') processWait(responseObj)
//    else processError(responseObj);
}

function processOk(responseObj) {
    let fileName = document.getElementById('file_name');
    let fileSource = document.getElementById('file_source');
    let progress = document.getElementById('progress_text');
    let percentage = document.getElementById('progress_value');
    let cancelButton = document.getElementById('current_download_cancel');
    let finishButton = document.getElementById('current_download_done');

    fileName.textContent = responseObj.fileName;
    fileSource.textContent = responseObj.fileSource;
    let percentageValue = getPercentage(responseObj.totalBytes, responseObj.length);
    let transferRate = formatTransferRate(responseObj.transferRate);
    if (percentageValue == 100) {
        // change resume and cancel to hidden and finished to visible
        finishButton.removeAttribute('hidden');
        cancelButton.remove();
        percentage.setAttribute("class", "progress-bar bg-success");
    }
    progress.textContent = insertSpacesInTransferRate(responseObj.totalBytes)
    + ' / ' + insertSpacesInTransferRate(responseObj.length)
    + ' bytes [ ' + percentageValue + '% ] | ' + transferRate;

    percentage.innerHTML = percentageValue + "%";
    percentage.setAttribute("style","width: " + percentageValue + "%");
    percentage.setAttribute("aria-valuenow", percentageValue);
}

function processWait(responseObj) {
    let progress = document.getElementById('progress_text');
    progress.textContent = 'Something went wrong, download will resume in... '
        + getReadableTime(responseObj.transferRate);
}

function getReadableTime(counter) {
    let seconds = counter / 1000;
    let hours = Math.floor(seconds / 3600);
    if (hours < 10) hours = '0' + hours;
    seconds = seconds % 3600;
    let minutes = Math.floor(seconds / 60);
    if (minutes < 10) minutes = '0' + minutes;
    seconds = Math.floor(seconds % 60);
    if (seconds < 10) seconds = '0' + seconds;
    return hours + ':' + minutes + ':' + seconds;
}

function processError(error) {
    let progress = document.getElementById('progress_text');
    document.getElementById('download_error').textContent = error.errorMessage;
}

// calculate percentage value
function getPercentage(current, total) {
       if (current > 0 && total > 0) {
            return Math.round((current / total) * 100);
       } else {
            return 100;
       }
}

function insertSpacesInTransferRate(value) {
let arr = value.toString().split('');
let arrLength = arr.length;
let splitCount = 1;
let firstBreak = arrLength % 3;
let number = '';
for (let i = 0; i < arrLength; i++) {
    if (i != 0 && i == firstBreak) {
        number += ' ';
    }
    if (i == (firstBreak + (splitCount * 3))) {
        number += ' '
        splitCount++;
    }
    number += arr[i];
}
return number;
}

function formatTransferRate(value) {
    if (value.toString().length >= 4) {
        return (value * 0.0009765625).toFixed(1) + ' MB/s';
    }
    return value + ' kB/s';
}

    // connect and subscribe to message broker at given address
function connect() {
    const socket = new SockJS("/notifications");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe("/user/notification/item", function (response) {
            printProgress(response);
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

// register client
function start() {
    if (stompClient) {
        stompClient.send("/swns/start", {});
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
    setTimeout(() => {
        start();
    }, 1000);
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