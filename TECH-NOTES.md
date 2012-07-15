# Technical Notes

## General

## Specific Objects

### Container

- The Lemur editor doesn't allow containers with a dimension less than 25.

- Containers appear to have internal padding: an internal offset of
  `[0 0]` results in some space around the contained object.

### Pads

- There's no clear documentation for the properties `Multilabel` and
  `Multicolor` - I'm guessing these are scriptable/OSC controllable
  only. For now, they're always off.
