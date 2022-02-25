# CapML

## This is an unnecessary markup language for android ui

Not much of a readme so far. Still kind of working out how I want to do this.

For the widgets in particular, I'm going to start with basics. Gathering data.

- Checkboxes -> CB
    - Always either true or false.
    - Text Attribute
- EditText -> ET
    - Empty or prefilled.
    - Text Attribute
- TextView -> TV
    - Always have text attribute
- Spinners -> SP
    - Number of ~~values~~
  
They also almost always relate to a data model. Maybe a callback in the class itself?
Lines are significant. tabs too.
```
# this will be a comment!
~WidgetType
+String, but like without quotes?
-
~SP
+a value
+another value
-
```

### Written by Dennis Capone