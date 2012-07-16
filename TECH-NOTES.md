# Technical Notes

## General

- We need to generalise ADSRH (e.g. in Pads): these actually take
  expressions.

## Specific Objects

### Container

- The Lemur editor doesn't allow containers with a dimension less than 25.

- Containers appear to have internal padding: an internal offset of
  `[0 0]` results in some space around the contained object.

### Pads

- There's no clear documentation for the properties `Multilabel` and
  `Multicolor` - I'm guessing these are scriptable/OSC controllable
  only. For now, they're always off.

- Need to generalise ADSRH (see above).

- To-do: "Capture", "light". (Light can apparently be a vector or
  expression; we should investigate this.)
