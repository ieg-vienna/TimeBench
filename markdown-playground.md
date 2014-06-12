---
layout: tactile
title: Playground with Markdown
---

Markdown
--------

[General syntax introduction](http://daringfireball.net/projects/markdown/syntax)

__bold__ or **bold**

_italic_ or *italic*

___bold italic___ or ***bold italic***

inline `code` in text

<http://example.com> URL with angle brackets

* bullet list
* bullet list

1. ordered list
1. ordered list

Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis
nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore
eu fugiat nulla pariatur.
Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia
deserunt mollit anim id est laborum.

<table>
    <tr>
        <td>custom</td>
        <td>HTML</td>
    </tr>
    <tr>
        <td>is</td>
        <td>possible</td>
    </tr>
</table>

"quotation marks" and dashes -- are converted

Handle special characters like in St. Pölten.

## GitHub Flavor Markdown

We aim to be largely compatible with [GFM](https://help.github.com/articles/github-flavored-markdown).

ignoring underscore_in_name (RedCarpet extension `no_intra_emphasis`; by default in Kramdown)

~~Mistaken text.~~ (RedCarpet extension `strikethrough`; not in Kramdown)

URL autolinking http://example.com  (RedCarpet extension `autolink`; not in Kramdown)

GitHub flavored fenced code blocks (triple backticks)

```java
        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
            }
        });
```

| Left-Aligned  | Center Aligned  | Right Aligned |
| :------------ |:---------------:| -----:|
| col 2 is      | centered        | $1600 |
| zebra stripes | are neat        |    $1 |
|===== |===== |===== |
| Foot 1 | F2 | F3 |

## RedCarpet extensions to Markdown

_underlined_ text (RedCarpet extension `underline` -- collides with italics)

## Kramdown extensions to Markdown

[Quick Syntax](http://kramdown.gettalong.org/quickref.html) or [Long Syntax](http://kramdown.gettalong.org/syntax.html)

~~~ java
        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
            }
        });
~~~

definition term
: definition

| Head 1  | Head 2
| :------------ | -----:
| zebra stripes | are neat
|=====
| Foot 1 | Foot 2
