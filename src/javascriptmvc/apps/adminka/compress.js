//js apps/adminka/compress.js

MVCOptions = {
onload: false,
show_files: true,
compress_callback: function(total){
var compressed = MVCOptions.collect_and_compress(total);
MVCOptions.save('apps/adminka/production.js', compressed);
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