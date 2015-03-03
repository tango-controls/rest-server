/**
* Main controller
*
* @type {MVC.Controller}
*/
AdminkaController = MVC.Controller.extend('main',{
    /**
    * This is the main entry point of the application. This function is invoked after jmvc has been completely initialized.
    *
    * @param {Object} params
    */
    load: function(params){
    	document.body.innerHTML += "<h1 id='hello'>Under construction!!!</h1>";
    }
});