(function() {
  var K, Kmap, KmapVim, addColumn, appendComments, columnWidth, commentCache, commentHtml, commentTemplate, focusNext, focusPrev, getParent, htmlFor, initEvents, jobAdHtml, loading, onItemFocus, openCurrent, positionItem, removeFollowingColumns, scrollH, showChildren, showComments, showReplies, showStories, storyHtml, storyTemplate, upToParent;

  _.templateSettings = {
    interpolate: /\{\{(.+?)\}\}/g
  };

  storyTemplate = _.template($('#story-template').text());

  commentTemplate = _.template($('#comment-template').text());

  columnWidth = 550;

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
    return $(document).on('keydown', 'div.item', function(evt) {
      var fn;
      if (evt.ctrlKey || evt.altKey || evt.shiftKey) return;
      fn = KmapVim(evt.which);
      if (fn) {
        fn($(evt.target));
        return evt.preventDefault();
      }
    }).on('focus', 'div.item', function(evt) {
      return onItemFocus(evt.target);
    });
  };

  focusNext = function(elt) {
    var _ref;
    return (_ref = elt.next()) != null ? _ref.focus() : void 0;
  };

  focusPrev = function(elt) {
    var _ref;
    return (_ref = elt.prev()) != null ? _ref.focus() : void 0;
  };

  onItemFocus = function(elt) {
    positionItem(elt);
    return removeFollowingColumns(elt);
  };

  positionItem = function(elt) {
    var column, isFirst, pos, targetTop;
    elt = $(elt);
    pos = elt.position();
    column = $(elt.parent());
    isFirst = elt.prev().size() === 0;
    targetTop = (isFirst != null ? isFirst : {
      0: 25
    }) - pos.top;
    return column.stop().animate({
      'top': targetTop
    }, 'fast');
  };

  showStories = function() {
    var div;
    div = $('#stories');
    div.css('width', columnWidth);
    div.attr('data-column-number', 1);
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

  addColumn = function() {
    var columns, ncols, newCol;
    columns = $('#content > div.column');
    ncols = columns.length;
    newCol = $('<div/>').addClass('column').css('left', ncols * columnWidth).attr('data-column-number', 1 + ncols);
    $('#content').append(newCol);
    return newCol;
  };

  removeFollowingColumns = function(elt) {
    return $(elt).parent().nextAll('.column').stop().animate({
      width: 0
    }, 'fast', function() {
      return $(this).remove();
    });
  };

  commentCache = {};

  scrollH = function() {
    var overWidth;
    overWidth = $(document).width() - $(window).width();
    return $('body').animate({
      scrollLeft: Math.max(0, overWidth)
    });
  };

  loading = null;

  showComments = function(story) {
    var div, id;
    id = story != null ? story.attr('id') : void 0;
    if (!/^\d+$/.exec(id)) return;
    if (loading === id) return;
    loading = id;
    div = addColumn();
    div.addClass('loading');
    return $.ajax({
      url: "/comments/" + id,
      dataType: 'json',
      success: function(comments) {
        if (loading !== id) return;
        div.removeClass('loading');
        appendComments(comments, div, id);
        story.addClass('active-parent');
        $('.comment:first-child', div).focus();
        return scrollH();
      },
      error: function() {
        if (loading === id) return loading = null;
      }
    });
  };

  showReplies = function(commentDiv) {
    var comment, div, id, replies;
    id = $(commentDiv).attr('id');
    comment = commentCache[id];
    replies = comment != null ? comment.replies : void 0;
    if (!comment || replies.length === 0) return;
    div = addColumn();
    appendComments(replies, div, id);
    commentDiv.addClass('active-parent');
    $('.comment:first-child', div).focus();
    scrollH();
    return div.animate({
      width: ['0px', '550px']
    }, 'fast');
  };

  appendComments = function(comments, div, parentid) {
    var c, e, _i, _len, _results;
    _results = [];
    for (_i = 0, _len = comments.length; _i < _len; _i++) {
      c = comments[_i];
      commentCache[c.id] = c;
      e = htmlFor(c);
      e.attr('parentid', parentid);
      _results.push(div.append(e));
    }
    return _results;
  };

  getParent = function(item) {
    var parentid;
    parentid = item.attr('parentid');
    if (parentid) return $('#' + parentid);
  };

  upToParent = function(item) {
    var parent;
    if (item.attr('type') !== 'comment') return;
    parent = getParent(item);
    if (parent) {
      parent.removeClass('active-parent');
      return parent.focus();
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
    var data;
    data = $.extend({
      id: '-1',
      href: '#',
      title: '???',
      cc: 0
    }, story);
    return $(storyTemplate({
      s: data
    })).data('story', story);
  };

  jobAdHtml = function(ad) {
    return storyHtml(ad);
  };

  commentHtml = function(comment) {
    var data;
    data = $.extend({
      id: '-1',
      user: '',
      replies: [],
      comment: ''
    }, comment);
    return $(commentTemplate({
      c: data
    }));
  };

}).call(this);
