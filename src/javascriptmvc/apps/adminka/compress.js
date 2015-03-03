//js apps/adminka/compress.js

MVCOptions = {
    onload: false,
    show_files: true,
    compress_callback: function(total){
        var collected = MVCOptions.collect(total);
        MVCOptions.save('apps/adminka/collected.js', collected);
        print("Compiler output start >>>>>>");
        MVCOptions.compress('apps/adminka/collected.js','apps/adminka/production.js');
        print("<<<<<< Compiler output end.");
        MVCOptions.remove('apps/adminka/collected.js');
        print("Compressed to 'apps/adminka/production.js'.");
        load('jmvc/rhino/documentation/setup.js');
        
        var app = new MVC.Doc.Application(total, "adminka");
        app.generate();
        print("Generated docs.");
        quit();
    },
    env: "compress"
}
load('jmvc/rhino/compression/setup.js');
window.location = 'apps/adminka/index.html';