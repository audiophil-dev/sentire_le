# Documentation for the Sentire "lite" Software (sentire-le)

## Sentire

Sentire [sen'ti:re] is an [art project](sentire.me), a research project and furthermore, the name of a digital system that mediates between bodily movements and musical sounds. 

The technology can sense inter-body proximity and touch.

These physical parameters are mapped to an algorithmic _sound environment_ in real time, allowing for a _closed-loop auditory interaction_ between two or more people in a physical environment.

This site documents the installation and usage of the sentire "lite" software, a public version of the sentire software.

The intention is to make a basic version of sentire accessible to a broader public. With the software, an audio interface and two (modified) audio cables (see section Setting up sentire), you can try the interactive system. 

Even without using the hardware, the [software](/docs/index#sentire-software) (written in [SuperCollider](https://supercollider.github.io/)) can be a good starting point for working with algorithmic sound environments mapped to one-dimensional parameter spaces.

## Sentire Software

### Installation

The following steps will guide you through the installation of sentire. 

You will need a working version of [SuperCollider](https://supercollider.github.io/). If you have not installed it yet, please follow the instructions on the SuperCollider page.

Once you have successfully installed `SuperCollider`, you should either clone the [sentire repository](https://github.com/audiophil-dev/sentire_le) or download the latest release (TODO: add link) and unzip it on your computer.

In the following instructions replace `$yourSentireDirectory` with your local (just downloaded) sentire directory

#### __Step 1: Copy Sentire Extensions__

First you need to copy the folder `SentireExtensions` (`$yourSentireDirectory/Extensions`) to the `SuperCollider` Extensions folder.

On Linux this folder is usually located in 

```~/.local/share/SuperCollider/Extensions```

On MacOs it should be located in

```~/Library/Application\ Support/SuperCollider/Extensions```

#### __Step 2: Recompile SuperCollider Class Library__

In the SuperCollider IDE either navigate to `Language` and press `Recompile Class Library` or press `ctrl`+`shift`+`l` on Linux (on MacOs press `cmd`+`shift`+`l`).

The Class Library should compile without any errors.

#### __Step 3: Install JITLibExtensions

In SuperCollider execute

```Quarks.gui();```

and install the `JITLibExtensions`.

#### __Step 4: Start Sentire

Open `main.scd` in `$yourSentireDirectory` and execute the whole code inside the brackets.

A Window named "Sentire" should open, and no errors should be printed to the post window.













