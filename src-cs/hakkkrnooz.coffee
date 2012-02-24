$ () ->
    hookup()
    showStories()

K =
    Enter: 13
    Esc:   27
    Space: 32
    Left:  37
    Up:    38
    Right: 39
    Down:  40

hookup = () ->
    $(document)
    .on 'keydown', 'div.item', (e) ->
        div = $(e.target)
        item = div.attr 'data'
        switch e.which
            when K.Enter, K.Right
                e.preventDefault()
                showChildren(item)
            when K.Left
                e.preventDefault()
                focusParent(item)
            when K.Down
                e.preventDefault()
                div.next()?.focus()
            when K.Up
                e.preventDefault()
                div.prev()?.focus()


showStories = (stories) ->
    $.getJSON "/stories", (stories) ->
        div = $('#stories')
        for story in stories
            div.append htmlFor(story)
        $('.story:first-child', div).focus()

showChildren = (item) ->
    switch item?.type
        when 'story' then showStories(item)
        when 'comment' then showReplies(item)

showComments = (story) ->
    id = story?.id
    return if not /^\d+$/.exec(id)
    div = $('#comments')
    div.empty()
    $.getJSON "/comments/" + id, (comments) ->
        for c in comments
            c.parent = story
            div.append htmlFor(c)
        $('.comment:first-child', div).focus()

showReplies = (comment) ->
    "FIXME"

focusParent = (item) ->
    $('#' + item.id).focus()

htmlFor = (obj) ->
    e = switch obj?.type
        when 'story' then storyHtml(obj)
        when 'job-ad' then jobAdHtml(obj)
        when 'comment' then commentHtml(obj)
        else $('<div/>').html('??????')
    e.data = obj
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
