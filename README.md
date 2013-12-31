linkbasher
==========

An Android utility app that resolves shortened links faster than your browser.

Why?!
-----
Created out of frustration after far too many instances of clicking on a
shortened YouTube link in the Twitter app, which first launches Chrome (to
resolve the `t.co` link), and then launches the YouTube app. Even worse is when
one clicks on a `t.co` link to a tweet which opens the tweet in the mobile site
rather than my phone's Twitter app!

What?!
------
Here's an example of the current situation:

1. Click on `t.co` URL in Twitter app
2. Browser is launched with `t.co` URL
3. Browser carries on loading (it's a bit bloated)
4. Browser finally loads (panting)
5. Browser restores your previous session and tabs
5. Browser opens a new tab for the `t.co` URL
6. Browser follows `t.co` URL
7. Browser resolves `t.co` URL to `youtube.com` URL
8. Browser launches YouTube app (or maybe loads a tab with `m.youtube.com` if
   it's feeling mean)

But with Linkbasher:

1. Click on `t.co` URL in Twitter app
2. Linkbasher is launched with `t.co` URL
3. Linkbasher resolves ('expands') URL
4. Linkbasher launches appropriate app (e.g. YouTube)

Linkbasher is far quicker than opening your Browser just to resolve a
shortened link :D

How?!
-----
Linkbasher registers itself as a handler for some of the most-used
link shortening services, such as `t.co` (Twitter), `goo.gl` (Google), `bit.ly`,
`tinyurl.com` and `ow.ly`. When set as the default app for these domains, it 
is used to resolve these links to their expanded form, and then launches a new
Intent with the resolved URL.

If the shortened link returns anything other than a 3xx HTTP status code, the 
user is prompted to choose a different app from those that are available (as 
if they clicked on the link in the normal fashion).

Really?!
--------
Unfortunately, yes.
