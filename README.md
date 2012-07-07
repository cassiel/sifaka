# Sifaka: a Clojure-based template builder for Lemur

Sifaka is a package for constructing [Liine Lemur][liine] templates,
and uploading them
to iOS devices, purely in Clojure, avoiding the rather quirky WYSIWYG
template editor.

The code is at an early stage; we're just testing with `Container`
objects at the moment, as we shake out some details of the XML
generation. This is likely to be a slow process, as the object
parameters are numerous and subtle.

Thanks to Matthew Stanton, whose Python code [here][stanton] pointed the
way to uploading templates via TCP/IP. (We figured out the rest, and
corrected some errors, by sniffing network packets from the editor.) No
thanks to JazzMutant whose "OSC-style" example code is confusing,
misdocumented and just plain wrong.

## Usage

Sifaka is a [Leiningen 2][lein] project, so build in the usual
manner. There are one or two upload examples at the bottom of
`scratch.clj`.

## Technical Notes

- The Lemur editor doesn't allow containers with a dimension less than 25.

- Containers appear to have internal padding: an internal offset of
  `[0 0]` results in some space around the contained object.

[liine]: http://liine.net
[stanton]: http://music.trontronic.com/lemur/lemur_loader.py
[lein]: https://github.com/technomancy/leiningen/
