---
layout: initializr
title: Documentation Guidelines
---

# {{ page.title }}

`git clone -b gh-pages --single-branch git@github.com:alex-rind/TimeBench.git TimeBench-Docs.git`

Kate provides Syntax highlighting.

There is also a Notepad++ extension: <http://superuser.com/questions/586177/how-to-use-markdown-in-notepad/586181#586181>

### Stylesheets

SASS support is not in the Jekyll version currently deployed at GitHub pages.
Therefore we pre-compile it locally:

~~~ bash
sass main.scss  > main.css

sass --watch main.scss:main.css
~~~
