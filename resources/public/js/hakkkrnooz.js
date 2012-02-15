(function() {
  var htmlFor, showStories;
  $(function() {
    hookup;    return showStories;
  });
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
      "class": 'story'
    }).append($('<a/>', {
      href: story.href
    }).text(story.title)).append($('<span/>', {
      "class": 'cc'
    }).text(story.cc));
  };
}).call(this);
