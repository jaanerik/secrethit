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

function clearBoard() {
    $("body").empty();
}

function connect(callback) {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    socket = new SockJS('/secrethit');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {

        console.log(stompClient.ws._transport.url);

        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/registrations', function (registrationsOrStart) {
            if (registrationsOrStart.body === "$start$") {
                //subscribe to gameState endpoint?
                clearBoard();
            } else {
                clearRegistrations();
                JSON.parse(registrationsOrStart.body).forEach(it =>
                    showRegistration(it.name)
                );
                if (JSON.parse(registrationsOrStart.body).length >= 5) {
                    showGameStart();
                }
            }
        });
        stompClient.subscribe('/user/queue/reply', function (registration) {
            console.log(registration);
            sessionId = JSON.parse(registration.body)["sessionId"];
            role = JSON.parse(registration.body)["role"];
            isDead = JSON.parse(registration.body)["isDead"];
            alert("You are a " + role + "!")
        });
        // });
        stompClient.subscribe('/user/queue/errors', function (greeting) {
            console.log(greeting);
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

function clearRegistrations() {
    $("#registrations *").remove();
}

function showGameStart() {
    $("#startGame").css("display", "block");
}

function showRegistration(message) {
    $("#registrations").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#join" ).click(function() {
        connect(sendName)
    });
    $( "#startGame").click(function () {
        if (stompClient != null)
            stompClient.send("/app/register", {}, JSON.stringify({'name': '$start$'}));
    });
});

