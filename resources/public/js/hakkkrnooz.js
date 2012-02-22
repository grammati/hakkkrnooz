(function() {
  var K, commentHtml, hookup, htmlFor, showComments, showStories, storyHtml;
  $(function() {
    hookup();
    return showStories();
  });
  K = {
    Enter: 13,
    Esc: 27,
    Space: 32,
    Left: 37,
    Up: 38,
    Right: 39,
    Down: 40
  };
  hookup = function() {
    return $(document).on('keydown', 'div.story', function(e) {
      var id, story, _ref, _ref2;
      story = $(e.target);
      id = story != null ? story.attr('id') : void 0;
      switch (e.which) {
        case K.Enter:
        case K.Right:
          if (id) {
            return showComments(id);
          }
          break;
        case K.Down:
          e.preventDefault();
          return (_ref = story.next()) != null ? _ref.focus() : void 0;
        case K.Up:
          e.preventDefault();
          return (_ref2 = story.prev()) != null ? _ref2.focus() : void 0;
      }
    });
  };
  showStories = function(stories) {
    return $.getJSON("/stories", function(stories) {
      var div, story, _i, _len, _results;
      div = $('#stories');
      _results = [];
      for (_i = 0, _len = stories.length; _i < _len; _i++) {
        story = stories[_i];
        _results.push(div.append(htmlFor(story)));
      }
      return _results;
    });
  };
  showComments = function(id) {
    var div;
    if (!/^\d+$/.exec(id)) {
      return;
    }
    div = $('#comments');
    div.empty();
    return $.getJSON("/comments/" + id, function(comments) {
      var c, _i, _len, _results;
      _results = [];
      for (_i = 0, _len = comments.length; _i < _len; _i++) {
        c = comments[_i];
        _results.push(div.append(htmlFor(c)));
      }
      return _results;
    });
  };
  htmlFor = function(obj) {
    switch (obj != null ? obj.type : void 0) {
      case 'story':
        return storyHtml(obj);
      case 'comment':
        return commentHtml(obj);
      default:
        return $('<div/>').html('??????');
    }
  };
  storyHtml = function(story) {
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
  commentHtml = function(comment) {
    return $('<div/>', {
      id: comment.id,
      "class": 'comment',
      tabindex: 1
    }).html(comment.comment.join('\n'));
  };
}).call(this);
