$ () ->
    hookup
    showStories

showStories = (stories) ->
    $.getJSON "/stories", (stories) ->
        for story in stories
            $('#stories').append htmlFor(story)

htmlFor = (story) ->
    $ '<div/>',
        id: story.id
        class: 'story'
    .append(
        $ '<a/>',
            href: story.href
        .text story.title
    ).append(
        $ '<span/>',
            class: 'cc'
        .text story.cc
    )

