(function() {
  var K, Kmap, KmapVim, commentHtml, focusNext, focusPrev, getParent, htmlFor, initEvents, jobAdHtml, openCurrent, showChildren, showComments, showReplies, showStories, storyHtml, upToParent;

  $(function() {
    showStories();
    return initEvents();
  });

  K = {
    Enter: 13,
    Esc: 27,
    Space: 32,
    Left: 37,
    Up: 38,
    Right: 39,
    Down: 40,
    H: 72,
    J: 74,
    K: 75,
    L: 76
  };

  Kmap = function(key) {
    switch (key) {
      case K.Enter:
        return openCurrent;
      case K.Right:
        return showChildren;
      case K.Left:
        return upToParent;
      case K.Down:
        return focusNext;
      case K.Up:
        return focusPrev;
    }
  };

  KmapVim = function(key) {
    switch (key) {
      case K.L:
        return showChildren;
      case K.H:
        return upToParent;
      case K.J:
        return focusNext;
      case K.K:
        return focusPrev;
      default:
        return Kmap(key);
    }
  };

  initEvents = function() {
    return $(document).on('keydown', 'div.item', function(e) {
      var fn;
      fn = KmapVim(e.which);
      if (fn) {
        fn($(e.target));
        return e.preventDefault();
      }
    });
  };

  focusNext = function(e) {
    var _ref;
    return (_ref = e.next()) != null ? _ref.focus() : void 0;
  };

  focusPrev = function(e) {
    var _ref;
    return (_ref = e.prev()) != null ? _ref.focus() : void 0;
  };

  showStories = function() {
    var div;
    div = $('#stories');
    div.addClass('loading');
    return $.getJSON("/stories", function(stories) {
      var story, _i, _len;
      div.removeClass('loading');
      for (_i = 0, _len = stories.length; _i < _len; _i++) {
        story = stories[_i];
        div.append(htmlFor(story));
      }
      return $('.story:first-child', div).focus();
    });
  };

  openCurrent = function(item) {
    switch (item != null ? item.attr('type') : void 0) {
      case 'story':
        return window.open(item.data('story').href);
      case 'comment':
        return expandComment;
    }
  };

  showChildren = function(item) {
    switch (item != null ? item.attr('type') : void 0) {
      case 'story':
        return showComments(item);
      case 'comment':
        return showReplies(item);
    }
  };

  showComments = function(story) {
    var div, id;
    id = story != null ? story.attr('id') : void 0;
    if (!/^\d+$/.exec(id)) return;
    div = $('#comments');
    div.empty();
    div.addClass('loading');
    return $.getJSON("/comments/" + id, function(comments) {
      var c, e, _i, _len;
      div.removeClass('loading');
      for (_i = 0, _len = comments.length; _i < _len; _i++) {
        c = comments[_i];
        e = htmlFor(c);
        e.attr('parentid', id);
        div.append(e);
      }
      story.addClass('active-parent');
      return $('.comment:first-child', div).focus();
    });
  };

  showReplies = function(comment) {
    $('.comment-children', $(comment).parent()).hide();
    return $('.comment-children', comment).show();
  };

  getParent = function(item) {
    var parentid;
    parentid = item.attr('parentid');
    if (parentid) return $('#' + parentid);
  };

  upToParent = function(item) {
    var parent, _ref;
    if (item.attr('type') !== 'comment') return;
    if ((_ref = item.parent()) != null) _ref.empty();
    parent = getParent(item);
    if (parent) {
      parent.focus();
      return parent.removeClass('active-parent');
    }
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
    e.attr('type', obj.type);
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
    }).text(story.cc)).data('story', story);
  };

  jobAdHtml = function(ad) {
    return storyHtml(ad);
  };

  commentHtml = function(comment) {
    var replies, reply, _i, _len, _ref;
    replies = $('<div/>', {
      "class": 'comment-children'
    });
    _ref = comment.replies;
    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
      reply = _ref[_i];
      replies.append(commentHtml(reply));
    }
    return $('<div/>', {
      id: comment.id,
      "class": 'item comment',
      tabindex: 1
    }).append($('<div/>', {
      "class": 'comment-text'
    }).html(comment.comment.join('\n'))).append(replies);
  };

}).call(this);
