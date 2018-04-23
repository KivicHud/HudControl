# How to change boot logo in Kivic HUD?

## Prepare boot logo images

Make sure that the resolution of the boot logo images is 480x240.

## Install Boot Animation Creator

Install [Boot animation creator](https://docs.google.com/file/d/0B_TsLfIpuP_ac3NVUjltb1d5MUE/edit) for android device. You may need to install the .NET Framework to run boot animation creator. Please refer to the [Creator site](https://forum.xda-developers.com/showthread.php?t=1234611) to create a boot animation. Keep in mind that Kivic HUD doesn't support audio for boot animation.

* Set number of loops to 0, otherwise boot animation will stop after n loop. Valid formats are as follows.
```
    480 240 22
    p 0 0 part
```


## Install HudBootAnimation.exe

* Copy KivicHudDisplay folder to anywhere.
* Unplug Kivic HUD from your computer.
* Install USB driver.

## Apply boot logo via HudBootAnimation

Please note that the System version of Kivic HUD is less than 2.1.0, you need to update HUD firmware.

* Plug Kivic hud into your computer.
* Run HudBootAnimation.exe and wait until USB device detected.
* Click down arrow button to apply logo image.
* Select a bootanimation.zip for hudway.
