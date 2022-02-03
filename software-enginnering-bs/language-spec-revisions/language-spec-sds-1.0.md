# Introduction

## Purpose

This is a specification for the technical aspects of how the language works, this will extend on the concepts covered in the `overview-srs.md` file. This document will go over the technical aspects of how the language should be implemented and the reqirements for interoperability.

## Intended Audience

The readers of this document should be those who plan on creating a program in order to interpret, compile, execute, or edit the program files defined later in this document.

## Intended Use

The files will be output from programs into a form that is sharable by the end user, as specified in the overview.

## Scope

This will be definitions of the storage medium and builtin functions for the system.

## Definitions and Acronyms

//todo

# Design

## File storage

The file will be stored in XML, templating out the actual contents of the file: 
```xml
<custom-blocks>
    <custom name="blockname">
    <!-- code -->
    </custom>
</custom-blocks>
<main>
    <!-- code -->
</main>
```

## layout

code blocks will contain a list of blocks and a list of wires contained. both with their absolute positions on a canvas, the blocks should reference the wires they are connected to and vis-versa in arguments.

example using pseudo-blocks:

```xml
<main>
<wires>
    <wire wireid=0>
        <start x=0 y=0 block=0 />
        <segment x=.5 y=.5 />
        <end x=1 y=1 block=2 />
    </wire>
</wires>
<blocks>
    <globalinput blockid=0 name="a" out=0 x=-2 y=3 />
    <globalinput blocid=1 name="" x=-2  y= 0 />
    <blocka blockid=2 x=0 y=0 inp1=0 out2=2 />
</blocks>
</main>
```

main also counts as a code block as it can have inputs and outputs.
a value of -1 (or no value) means an input is not connected.

### wire branching

Wires should be capable of branching in order to connect one output to multiple inputs (multi-input will be unsupported and should throw an error, except through some kind of "mux" block to run each synchronously, this will be described more later). In order to support that, wires can specify a branch block in them which will act as a normal segment block, except it will have contents that end with an end block.

example using pseudo-blocks:

```xml
    <wire wireid=0>
        <start x=0 y=0 block=0 />
        <branch x=.5 y=.5>
            <end x=0 y=1 block=3 />
        </brach>
        <end x=1 y=1 block=2 />
    </wire>
```

### grouping without creating a codeblock

there can be artificial grouping by creating a group block around a group of blocks in main, there are a 2 types of groups, named groups and commented out groups

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

In order to run, the code takes all the independent "webs" traverses them to the inputs and runs them through. the order in which each web is run to eachother is not defined and for all intents and purposes should be treated as asynchronous, in order to "combine" independent webs with timing requirements, there will be a block that combines two or more nets to specifically run after eachother

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



# Blocks

## normal blocks

### primitive operations

### suppliers, functions, etc

## "hollow" blocks

### flow control

## 