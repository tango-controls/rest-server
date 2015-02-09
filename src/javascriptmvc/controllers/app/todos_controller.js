TodosController = Controller.extend('todos', {
    mouseover:function (params) {
        params.element.style.backgroundColor = 'Green';
    },
    mouseout :function (params) {
        params.element.style.backgroundColor = '';
    }
});