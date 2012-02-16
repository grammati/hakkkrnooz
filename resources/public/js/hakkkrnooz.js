(function() {
  var hookup, htmlFor, showStories;
  $(function() {
    hookup();
    return showStories();
  });
  hookup = function() {
    return $(document).on('keypress', 'div.story', function(e) {
      return $('#comments').html(e.target.id);
    });
  };
  showStories = function(stories) {
    return $.getJSON("/stories", function(stories) {
      var story, _i, _len, _results;
      _results = [];
      for (_i = 0, _len = stories.length; _i < _len; _i++) {
        story = stories[_i];
        _results.push($('#stories').append(htmlFor(story)));
      }
      return _results;
    });
  };
  htmlFor = function(story) {
    return $('<div/>', {
      id: story.id,
      "class": 'story',
      tabindex: 1
    }).append($('<a/>', {
      href: story.href
    }).text(story.title)).append($('<span/>', {
      "class": 'cc'
    }).text(story.cc));
  };
}).call(this);
