/**
 * Model for connecting to resources with JSONP
 *
 * Since JSONP does not really have a way to distinguish between timeout and any exception on the server
 * side usually onComplete serves as onFailure.
 *
 * It is expected that if server answers in a normal way then instances of this model will be created and user
 * may verify that there is no errors by checking corresponding field of this model. onFailure may be also
 * called if response contains an array of errors.
 *
 * If there is no answer from the server a provided onFailure callback will be called with url as the argument
 */
MVC.Model.JsonP = MVC.Model.extend(
    {
        error_timeout          : 4,
        init                   : function () {
            if (!this.className) return;
            if (!this.domain) throw('a domain must be provided for remote model');
            if (!this.controller_name)
                this.controller_name = this.className;
            this.plural_controller_name = MVC.String.pluralize(this.controller_name);
            this._super();
        },
        find_all               : function (params, cbs) {
            var callbacks = this._clean_callbacks(cbs);
            var callback = callbacks.onSuccess;
            var error_callback = callbacks.onFailure;

            this.add_standard_params(params, 'find_all');

            var n = parseInt(Math.random() * 100000);
            //params.callback = MVC.String.classize(this.className)+'.listCallback'+n;
            var url = this.find_url ? this.find_url + "?" : this.domain + '/' + this.plural_controller_name + '.json?';
            //var url = url + MVC.Object.to_query_string(params)+'&'+n;
            //make callback function create new and call the callback with them
            if (!callback) callback = (function () {
            });


            new MVC.JsonP(url, {
                error_timeout:this.error_timeout,
                parameters: params,
                onFailure : error_callback,
                onComplete : MVC.Function.bind(function (callback_params) {
                    var newObjects = this.create_many_as_existing(callback_params);
                    if(callback_params.errors)
                        error_callback(newObjects);
                    else
                        callback(newObjects);
                }, this),
                method    : 'get'
            })


            /*
             var error_timer = this.check_error(url, error_callback);
             this['listCallback'+n] = function(callback_params){
             clearTimeout(error_timer);
             var newObjects = this.create_many_as_existing( callback_params);
             this.remove_scripts();
             callback(newObjects);
             delete this['listCallback'+n];
             };
             params['_method'] = 'GET';
             clearTimeout(this.remove_scripts_timer);




             include(url);*/
        },
        create                 : function (params, cbs) {

            var callbacks = this._clean_callbacks(cbs);
            var callback = callbacks.onSuccess;
            var error_callback = callbacks.onFailure;

            this.add_standard_params(params, 'create');


            var klass = this, className = this.className,
                url = this.create_url ? this.create_url + "?" : this.domain + '/' + this.plural_controller_name + '.json?';
            var tll = this.top_level_length(params, url);
            var result = this.seperate(params[this.controller_name], tll, this.controller_name);
            var postpone_params = result.postpone, send_params = result.send;

            if (!callback) callback = (function () {
            });

            params['_method'] = 'POST';

            if (result.send_in_parts) {
                params[this.controller_name] = send_params;
                params['_mutlirequest'] = 'true';

                new MVC.JsonP(url, {
                    error_timeout:this.error_timeout,
                    parameters: params,
                    onComplete: MVC.Function.bind(this.parts_create_callback(params, callback, postpone_params), this),
                    onFailure : error_callback,
                    method    : 'post'
                });

                /*klass.createCallback = ;
                 params[this.controller_name] = send_params;
                 params['_mutlirequest'] = 'true';
                 clearTimeout(this.remove_scripts_timer);
                 include(url+MVC.Object.to_query_string(params)+'&'+Math.random());*/

            } else {
                params['_mutlirequest'] = null;

                new MVC.JsonP(url, {
                    error_timeout:this.error_timeout,
                    parameters: params,
                    onComplete: MVC.Function.bind(this.single_create_callback(callback, error_callback), this),
                    onFailure : error_callback,
                    method    : 'post'
                });


                //clearTimeout(this.remove_scripts_timer);
                //include(url+MVC.Object.to_query_string(params)+'&'+Math.random());
            }
        },
        update:function(id,attributes,cbks){
            var params = {};

            params[this.id] = id;
            MVC.Object.extend(params,attributes);

            this.add_standard_params(params, 'update');

            this.create(params,cbks);
        },
        destroy:function(id,cbks){
            var params = {};

            params[this.id] = id;

            var callbacks = this._clean_callbacks(cbks);
            var callback = callbacks.onSuccess;
            var error_callback = callbacks.onFailure;

            //TODO synch server side delete->destroy
            this.add_standard_params(params, 'delete');

            var n = parseInt(Math.random() * 100000);
            //params.callback = MVC.String.classize(this.className)+'.listCallback'+n;
            var url = this.destroy_url ? this.destroy_url + "?" : this.domain + '/' + this.plural_controller_name + '.json?';
            //var url = url + MVC.Object.to_query_string(params)+'&'+n;
            //make callback function create new and call the callback with them
            if (!callback) callback = (function () {
            });


            new MVC.JsonP(url, {
                error_timeout:this.error_timeout,
                parameters: params,
                onFailure : error_callback,
                onComplete : MVC.Function.bind(this.standard_callback(callback,error_callback),this),
                method    : 'delete'
            })
        },
        standard_callback:function(callback,error_callback){
            return function (callback_params) {
                if (callback_params.errors) {
                    error_callback(callback_params.errors);
                } else {
                    callback(callback_params);
                }
            };
        },
        parts_create_callback  : function (params, callback, postpone_params) {
            return function (callback_params) {
                if (!callback_params.id) throw 'Your server must callback with the id of the object.  It is used for the next request';
                params[this.controller_name] = postpone_params;
                params.id = callback_params.id;
                this.create(params, callback);
            };
        },
        single_create_callback : function (callback, error_callback) {
            return function (callback_params) {
                if (callback_params.errors) {
                    var inst = new this(callback_params[this.className] ? callback_params[this.className]: {});
                    inst.add_errors(callback_params.errors);
                    error_callback(inst);
                } else {
                    callback(this.create_as_existing(callback_params));
                }
            };
        },
        /**
         * //TODO redefine server error protocol
         *
         * Server always returns single object in case of error.
         *
         * This method works around this limitation.
         *
         * @param instances an array of raw objects from the server
         * @return {Array} an array of newly created objects
         */
        create_many_as_existing: function (instances) {
            if (!instances) return [];
            if (instances.errors) return [this.create_as_existing(instances)];

            return this._super(instances);
        },
        add_standard_params    : function (params, action) {
            if (!params.referer) params.referer = window.location.href;
            if (!params.action) params.action = action;
        },
        callback_name          : 'callback',
        domain                 : null,
        top_level_length       : function (params, url) {
            var p = MVC.Object.extend({}, params);
            delete p[this.controller_name];
            return url.length + MVC.Object.to_query_string(p).length;

        },
        seperate               : function (object, top_level_length, name) {
            var remainder = 2000 - 9 - top_level_length;
            var send = {};
            var postpone = {};
            var send_in_parts = false;
            for (var attr in object) {
                if (!object.hasOwnProperty(attr)) continue;
                var value = object[attr], value_length;
                var attr_length = encodeURIComponent(name + '[' + attr + ']').length;

                if (typeof value == 'string') {
                    value_length = encodeURIComponent(value).length;
                } else {
                    value_length = value.toString().length;
                }

                if (remainder - attr_length <= 30) {
                    postpone[attr] = value;
                    send_in_parts = true;
                    continue;
                }
                ;
                remainder = remainder - attr_length - 2; //2 is for = and &
                if (remainder > value_length) {
                    send[attr] = value;
                    remainder -= value_length;
                } else if (typeof value == 'string') {
                    var guess = remainder;
                    while (encodeURIComponent(value.substr(0, guess)).length > remainder) {
                        guess = parseInt(guess * 0.75) - 1;
                    }
                    send[attr] = value.substr(0, guess);
                    postpone[attr] = value.substr(guess);
                    send_in_parts = true;
                    remainder = 0;
                } else {
                    postpone[attr] = value;
                }
            }
            return {send: send, postpone: postpone, send_in_parts: send_in_parts};
        },
        random                 : parseInt(Math.random() * 1000000)
    },
//prototype functions
    {});

