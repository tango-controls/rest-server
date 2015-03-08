include.namespace = function(){
    //TODO ?!
};

include.css = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().stylesheets,arguments[i]))
            include.app().stylesheets.push(arguments[i]);
    }
}

include.engines = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().engines,arguments[i]))
            include.app().engines.push(arguments[i]);
    }
}

include.resources = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().resources,arguments[i]))
            include.app().resources.push(arguments[i]);
    }
}

include.plugins = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().plugins,arguments[i]))
            include.app().plugins.push(arguments[i]);
    }
}

include.models = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().models,arguments[i]))
            include.app().models.push(arguments[i]);
    }
}

include.controllers = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().controllers,arguments[i]))
            include.app().controllers.push(arguments[i]);
    }
}

include.views = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().views,arguments[i]))
            include.app().views.push(arguments[i]);
    }
}

include.unit_tests = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().tests.unit,arguments[i]))
            include.app().tests.unit.push(arguments[i]);
    }
}
include.functional_tests = function(){
    for(var i = 0, size = arguments.length;i<size;++i){
        if(!MVC.Array.include(include.app().tests.functional,arguments[i]))
            include.app().tests.functional.push(arguments[i]);
    }
}

create_app_json = function(app_name){
    MVCOptions.create_folder("apps/"+app_name);
    render_to("apps/"+app_name+"/" + app_name + ".json", "jmvc/rhino/command/templates/application.json",data);
}

/**
 *
 * @param app_name
 * @return {JSON} json structure of the app
 */
read_app_json = function(app_name){
    if(!MVCOptions.exists("apps/"+app_name+"/" + app_name + ".json")) {
        throw "\n\n\tApplicationNotFound: application["+app_name+"] does not exist!\n\t\t Please creates it first executing 'js jmvc\\generate\\app "+app_name+"'(Windows) or './js jmvc/generate/app "+app_name+"'(Linux/Mac)";
    }

    var appJson = readFile("apps/"+app_name+"/" + app_name + ".json");
    var app = JSONparse(appJson);
    return app;
};

/**
 * Reads json and applies app.js to it
 *
 * @param app_name
 * @return {JSON} json structure of the app
 */
load_app_json = function(app_name){
    var app = read_app_json(app_name);
    include.app = function(){
        return app;
    };
    if(MVCOptions.exists('apps/' + app_name + '.js')) {
        load('apps/' + app_name + '.js')
    }
    if(MVCOptions.exists('apps/' + app_name + '/test.js')) {
        load('apps/' + app_name + '/test.js')
    }

    return app;
};

/**
 *
 * @param {JSON} app
 * @param {Object} obj
 */
update_app_json = function(app, obj){
    var app_name = app.application_name;
    print("Merging obj into app... ");
    for(var a in obj){
        if(!obj.hasOwnProperty(a)) continue;
        if(a == 'tests'){
            //functional tests
            for(var i = 0, size = obj[a].functional.length;i<size;++i){
                if(!MVC.Array.include(app[a].functional,obj[a].functional[i])) {
                    app[a].functional.push(obj[a].functional[i]);
                    print("Add tests/functional/"+i);
                }
            }
            //unit tests
            for(var i = 0, size = obj[a].unit.length;i<size;++i){
                if(!MVC.Array.include(app[a].unit,obj[a].unit[i])) {
                    app[a].unit.push(obj[a].unit[i]);
                    print("Add tests/unit/"+i);
                }
            }
        } else {
            for(var i = 0, size = obj[a].length;i<size;++i){
                if(!MVC.Array.include(app[a],obj[a][i])) {
                    app[a].push(obj[a][i]);
                    print("Add "+a+"/"+i);
                }
            }
        }
    }
    print("Done.\n")
    var app_json = 'apps/' + app_name + '/' + app_name + '.json';
    MVCOptions.save(app_json,  MVC.Object.to_json(app)  );
    render_to("apps/"+app_name+".js", "jmvc/rhino/command/templates/application.ejs", app);
    render_to("apps/"+app_name+"/test.js", "jmvc/rhino/command/templates/test.ejs", app);
    print("               apps/"+app_name+"/"+app_name+".json\n")
};

save_app_json = function(app_name, app){
    var app_json = 'apps/' + app_name + '/' + app_name + '.json';
    MVCOptions.save(app_json,  MVC.Object.to_json(app)  );
    render_to("apps/"+app_name+".js", "jmvc/rhino/command/templates/application.ejs", app);
    render_to("apps/"+app_name+"/test.js", "jmvc/rhino/command/templates/test.ejs", app);
    print("               apps/"+app_name+"/"+app_name+".json\n")
};

/**
 *
 * @param app_name
 * @param path_to_entity [models/whatever/Model.js]
 */
add_entity = function(app_name, path_to_entity){
    var match = path_to_entity.match(/(controllers|engines|plugins|models|resources|stylesheets|test\/functiona|test\/unit|views)\/(.*)\.(js|ejs|css)/);
    var type = match[1];
    var entity = match[2];
    var ext = match[3];

    var app = load_app_json(app_name);

    if(type != 'test/unit' && type != 'test/functional')
        app[type].push(entity);
    else
        switch(type){
            case 'test/unit':
                app.tests.unit.push(entity);
                break;
            case 'test/functional':
                app.tests.functional.push(entity);
                break;
        }

    save_app_json(app_name, app);
};