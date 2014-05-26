/**
 *
 * User: ingvord
 * Date: 3/8/14
 */
;
/**
 * This model wraps File API
 *
 * @type {*}
 */
MVC.Model.File = MVC.Model.extend(
    /*@Static*/
    {
        _file_system: new MVC.FileSystem(),
        //override this to store files in a different location
        _root: './data/',
        ext: '.json',
        create: function (attributes, cbks) {
            var cbks = this._clean_callbacks(cbks);
            var instance = this.create_as_existing(attributes);
            if (!instance[this.id]) {
                throw 'Can not create File instance without id specified!';
            }
            var cbk = function (fileEntry) {
                fileEntry.createWriter(function (writer) {
                    writer.onwrite = function (evt) {
                        console.log("write succeed");
                        cbks.onSuccess(instance);
                    };

                    writer.onerror = cbks.onFailure;
                    writer.write(JSON.stringify(instance.attributes()));
                });
            };
            this._file_system.getFile(this._root + instance[this.id] + this.ext, cbk, console.error);
        },
        update: function (id, attributes, cbks) {
            var cbks = this._clean_callbacks(cbks);
            var instance = this.find(id);
            if (!instance) throw 'Can not update File instance ' + id + ' no such instance has been found';
            var cbk = function (fileEntry) {
                fileEntry.createWriter(function (writer) {
                    writer.onwrite = function (evt) {
                        console.log("write succeed");
                        cbks.onSuccess(instance);
                    };

                    writer.onerror = cbks.onFailure;
                    writer.write(JSON.stringify(instance.attributes()));
                });
            };
            this._file_system.getFile(this._root + id + this.ext, cbk, console.error);
        },
        destroy: function (id, cbks) {
            var cbks = this._clean_callbacks(cbks);
            var callback = function () {
                cbks.onSuccess();
            };

            this._file_system.getFile(this._root + id + this.ext, function (fileEntry) {
                fileEntry.remove(callback, cbks.onFailure);
            }, console.error);
        },
        find_all: function (params, cbks) {
            console.log('ROOT=' + this._root);
            var cbks = this._clean_callbacks(cbks);

            var create = MVC.Function.bind(this.create_as_existing, this);
            var result = [];
            var callback = function (dirEntry) {
                console.log('createReader');
                dirEntry.createReader().readEntries(function (entries) {
                    console.log('each entry');
                    if (entries.length == 0) {
                        cbks.onSuccess([]);
                    } else {
                        var totalEntries = entries.length - 1;
                        $.each(entries, function (ndx, entry) {
                            if (!entry.isFile) {
                                totalEntries--;
                                return;
                            }

                            entry.file(function (file) {
                                var reader = new FileReader();
                                reader.onloadend = function (evt) {
                                    console.log("read succeed: " + file.path);

                                    var attributes = JSON.parse(evt.target.result);
                                    var instance = create(attributes);
                                    result.push(instance);

                                    if (ndx == totalEntries)
                                        cbks.onSuccess(result);
                                };

                                reader.onerror = function () {
                                    totalEntries--;
                                    console.error('Failed to read file: ' + file.path);
                                };
                                reader.readAsText(file);
                            }, cbks.onFailure);
                        });
                    }
                }, console.error);
            };
            this._file_system.getDirectory(this._root, callback, console.error);
        }

    },
    /*@Prototype*/
    {


    }
);

if (!MVC._no_conflict && typeof Model.File == 'undefined') {
    Model.File = MVC.Model.File;
}