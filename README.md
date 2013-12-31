linkbasher
==========

An Android utility app that resolves shortened links faster than your browser.

Why?!
-----
Created out of frustration after far too many instances of clicking on a
YouTube link in the Twitter app thinking it would load the YouTube app, and
instead being presented with my web browser.

The problem is that when you click on a `youtube.com` URL in the Twitter app, 
it actually launches an Intent with a shortened `t.co` URL rather than the 
`youtube.com` URL. So instead of the YouTube app launching, Chrome (or your 
chosen browser) does, adding an unnecessary and cumbersome additional step
between Twitter and your chosen cat video.

In an ideal world, the Twitter app would just launch the expanded URL directly.

(This isn't restricted to just the Android Twitter app - other apps which launch
shortened links sometimes manifest the same behaviour.)

What?!
------
Here's an example of the current situation:

1. Click on `youtube.com` URL in Twitter app
2. Browser is launched with `t.co` URL (wrapping the `youtube.com` URL)
3. Browser carries on loading (it's a bit bloated)
4. Browser finally loads (panting)
5. Browser restores your previous session and tabs
5. Browser opens a new tab for the `t.co` URL
6. Browser follows `t.co` URL
7. Browser resolves `t.co` URL to `youtube.com` URL
8. Browser launches YouTube app (or maybe loads a tab with `m.youtube.com` if
   it's feeling mean)

But with Linkbasher:

1. Click on `youtube.com` URL in Twitter app
2. Linkbasher is launched with `t.co` URL
3. Linkbasher follows `t.co` URL to get `youtube.com` URL
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

Drawbacks?!
-----------
Here are the obvious two:

1. You have to click 'Linkbasher', 'Always' each time you click on a new
   shortened link domain.
2. Linkbasher, unlike Chrome, doesn't compare URLs against Google's malware/
   safe browsing API when following links. This *shouldn't* be a problem if
   the app loading the resolved URL is secure, but it's worth being aware of.

What's with all the ?!'s?!
--------------------------
I got a great deal on them in the boxing day sales.
