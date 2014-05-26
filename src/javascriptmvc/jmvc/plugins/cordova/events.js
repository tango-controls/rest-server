/**
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 30.03.13
 */
/**
 * Represents onDeviceReady action. This action happens when cordova fires it.
 *
 * @type {*}
 */
MVC.Controller.Action.DeviceEvent = MVC.Controller.Action.Event.extend(
    /* @static */
    {
        /**
         * Cordova lifecycle events:
         *
         * mobileinit - handles jquery mobile configuration
         * deviceready
         * pause
         * resume
         * online
         * offline
         * backbutton
         * batterycritical
         * batterylow
         * batterystatus
         * menubutton
         * searchbutton
         * startcallbutton
         * endcallbutton
         * volumedownbutton
         * volumeupbutton
         *
         *
         * matches "events$"
         */
        match: new 
RegExp("mobileinit|deviceready|pause|resume|online|offline|backbutton|batterycritical|batterylow|batterystatus|menubutton|searchbutton|startcallbutton|endcallbutton|volumedownbutton|volumeupbutton$")
    },
    /* @prototype */
    {
        /**
         *
         * @param {Object} action
         * @param {Object} f
         * @param {Object} controller
         */
        init: function(action_name, callback, className){
            this.action = action_name;
            this.callback = callback;
            this.className = className;
            this.element = document;
            this.event_type = action_name;

            MVC.Event.observe(document, this.event_type, callback, this.fail);
        },
        fail:function(error){
            alert("Шеф, всё пропало - гипс снимают, клиент уезжает...");
            alert(error);
        }
    }
);
