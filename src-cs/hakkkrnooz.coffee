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
        when K.Enter, K.Right then showChildren
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
    $.getJSON "/stories", (stories) ->
        div = $('#stories')
        for story in stories
            div.append htmlFor(story)
        $('.story:first-child', div).focus()

showChildren = (item) ->
    switch item?.attr('type')
        when 'story' then showComments(item)
        when 'comment' then showReplies(item)

showComments = (story) ->
    id = story?.attr('id')
    return if not /^\d+$/.exec(id)
    div = $('#comments')
    div.empty()
    $.getJSON "/comments/" + id, (comments) ->
        for c in comments
            e = htmlFor(c)
            e.attr('parentid', id)
            div.append e
        $('.comment:first-child', div).focus()

showReplies = (comment) ->
    "FIXME"

getParent = (item) ->
    parentid = item.attr('parentid')
    $('#' + parentid) if parentid

upToParent = (item) ->
    getParent(item)?.focus()

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
    )

jobAdHtml = (ad) ->
    storyHtml(ad)

commentHtml = (comment) ->
    $ '<div/>',
        id: comment.id
        class: 'item comment'
        tabindex: 1
    .append(
        $ '<div/>',
            class: 'comment-text'
        .html(comment.comment.join('\n'))
    ).append(
        $ '<div/>',
            class: 'comment-children'
    )
