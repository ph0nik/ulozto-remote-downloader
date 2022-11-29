
let port = 8081;
let stompClient;

function printResponse(response) {
    let responseObj = JSON.parse(response.body);

    let fileName = document.getElementById('file_name');
    let fileSource = document.getElementById('file_source');
    let progress = document.getElementById('progress-text');
    let percentage = document.getElementById('progress');
    let history = document.getElementById('downloadHistory');
    let cancelButton = document.getElementById('currentDownloadCancel');
    let finishButton = document.getElementById('currentDownloadDone');

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
    progress.textContent = responseObj.totalBytes + ' / ' + responseObj.length + ' bytes [ ' + percentageValue + '% ] | ' + transferRate;

    percentage.innerHTML = percentageValue + "%";
    percentage.setAttribute("style","width: " + percentageValue + "%");
    percentage.setAttribute("aria-valuenow", percentageValue);

}

// calculate percentage value
function getPercentage(current, total) {
       if (current > 0 && total > 0) {
            return Math.round((current / total) * 100);
       } else {
            return 100;
       }
}
// TODO
function insertSpacesInTransferRate(value) {
    let j = Math.floor(value.toString().length / 3);
    let div = value.toString().length - (j * 3);
    let lead = value.toString().slice(0, div);
    let rest = value.toString().slice(div, value.toString().length);
       for (let i = 0; i < j; i++) {

       }
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