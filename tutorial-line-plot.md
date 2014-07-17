---
# layout: tactile
layout: initializr
title: Line Plot
---

TimeBench Tutorial: Line Plot
--------

With this tutorial we want to show the potential of TimeBench as a visualization development platform. Since TimeBench is based off Prefuse our tutorial shows certain parallels to Prefuse code.

### Introduction

Developing with prefuse is for the most part configuration. You can achieve both basic and advanced visualizations simply by creating objects and setting their properties. However, this makes it very easy produce source code that is hard to understand.

We recommend structuring your TimeBench code similar to this:

* data setup
* renderers
* action lists
* displays and controls

Contrary to working with Prefuse, TimeBench can process temporal data objects.

### Step 0:
This demo utilizes a factory that generates random numeric values for random time instants.

```
public static TemporalDataset generateRandomNumericalInstantData(int count,
        String dataColumn) throws TemporalDataException {
final long MIN_GAP_MS = 360000 * 24; // 1 day
final long MAX_GAP_MS = 360000 * 24 * 4; // 4 days
final double MIN_VALUE = 0.0d;
final double VALUE_RANGE = 100.0d;

TemporalDataset tmpds = new TemporalDataset();
tmpds.addDataColumn(dataColumn, double.class, 0.0d);

double value = Math.random() * VALUE_RANGE + MIN_VALUE;
long time = System.currentTimeMillis();
Calendar cal = JavaDateCalendarManager.getSingleton().getDefaultCalendar();
TemporalElement elem;
TemporalObject obj;

for (int i = 0; i < count; i++) {
    elem = tmpds.addInstant(time, time,
            cal.getBottomGranularity());
    obj = tmpds.addTemporalObject(elem);
    obj.set(dataColumn, value);
    // auto-regressive with order 1
    value = (Math.random() * VALUE_RANGE + MIN_VALUE) * 0.2d + value * 0.8d;
    time += Math.round(Math.random() * (MAX_GAP_MS - MIN_GAP_MS)
            + MIN_VALUE);
}

return tmpds;
}
```

We receive the central data element for visualizing time-oriented data: `TemporalDataset`s

Similar to Prefuse we require `Visualization` and a display, represented by `TimeAxisDisplay`

```
final Visualization vis = new Visualization();
final TimeAxisDisplay display = new TimeAxisDisplay(vis);
// display width must be set before the time scale
// otherwise the initial layout does not match the display width
display.setSize(700, 450);
```

### Step 1: Preparing Data and Time Scale

The time scale needs to be generated based upon the infimum and supremum of the data set, which are actually its earliest and the latest entries, including a buffer. We calculate a time scale, an overview time scale and a range adapter, necessary for managing current scale versus overview scale level.

```
VisualTable vt = vis.addTable(GROUP_DATA,
  tmpds.getTemporalObjectTable());
vt.addColumn(COL_LABEL, new LabelExpression());


long border = (tmpds.getSup() - tmpds.getInf()) / 20;
final AdvancedTimeScale timeScale = new AdvancedTimeScale(
    tmpds.getInf() - border, tmpds.getSup() + border,
    display.getWidth() - 1);
AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
RangeAdapter rangeAdapter = new RangeAdapter(overviewTimeScale,
    timeScale);

timeScale.setAdjustDateRangeOnResize(true);
timeScale.addChangeListener(new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
      vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
    }
});
```

### Step 2: Set up renderers

```
// STEP 2: set up renderers for the visual data
ShapeRenderer dotRenderer = new ShapeRenderer(8);
DefaultRendererFactory rf = new DefaultRendererFactory(dotRenderer);
rf.add(new InGroupPredicate(GROUP_LINES), new LineRenderer());
rf.add(new InGroupPredicate(GROUP_AXIS_LABELS), new AxisRenderer(
    Constants.FAR_LEFT, Constants.CENTER));
vis.setRendererFactory(rf);
```

### Step 3: Process data
<!--
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

GitHub flavored fenced code blocks (triple tilde, RedCarpet or Kramdown)

~~~ java
timeScale.setAdjustDateRangeOnResize(true);
timeScale.addChangeListener(new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
  vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
    }
});
~~~

GitHub flavored fenced code blocks (triple backticks -- not in Kramdown)

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

## RedCarpet extensions to Markdown

[Extensions](https://github.com/vmg/redcarpet/blob/v2.3.0/README.markdown#and-its-like-really-simple-to-use ) available in v. 2.3.0

_underlined_ text (RedCarpet extension `underline` -- collides with italics)

## Kramdown extensions to Markdown

[Quick Syntax](http://kramdown.gettalong.org/quickref.html) or [Long Syntax](http://kramdown.gettalong.org/syntax.html)

definition term
: definition

| Head 1  | Head 2
| :------------ | -----:
| zebra stripes | are neat
|=====
| Foot 1 | Foot 2

Kramdown seems to better support autogenerated semantic anchors for headers.

Kramdown supports user specified anchors.
-->
