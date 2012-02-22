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
    .on 'keydown', 'div.story', (e) ->
        story = $(e.target)
        id = story?.attr 'id'
        switch e.which
            when K.Enter, K.Right then showComments(id) if id
            when K.Down
                e.preventDefault()
                story.next()?.focus()
            when K.Up
                e.preventDefault()
                story.prev()?.focus()


showStories = (stories) ->
    $.getJSON "/stories", (stories) ->
        div = $('#stories')
        for story in stories
            div.append htmlFor(story)

showComments = (id) ->
    return if not /^\d+$/.exec(id)
    div = $('#comments')
    div.empty()
    $.getJSON "/comments/" + id, (comments) ->
        for c in comments
            div.append htmlFor(c)


htmlFor = (obj) ->
    switch obj?.type
        when 'story' then storyHtml(obj)
        when 'comment' then commentHtml(obj)
        else $('<div/>').html('??????')

# TODO - get a temlate engine - this is messy

storyHtml = (story) ->
    $ '<div/>',
        id: story.id
        class: 'story'
        tabindex: 1
    .append(
        $ '<a/>',
            href: story.href
        .text story.title
    ).append(
        $ '<span/>',
            class: 'cc'
        .text story.cc
    )

commentHtml = (comment) ->
    $ '<div/>',
        id: comment.id
        class: 'comment'
        tabindex: 1
    .html(comment.comment.join('\n'))

