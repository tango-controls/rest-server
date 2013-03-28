include.css('jquery.mobile-1.2.0-custom','all');
include.resources();
include.engines('mTango');
include.plugins(
    'controller','controller/stateful',
    'view','view/helpers',                         
    'dom/element',
    'model'
    );

include(function(){ //runs after prior includes are loaded
  include.models('Page','TangoTest');
  include.controllers(
      'mTango-web',
      'DataReadWrite','ExecuteCommand','SampleApplication');
  include.views(
      //empty page template
      'views/empty_page',
      //pages
      'views/data_read_write/header','views/data_read_write/content','views/data_read_write/footer',
      'views/exec_cmd/header','views/exec_cmd/content','views/exec_cmd/footer',
      'views/smpl_app/header','views/smpl_app/content','views/smpl_app/footer',
      //controllers
      'views/DataReadWrite/poll-attribute',
      'views/ExecuteCommand/execute-cmd'
  );
});