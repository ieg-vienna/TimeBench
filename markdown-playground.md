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

<table>
    <tr>
        <td>some</td>
        <td>HTML</td>
    </tr>
    <tr>
        <td>code</td>
        <td>is possible</td>
    </tr>
</table>

## GitHub Flavor Markdown

We aim to be largely compatible with [GFM](https://help.github.com/articles/github-flavored-markdown).

ignoring underscore_in_name

~~Mistaken text.~~ NOT SUPPORTED YET

URL autolinking http://example.com NOT SUPPORTED YET

```java
        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
            }
        });
```

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

