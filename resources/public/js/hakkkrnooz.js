(function() {
  var K, Kmap, KmapVim, addColumn, appendComments, columnWidth, commentCache, commentHtml, commentTemplate, focusNext, focusPrev, getParent, htmlFor, initEvents, jobAdHtml, openCurrent, removeLastColumn, scrollH, showChildren, showComments, showReplies, showStories, storyHtml, storyTemplate, upToParent;

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

  removeLastColumn = function() {
    var cols;
    cols = $('#content > div.column');
    if (cols.length > 1) return cols.last().remove();
  };

  commentCache = {};

  showComments = function(story) {
    var div, id;
    id = story != null ? story.attr('id') : void 0;
    if (!/^\d+$/.exec(id)) return;
    div = addColumn();
    div.addClass('loading');
    return $.getJSON("/comments/" + id, function(comments) {
      div.removeClass('loading');
      appendComments(comments, div, id);
      story.addClass('active-parent');
      return $('.comment:first-child', div).focus();
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
    return scrollH();
  };

  appendComments = function(comments, div, parentid) {
    var c, e, _i, _len;
    for (_i = 0, _len = comments.length; _i < _len; _i++) {
      c = comments[_i];
      commentCache[c.id] = c;
      e = htmlFor(c);
      e.attr('parentid', parentid);
      div.append(e);
    }
    return div.fadeIn('fast');
  };

  scrollH = function() {
    var overWidth;
    overWidth = $(document).width() - $(window).width();
    return window.scroll(Math.max(0, overWidth), window.scrollY);
  };

  getParent = function(item) {
    var parentid;
    parentid = item.attr('parentid');
    if (parentid) return $('#' + parentid);
  };

  upToParent = function(item) {
    var parent, _ref;
    if (item.attr('type') !== 'comment') return;
    if ((_ref = item.parent()) != null) _ref.remove();
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
    var data;
    data = $.extend({
      id: '-1',
      href: '#',
      title: '???',
      cc: 0
    }, story);
    return $(storyTemplate(data)).data('story', story);
  };

  jobAdHtml = function(ad) {
    return storyHtml(ad);
  };

  commentHtml = function(comment) {
    var data;
    data = $.extend({
      id: '-1',
      comment: []
    }, comment);
    return $(commentTemplate(data));
  };

}).call(this);
