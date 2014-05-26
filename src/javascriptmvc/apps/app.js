include.resources();
include.plugins('controller', 'view');

include(function () {
    include.models();
    include.controllers('app','app/todos');
    include.views();
});