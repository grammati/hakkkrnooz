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
    .on 'keydown', 'div.item', (e) ->
        fn = KmapVim(e.which)
        if fn
            fn $(e.target)
            e.preventDefault()

focusNext = (e) ->
    e.next()?.focus()

focusPrev = (e) ->
    e.prev()?.focus()

showStories = () ->
    div = $('#stories')
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

showComments = (story) ->
    id = story?.attr('id')
    return if not /^\d+$/.exec(id)
    div = $('#comments')
    div.empty()
    div.addClass('loading')
    $.getJSON "/comments/" + id, (comments) ->
        div.removeClass('loading')
        for c in comments
            e = htmlFor(c)
            e.attr('parentid', id)
            div.append e
        story.addClass('active-parent')
        $('.comment:first-child', div).focus()

showReplies = (comment) ->
    $('.comment-children', $(comment).parent()).hide()
    $('.comment-children', comment) .show()

getParent = (item) ->
    parentid = item.attr('parentid')
    $('#' + parentid) if parentid

upToParent = (item) ->
    return if item.attr('type') != 'comment'
    # Remove this level of comments by clearing its DOM parent.
    item.parent()?.empty()
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

# TODO - get a template engine - this is messy

storyHtml = (story) ->
    $ '<div/>',
        id: story.id
        class: 'item story'
        tabindex: 1
    .append(
        $ '<a/>',
            href: story.href
            class: 'story-link'
        .text story.title
    ).append(
        $ '<span/>',
            class: 'cc'
        .text story.cc
    ).data('story', story)

jobAdHtml = (ad) ->
    storyHtml(ad)

commentHtml = (comment) ->
    replies = $ '<div/>',
        class: 'comment-children'
    for reply in comment.replies
        replies.append commentHtml(reply)

    $ '<div/>',
        id: comment.id
        class: 'item comment'
        tabindex: 1
    .append(
        $ '<div/>',
            class: 'comment-text'
        .html(comment.comment.join('\n'))
    ).append(
        replies
    )
