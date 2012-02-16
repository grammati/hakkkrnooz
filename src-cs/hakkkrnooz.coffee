$ () ->
    hookup()
    showStories()

hookup = () ->
    $(document)
    .on 'keypress', 'div.story', (e) ->
        $('#comments').html(e.target.id)

showStories = (stories) ->
    $.getJSON "/stories", (stories) ->
        for story in stories
            $('#stories').append htmlFor(story)

htmlFor = (story) ->
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

