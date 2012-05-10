# hakkkrnooz.coffee

_.templateSettings = {
  interpolate : /\{\{(.+?)\}\}/g
}

storyTemplate = _.template($('#story-template').text())
commentTemplate = _.template($('#comment-template').text())

columnWidth = 550;

$ () ->
    initEvents()
    initStyles()
    showStories()

initStyles = () ->
    $('body').addClass($.cookie('theme') or 'whitey')
    columnWidth = $.cookie('column-width') or 550

# Keyboard event handling
K =
    Enter: 13
    Esc:   27
    Space: 32
    Left:  37
    Up:    38
    Right: 39
    Down:  40
    H:     72
    J:     74
    K:     75
    L:     76

keyMap = (key) ->
    switch key
        when K.Enter
            openCurrent
        when K.Right, K.L
            showChildren
        when K.Left, K.H
            upToParent
        when K.Down, K.J
            focusNext
        when K.Up, K.K
            focusPrev

initEvents = () ->
    $(document)
    .on 'keydown', 'div.item', (evt) ->
        return if evt.ctrlKey or evt.altKey or evt.shiftKey
        fn = keyMap(evt.which)
        if fn
            fn $(evt.target)
            evt.preventDefault()
    .on 'focus', 'div.item', (evt) ->
        onItemFocus(evt.target)

focusNext = (elt) ->
    elt.next()?.focus()

focusPrev = (elt) ->
    elt.prev()?.focus()

onItemFocus = (elt) ->
    scrollColumn(elt)
    removeFollowingColumns(elt)

scrollColumn = (elt) ->
    # Slide the column containing the given element up or down so that
    # the element where we want it.
    elt = $(elt)
    pos = elt.position()
    column = $(elt.parent())
    isFirst = elt.prev().size() == 0
    targetTop = (isFirst ? 0 : 25) - pos.top
    column.stop().animate({'top': targetTop}, 'fast')


showStories = () ->
    div = $('#stories')
    div.css('width', columnWidth)
        .attr('data-column-number', 1)
        .addClass('loading')
    $.getJSON "/stories", (stories) ->
        div.removeClass('loading')
        for story in stories
            div.append htmlFor(story)
        $('.story:first-child', div).focus()

openCurrent = (item) ->
    switch item?.attr('type')
        when 'story' then window.open(item.data('story').href)
        when 'comment' then expandComment

showChildren = (item) ->
    switch item?.attr('type')
        when 'story' then showComments(item)
        when 'comment' then showReplies(item)

addColumn = () ->
    columns = $('#content > div.column')
    ncols = columns.length
    newCol = $('<div/>').addClass('column')
                        .css('left', ncols * columnWidth)
                        .attr('data-column-number', 1 + ncols)
    $('#content').append(newCol)
    newCol

removeFollowingColumns = (elt) ->
    $(elt).parent().nextAll('.column')
        .stop()
        .animate {width: 0}, 'fast', () -> $(this).remove()

# yeah, it's global variable - but it's OK, it's coffeescript :)
commentCache = {}

scrollH = () ->
    # Scroll all the way to the right
    overWidth = $(document).width() - $(window).width()
    $('body').animate({scrollLeft: Math.max(0, overWidth)}, 'fast')

# Keep track of which story's comments we are loading
loading = null



showComments = (story) ->
    id = story?.attr('id')
    return if not /^\d+$/.exec(id)
    return if loading == id # already loading it
    loading = id
    div = addColumn()
    div.addClass('loading')
    # todo - don't reload every time?
    $.ajax
        url: "/comments/" + id,
        dataType: 'json',
        success: (comments) ->
            return if loading != id
            div.removeClass('loading')
            appendComments(comments, div, id)
            story.addClass('active-parent')
            $('.comment:first-child', div).focus()
            scrollH()
        error: () ->
            loading = null if loading == id

showReplies = (commentDiv) ->
    id = $(commentDiv).attr('id')
    comment = commentCache[id]
    replies = comment?.replies
    if not comment or replies.length == 0
        return
    div = addColumn()
    appendComments(replies, div, id)
    commentDiv.addClass('active-parent')
    $('.comment:first-child', div).focus()
    scrollH()
    div.animate({width: ['0px', columnWidth + 'px']}, 'fast');

appendComments = (comments, div, parentid) ->
    for c in comments
        commentCache[c.id] = c
        e = htmlFor(c)
        e.attr('parentid', parentid)
        div.append e

getParent = (item) ->
    parentid = item.attr('parentid')
    $('#' + parentid) if parentid

upToParent = (item) ->
    return if item.attr('type') != 'comment'
    # Get the parent comment or story (not DOM parent)
    parent = getParent(item)
    if parent
        parent.removeClass('active-parent')
        parent.focus()

htmlFor = (obj) ->
    e = switch obj?.type
        when 'story' then storyHtml(obj)
        when 'job-ad' then jobAdHtml(obj)
        when 'comment' then commentHtml(obj)
        else $('<div/>').html('??????')
    e.attr('type', obj.type)
    e

# Templates

storyHtml = (story) ->
    data = $.extend({id: '-1', href: '#', title: '???', cc: 0}, story)
    return $(storyTemplate({s: data})).data('story', story)

jobAdHtml = (ad) ->
    storyHtml(ad)

commentHtml = (comment) ->
    data = $.extend({id: '-1', user: '', replies: [], comment: ''}, comment)
    return $(commentTemplate({c: data}))
