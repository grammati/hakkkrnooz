(function() {
  var K, commentHtml, focusParent, hookup, htmlFor, jobAdHtml, showChildren, showComments, showReplies, showStories, storyHtml;
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
    return $(document).on('keydown', 'div.item', function(e) {
      var div, item, _ref, _ref2;
      div = $(e.target);
      item = div.attr('data');
      switch (e.which) {
        case K.Enter:
        case K.Right:
          e.preventDefault();
          return showChildren(item);
        case K.Left:
          e.preventDefault();
          return focusParent(item);
        case K.Down:
          e.preventDefault();
          return (_ref = div.next()) != null ? _ref.focus() : void 0;
        case K.Up:
          e.preventDefault();
          return (_ref2 = div.prev()) != null ? _ref2.focus() : void 0;
      }
    });
  };
  showStories = function(stories) {
    return $.getJSON("/stories", function(stories) {
      var div, story, _i, _len;
      div = $('#stories');
      for (_i = 0, _len = stories.length; _i < _len; _i++) {
        story = stories[_i];
        div.append(htmlFor(story));
      }
      return $('.story:first-child', div).focus();
    });
  };
  showChildren = function(item) {
    switch (item != null ? item.type : void 0) {
      case 'story':
        return showStories(item);
      case 'comment':
        return showReplies(item);
    }
  };
  showComments = function(story) {
    var div, id;
    id = story != null ? story.id : void 0;
    if (!/^\d+$/.exec(id)) {
      return;
    }
    div = $('#comments');
    div.empty();
    return $.getJSON("/comments/" + id, function(comments) {
      var c, _i, _len;
      for (_i = 0, _len = comments.length; _i < _len; _i++) {
        c = comments[_i];
        c.parent = story;
        div.append(htmlFor(c));
      }
      return $('.comment:first-child', div).focus();
    });
  };
  showReplies = function(comment) {
    return "FIXME";
  };
  focusParent = function(item) {
    return $('#' + item.id).focus();
  };
  htmlFor = function(obj) {
    var e;
    e = (function() {
      switch (obj != null ? obj.type : void 0) {
        case 'story':
          return storyHtml(obj);
        case 'job-ad':
          return jobAdHtml(obj);
        case 'comment':
          return commentHtml(obj);
        default:
          return $('<div/>').html('??????');
      }
    })();
    e.data = obj;
    return e;
  };
  storyHtml = function(story) {
    return $('<div/>', {
      id: story.id,
      "class": 'item story',
      tabindex: 1
    }).append($('<a/>', {
      href: story.href,
      "class": 'story-link'
    }).text(story.title)).append($('<span/>', {
      "class": 'cc'
    }).text(story.cc));
  };
  jobAdHtml = function(ad) {
    return storyHtml(ad);
  };
  commentHtml = function(comment) {
    return $('<div/>', {
      id: comment.id,
      "class": 'item comment',
      tabindex: 1
    }).append($('<div/>', {
      "class": 'comment-text'
    }).html(comment.comment.join('\n'))).append($('<div/>', {
      "class": 'comment-children'
    }));
  };
}).call(this);
