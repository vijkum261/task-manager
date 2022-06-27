$(document).ready(function(){
      $("#start_datepicker" ).datepicker();
      loadActiveTasks()
      loadCompletedTasks()
    })

loadActiveTasks = function () {
    $.ajax({
        url: '/api/tasks/read/active',
        type: 'POST',
        success: function (response) {
            var ulHTML = '<h5> No Active Tasks</h5>';
            $.each(response, function (i, item) {
                ulHTML += '<div class="list-group-item list-group-item-action flex-column align-items-start"><div class="form-check"><input class="form-check-input active-task-box" type="checkbox" onClick="completeTask(\'' + item.id + '\')"><label class="form-check-label" for="flexCheckDefault">' + item.text + '</label></div><small class="text-muted">' + computeDueBy(item.dueBy) + '</small></div>'
            });
            $('#active_tasks').empty();
            $('#active_tasks').append(ulHTML);
        }
    });
}

loadCompletedTasks = function () {
    $.ajax({
        url: '/api/tasks/read/completed',
        type: 'POST',
        success: function (response) {
            var ulHTML = '';
            $.each(response, function (i, item) {
                ulHTML += '<div class="list-group-item list-group-item-action flex-column align-items-start"><div class="d-flex w-100 justify-content-between"><h5 class="mb-1">' + item.text + '</h5><small>' + moment(item.completedOn).format('MMM Do') + '</small></div></div>'
            });
            $('#completed_tasks').empty();
            $('#completed_tasks').append(ulHTML);
        }
    });
}

computeDueBy = function (dueBy) {
    var currentDate = moment()
    currentDate.subtract(1, 'd')
    if (currentDate.isAfter(moment(dueBy))) {
        return '<b style="color:red;">Past Due</b>'
    } else {
        return 'Due by: ' + moment(dueBy).format('MMM Do')
    }
}

completeTask = function(task_id) {
    $.ajax({
        url: '/api/tasks/complete',
        type: 'POST',
        data: JSON.stringify({
            id: task_id
        }),
        success: function (response) {
            loadCompletedTasks();
            loadActiveTasks();
        }
    });
}

addTask = function() {
console.log(moment($("#start_datepicker" ).val()).utc().endOf("day").toDate())
    $.ajax({
        url: '/api/tasks/add',
        type: 'POST',
        data: JSON.stringify({
            text: $("#task_text" ).val(),
            dueBy: moment($("#start_datepicker" ).val()).endOf("day").toDate()
        }),
        success: function (response) {
            loadCompletedTasks();
            loadActiveTasks();
            $("#task_text" ).val('');
            $("#start_datepicker" ).val('');
        }
    });
}