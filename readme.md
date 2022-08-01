# Installation

## Clone or download repository ##

Replace ```$yourSentireDirectory``` in the instructions with you local (just downloaded) sentire directory.

## Create a symbolic link in the supercollider Extensions folder

### Linux ###

Navigate to

```
cd ~/.local/share/SuperCollider/Extensions
```

### MacOs ###

```
cd ~/Library/Application\ Support/SuperCollider/Extensions
```

### Linux & MacOs ###

Create symbolic link to the sentire extensions

```
ln -s ~/$yourSentireDirectory/Extensions Sentire
```

## Install needed Quarks ##

In supercollider execute

```
Quarks.gui();
```

and install the following quarks:

* JITLibExtensions

Recompile the Class Library

# Run the tests

From the main project folder run:

`./Helper_Scripts/run_tests.sh`

Note that you need *jackd* to be running (or have a working `~/.jackdrc` file).

To run individual tests add the folder `Test/Extensions` to your sclang_conf.yml and try evaluating:

`TestSentireEnvelope.run` 

or

`TestSentireMain.run`

# GitHub Actions Runner

There is a Rapsberry Pi 4 configured as a self-hosted github actions runner.

To reconfigure it follow the instructions provided by github adding a new self-hosted actions runner for Linux ARM.

In the Raspberry Pi you should use the user `sentire` to install the new actions runner (run the `./config.sh` command provided as described in GitHub docs). You can test by running `./run.sh`.

To enable it to run as a service you can do, as your own user because `sentire` user has no access to `sudo`:  `sudo ./svc.sh install sentire`, this will add the actions runner to systemd and tell it to run as `sentire` user.

# Start Sentire

## Desktop Version

Execute ```main.scd``` in the root directory

## Mobile Version (Raspberry Pi 4)

Get the latest version of pi-gen modified for sentire:

https://github.com/Sentire-Dev/pi-gen

And follow the instructions

After booting the rpi4 web interface should be ready at `http://<raspberrypi ip>:8000/`

# OSC JavaScript Frontend #

To use the OSC JavaScript Frontend you will need https://github.com/bgola/ws2udp

Follow the install instructions in the linked repository.

To run the frontend, execute the ```start-frontend.sh``` script in ```$yourSentireDirectory/Helper_Scripts```:

```
./start-frontend.sh
```

The frontend will be available at `http://<your ip>:8000/`.
If you are running in your own machine that would be `http://localhost:8000`
