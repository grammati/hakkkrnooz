# hakkkrnooz.coffee

_.templateSettings = {
  interpolate : /\{\{(.+?)\}\}/g
}

storyTemplate = _.template($('#story-template').text())
commentTemplate = _.template($('#comment-template').text())

columnWidth = 550;

$ () ->
    showStories()
    initEvents()

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

Kmap = (key) ->
    switch key
        when K.Enter then openCurrent
        when K.Right then showChildren
        when K.Left then upToParent
        when K.Down then focusNext
        when K.Up then focusPrev

KmapVim = (key) ->
    switch key
        when K.L then showChildren
        when K.H then upToParent
        when K.J then focusNext
        when K.K then focusPrev
        else Kmap(key)

initEvents = () ->
    $(document)
    .on 'keydown', 'div.item', (evt) ->
        return if evt.ctrlKey or evt.altKey or evt.shiftKey
        fn = KmapVim(evt.which)
        if fn
            fn $(evt.target)
            evt.preventDefault()
    .on 'focus', 'div.item', (evt) ->
        positionItem(evt.target)

focusNext = (elt) ->
    elt.next()?.focus()

focusPrev = (elt) ->
    elt.prev()?.focus()

positionItem = (elt) ->
    elt = $(elt)
    pos = elt.position()
    column = $(elt.parent())
    targetTop = 50 - pos.top
    column.animate({'top': targetTop})


showStories = () ->
    div = $('#stories')
    div.css('width', columnWidth)
    div.attr('data-column-number', 1)
    div.addClass('loading')
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
                        #.css('width', 0)
                        .attr('data-column-number', 1 + ncols)
    $('#content').append(newCol)
    newCol

removeLastColumn = () ->
    cols = $('#content > div.column')
    if cols.length > 1
        cols.last().remove()

# yeah, it's global variable - but it's OK, it's coffeescript :)
commentCache = {}

showComments = (story) ->
    id = story?.attr('id')
    return if not /^\d+$/.exec(id)
    div = addColumn()
    div.addClass('loading')
    # todo - don't reload every time?
    $.getJSON "/comments/" + id, (comments) ->
        div.removeClass('loading')
        appendComments(comments, div, id)
        story.addClass('active-parent')
        $('.comment:first-child', div).focus()
        scrollH()

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

appendComments = (comments, div, parentid) ->
    for c in comments
        commentCache[c.id] = c
        e = htmlFor(c)
        e.attr('parentid', parentid)
        div.append e
    div.fadeIn('fast')

scrollH = () ->
    overWidth = $(document).width() - $(window).width()
    window.scroll(Math.max(0, overWidth), window.scrollY)

getParent = (item) ->
    parentid = item.attr('parentid')
    $('#' + parentid) if parentid

upToParent = (item) ->
    return if item.attr('type') != 'comment'
    # Remove this level of comments by clearing its DOM parent.
    item.parent()?.remove()
    # Get the parent comment or story (not DOM parent, as above - confusing, I know)
    parent = getParent(item)
    if parent
        parent.focus()
        parent.removeClass('active-parent')

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
