---
layout: initializr
title: Documentation How To
---

# {{ page.title }}

This website hosts the documentation of [TimeBench](https://github.com/ieg-vienna/TimeBench) 
and related software libraries. 

It is served by [GitHub Pages](https://help.github.com/articles/what-are-github-pages) 
and pre-generated using [Jekyll](https://help.github.com/articles/using-jekyll-with-pages).

Thus, you can find and edit markdown source of the website in the 
[gh-pages](https://github.com/ieg-vienna/TimeBench/tree/gh-pages) branch of the project.

## Getting Started

1. Clone just the _gh-pages_ branch to your computer.

    Better keep the documentation separate from the source code.

    `git clone -b gh-pages --single-branch git@github.com:ieg-vienna/TimeBench.git TimeBench-Docs.git`


2. Check that you have [Ruby](https://www.ruby-lang.org/en/) installed 
    or install it using your system's software package management (e.g., `aptitude`).

3. Install Jekyll and other dependencies.

    Best use the _bundler_ package manager: `gem install bundler`

    Change to the working directory and run: `bundle install` 

4. Test the website locally.

    Run in the working directory: `jekyll serve --watch`

    Now the website will be generate in the subdirectory `_site` 
    and you can test it under <http://127.0.0.1:4000/TimeBench/>.

    As long as `jekyll serve --watch` runs, it will react on changes and update the website.

5. Edit page content.
    
    Open `*.md` files in your favorite text editor. 
    For example, Kate provides syntax highlighting out-of-the-box.
    There is also a [Notepad++ extension](http://superuser.com/questions/586177/how-to-use-markdown-in-notepad/586181#586181).

    Please consult some [links and examples of Markdown](markdown-playground.html). 
    The Markdown is largely compatible with the [comments etc. in GitHub](https://help.github.com/articles/github-flavored-markdown).

    __Important:__ Write the markdown files as UTF-8 without BOM. 
    Best set a fixed indent with of 4 spaces, do not mix tabs and space. 

    You can preview and test your changes using `jekyll serve --watch` and <http://127.0.0.1:4000/TimeBench/>.

6. Commit and push changes.

    Use `git commit` and `git push` to submit your tested changes to GitHub.

    Within a short time the changes will be visible here.

    If you are not a member of the core developers you can send your changes as a pull request.


### Stylesheets

The main stylesheet is the `main.scss`.
SASS support is not in the Jekyll version currently deployed at GitHub pages.
Therefore we pre-compile it locally:

~~~ bash
sass main.scss  > main.css
~~~
or

~~~ bash
sass --watch main.scss:main.css
~~~

### Credits

- Editor & administration: Alexander Rind
- Responsive layout based on [initializr-template](https://github.com/verekia/initializr-template) by [@verekia](http://twitter.com/#!/verekia).
- Some icons by [Jason Long](https://twitter.com/jasonlong).
