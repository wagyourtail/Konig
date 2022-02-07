# Introduction

## Purpose

This is a specification for the technical aspects of how the language works, 
this will extend on the concepts covered in the `overview-srs.md` file. 
This document will go over the technical aspects of how the language should be implemented and the 
reqirements for interoperability.

## Intended Audience

The readers of this document should be those who plan on creating a program in order to interpret, 
compile, execute, or edit the program files defined later in this document.

## Intended Use

The files will be output from programs into a form that is sharable by the end user, 
as specified in the overview.

## Scope

This will be definitions of the storage medium and builtin functions for the system.

## Definitions and Acronyms

//todo

# Design

## File storage

The file will be stored in XML, templating out the actual contents of the file: 
```xml
<code name="">
    <custom-blocks>
        <custom name="blockname">
        <!-- code -->
        </custom>
    </custom-blocks>
    <main>
        <!-- code -->
    </main>
</code>
```

## layout

code blocks will contain a list of blocks and a list of wires contained. both with their absolute 
positions on a canvas, the blocks should reference the wires they are connected to and vis-versa in arguments.

example using pseudo-blocks:
THIS IS NOT THE FULL SPEC AND SHOULD NOT BE USED AS A REFERENCE FOR SUCH
```xml
<main>
    <wires>
        <wire wireid="0">
            <start x="0" y="0" block="0" />
            <segment x=".5" y=".5" />
            <end x="1" y="1" block="2" />
        </wire>
    </wires>
    <blocks>
        <globalinput blockid="0" name="a" out="0" x="-2" y="3" />
        <globalinput blocid="1" name="" x="-2"  y="0" />
        <blocka blockid="2" x="0" y="0" inp1="0" out2="2" />
    </blocks>
</main>
```

main also counts as a code block as it can have inputs and outputs.
a value of -1 (or no value) means an input is not connected.

### wire branching

Wires should be capable of branching in order to connect one output to multiple inputs 
(multi-input will be unsupported and should throw an error, except through some kind of "mux" block to run 
each synchronously, this will be described more later). In order to support that, wires can specify a branch 
block in them which will act as a normal segment block, except it will have contents that end with an end block.

example using pseudo-blocks:

```xml
    <wire wireid="0">
        <start x="0" y="0" block="0" />
        <branch x=".5" y=".5">
            <end x="0" y="1" block="3" />
        </branch>
        <end x="1" y="1" block="2" />
    </wire>
```

### grouping without creating a codeblock

there can be artificial grouping by creating a group block around a group of blocks in main, 
there are 2 types of groups, named groups and commented out groups

```xml
    <commented>
        <blocka ...>
    </commented>
    <group name="test">
        <blocka ...>
    </group>
```

### commenting

As specified in the previous section. commented groups will not run (of course)

## How does it run

In order to run, the code takes all the independent "webs" traverses them to the inputs and runs them through. The order in which each web is run to eachother is not defined and for all intents and purposes should be treated as asynchronous, in order to "combine" independent webs with timing requirements. For actual web running itself, the order of the sub-web for each input to a block is not defined, it will be only run once but if there are multiple independent sub-webs inputting into a block, the order they run is not defined and can be parallel.

There will be a block that combines two or more nets to specifically run after eachother.

```xml
<main>
    <sync>
        <group>
        </group>
        <group>
        </group>
    </sync>
</main>
```

# Block specification

## block type headers

blocks can be optionally be defined in a `<headers>` tag or file, 
this is also the spec for how custom blocks are defined, except there is a bit more too them that will be
explained in a further section. 
this will allow for editors to not need to know all the block types without actually having access to 
the runtime environment. for these headers the blocks will be defined as such:

```xml
<headers>
    <blockid name="">
        <io>
            <input side="top" justify="center" name="name" type="boolean" optional="true" />
            <output side="bottom" justify="center" name="name" type="int"/>
        </io>
        <image src="path/to/img.png" />
    </block>
</headers>
```

where the blockid tag is named by how the block should be called later.

### io

the inputs and outputs tags are optional, and contain info about where the inputs and outputs are.
there are 12 sections for the inputs and outputs, each side with left, center and right alignment.
there is also an optional <skip> tag which will leave a blank space with the same width as an input.

### image

the image tag is optional, and contains the path to the image to be used for the block, otherwise the editor will 
do something else for rendering, most likely the name will be displayed.

### hollow

there is also an optional tag `<hollow name="">` which corresponds to making this block hollow and having code
specified by `<innercode>` as described below.


### code

if this block is a custom block, then the code tag is required, it's contents will be the same as an overall file's context,
as in a set of wires, in that case the inner `globalinput` and `globaloutput` tags correspond *only* to 
the inputs and outputs of the block itself.

## block referencing

blocks are then referenced in a way that is (incorrectly) shown above, the real way is like:
```xml

    <block blockid="0" x="0" y="0" mirrored="false" flipped="false">
        <io>
            <input name="a" wireid="" />
            <output name="b" wireid="0" />
        </io>
    </block>

```

if a block is hollow, it will also contain a section inside of it called `<innercode name="">`
which will contain the code that is inside the block, this will be explained in a bit more detail below.

# Blocks

this section will cover the specifics of what the block definition looks like for each type of block,
as well as specific rendering guidelines.

## general

The basics of how blocks generally looks is defined above, this section will be the *general* rendering guidelines that will be
expanded on in the more specific sections below.

## normal blocks

### primitive operations

#### const blocks

### suppliers, functions, etc

## "hollow" blocks

### flow control

## 