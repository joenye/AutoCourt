/* Copyright (c) 2012: Daniel Richman. License: GNU GPL 3 */
/* Additional features: Priyesh Patel                     */

(function () {

var dataelem = "#data";
var pausetoggle = "#pause";
var scrollelems = ["html", "body"];

var url = "bookings/log";
var fix_rn = true;
var load = 300 * 1024; /* 30KB */
var poll = 3000; /* 3s */

var kill = false;
var loading = false;
var pause = false;
var reverse = true;
var log_data = "";
var log_file_size = 0;

/* :-( https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/parseInt */
function parseInt2(value) {
    if(!(/^[0-9]+$/.test(value))) throw "Invalid integer " + value;
    var v = Number(value);
    if (isNaN(v))                 throw "Invalid integer " + value;
    return v;
}

function get_log() {
    if (kill | loading) return;
    loading = true;

    /* The "log_file_size - 1" deliberately reloads the last byte, which we already
     * have. This is to prevent a 416 "Range unsatisfiable" error: a response
     * of length 1 tells us that the file hasn't changed yet. A 416 shows that
     * the file has been trucnated */

    $.ajax(url, {
        success: function (data, s, xhr) {
            log_data = data;
            show_log();
            setTimeout(get_log, poll);
        },
        error: function (xhr, s, t) {
            loading = false;

            if (xhr.status === 416 || xhr.status == 404) {
                /* 416: Requested range not satisfiable: log was truncated. */
                /* 404: Retry soon, I guess */

                log_file_size = 0;
                log_data = "";
                show_log();
                setTimeout(get_log, poll);
            } else {
                throw "Unknown AJAX Error (status " + xhr.status + ")";
            }
        }
    });
}

function scroll(where) {
    for (var i = 0; i < scrollelems.length; i++) {
        var s = $(scrollelems[i]);
        if (where === -1)
            s.scrollTop(s.height());
        else
            s.scrollTop(where);
    }
}

function show_log() {
    if (pause) return;

    var t = log_data;

    if (reverse) {
        var t_a = t.split(/\n/g);
        t_a.reverse();
        if (t_a[0] == "") 
            t_a.shift();
        t = t_a.join("\n");
    }

    if (fix_rn)
        t = t.replace(/\n/g, "\r\n");

    $(dataelem).text(t);
    if (!reverse)
        scroll(-1);
}

function error(what) {
    kill = true;

    $(dataelem).text("An error occured :-(.\r\n" +
                     "Reloading may help; no promises.\r\n" + 
                     what);
    scroll(0);

    return false;
}

$(document).ready(function () {
    window.onerror = error;

    /* Add pause toggle */
    $(pausetoggle).click(function (e) {
        pause = !pause;
        $(pausetoggle).text(pause ? "Unpause" : "Pause");
        show_log();
        e.preventDefault();
    });

    get_log();
});

})();
