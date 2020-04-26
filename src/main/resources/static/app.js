var stompClient = null;
var socket = null;

var sessionId = "";
var role = "";
var isDead = "";

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#registrations").html("");
}

function connect(callback) {
    socket = new SockJS('/secrethit');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {

        console.log(stompClient.ws._transport.url);

        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/registrations', function (registration) {
            showRegistration(JSON.parse(registration.body).name);
        });
        stompClient.subscribe('/user/queue/reply', function (registration) {
            console.log(registration);
            sessionId = JSON.parse(registration.body).sessionId;
            role = JSON.parse(registration.body).role;
            isDead = JSON.parse(registration.body).isDead;
        });
        // });
        stompClient.subscribe('/user/queue/errors', function(greeting) {
            alert("Error " + greeting.body);
        });
        if (typeof callback === "function") {
            callback();
        }
    }, function (error) {
        alert("STOMP error " + error)
    });
}

function sendName() {
    stompClient.send("/app/register", {}, JSON.stringify({'name': $("#name").val()}));
}

function showRegistration(message) {
    $("#registrations").append("<tr><td>" + message + "</td></tr>");
}

function waitConnect() {
    connect();
    return $.ajax();
}

function waitSendName() {
    sendName();
    return $.ajax();
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    // $( "#connect" ).click(function() { connect(); });
    // $( "#disconnect" ).click(function() { disconnect(); });
    $( "#join" ).click(function() {
        connect(sendName)
        // connect();
        // $.ajax({
        //     success: sendName()
        // });
        // waitConnect().done(function(){
        //     waitSendName();
        // });
    });
});

