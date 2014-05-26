//js apps/app/compress.js

MVCOptions = {
    onload           :false,
    show_files: true,
    compress_callback:function (total) {
        var compressed = MVCOptions.collect_and_compress(total);
        MVCOptions.save('apps/app/production.js', compressed);
        print("Compressed to 'apps/app/production.js'.");
        load('jmvc/rhino/documentation/setup.js');

        var app = new MVC.Doc.Application(total, "app");
        app.generate();
        print("Generated docs.");
        quit();
    },
    env              :"compress"
}
load('jmvc/rhino/compression/setup.js');
window.location = 'apps/app/index.html';
