# Wireless Crafting Terminal (Beta)

[![CurseForge](http://cf.way2muchnoise.eu/full_wireless-crafting-terminal_downloads.svg)](https://minecraft.curseforge.com/projects/wireless-crafting-terminal)
[![Latest Version](http://cf.way2muchnoise.eu/versions/For%20MC_wireless-crafting-terminal_all.svg)](https://minecraft.curseforge.com/projects/wireless-crafting-terminal/files/latest)

## Table of Contents

* [About](#about)
* [Contact](#contact)
* [License](#license)
* [Downloads](#downloads)
* [Installation](#installation)
* [Issues](#issues)
* [Building](#building)
* [Contribution](#contribution)
* [API](#wireless-crafting-terminal-api)
* [Localization](#wireless-crafting-terminal-localization)
* [Credits](#credits)

## About

An OP addon to an already OP mod =]
Adds a Wireless Terminal version of the AE2 Crafting Terminal with the ability to install upgrades

Currently in BETA! I need testers! Drop me a line on [IRC](#contact) or [Twitter](#contact)

## Contact

* [Website](http://p455w0rd.net/mc/)
* [IRC #p455w0rdCraft on esper.net](http://webchat.esper.net/?channels=p455w0rdCraft&prompt=1)
* [GitHub](https://github.com/p455w0rd/WirelessCraftingTerminal)
* [Twitter](https://twitter.com/TheRealp455w0rd)

## License

I'm a huge fan of Open Source software as well as helping people learn.
As far as licensing, do what you want. Claim it as your own if you so wish. I don't care =D If you become popular/rich/get a hot wife due to claiming that you made my mod,
I'm just glad I could help make another person's life better in some way.

## Downloads

* Both the RV2 and RV3 versions are available on [CurseForge](http://minecraft.curseforge.com/projects/wireless-crafting-terminal)

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It requires that [AE2 RV3 Beta](http://ae-mod.info/Downloads/) (any build) also be installed.

## Issues/Feature Requests

* Post 'em in the [issues](https://github.com/p455w0rd/WirelessCraftingTerminal/issues) section. =D

Providing as many details as possible does help us to find and resolve the issue faster and also you getting a fixed version as fast as possible.

## Building

1. Clone this repository via 
  - SSH `git clone git@github.com:p455w0rd/WirelessCraftingTerminal.git` or 
  - HTTPS `git clone https://github.com/p455w0rd/WirelessCraftingTerminal.git`
2. Setup workspace 
  - Decompiled source `gradlew setupDecompWorkspace`
  - Obfuscated source `gradlew setupDevWorkspace`
  - CI server `gradlew setupCIWorkspace`
3. Build `gradlew build`. Jar will be in `build/libs`
4. For core developer: Setup IDE
  - IntelliJ: Import into IDE and execute `gradlew genIntellijRuns` afterwards
  - Eclipse: execute `gradlew eclipse`
5. For add-on developer: Core-Mod Detection
  - In order to have FML detect AE and WCT from your dev environment, add the following VM Arguments to your run profile
  - `-Dfml.coreMods.load=appeng.transformer.AppEngCore`
  - `-Dfml.coreMods.load=net.p455w0rd.wirelesscraftingterminal.transformer.WCTCore`

## Contribution

* Fork -> Edit -> PR

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [synching](https://help.github.com/articles/syncing-a-fork) if you plan to contribute on regular basis.

## Wireless Crafting Terminal API

* The API is very simple

# Wireless Crafting Terminal
To make your item a Wireless Crafting Terminal variant, register like normal with AE2 and implement
`net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem`

To open the WCT Gui from said item, use
`WCTApi.instance().openWirelessCraftingTerminalGui(EntityPlayer player);`

# Wireless Fluid Terminal
To make your item a Wireless Fluid Terminal variant, register like normal with AE2 and implement
`net.p455w0rd.wirelesscraftingterminal.api.IWirelessFluidTerminalItem`

To open the WCT Gui from said item, use
`WCTApi.instance().openWirelessFluidTerminalGui(EntityPlayer player);`

# WCT Configs
To check if the Infinity Booster Card is enabled in WCT configs, use
`WCTApi.instance().isInfinityBoosterCardEnabled()`

To check if the old infinite range mechanic option is enabled, use
`WCTApi.instance().isOldInfinityMechanicEnabled()`

### Maven

When compiling against the WCT API you can use gradle dependencies, just add

	repositories {
		maven {
			name = "covers Maven"
			url = "http://maven.covers1624.net"
		}
	}

    dependencies {
        compile "p455w0rd:WirelessCraftingTerminal:<VERSION>:api"
    }
	

or add the compile line to your existing dependencies task to your build.gradle

An example string would be `p455w0rd:WirelessCraftingTerminal:1.12.2-3.10.77:api` for the API only or `p455w0rd:WirelessCraftingTerminal:1.12.2-3.10.77`  for the whole mod.

## Wireless Crafting Terminal Localization

### English Text

`en_us` is included in this repository, fixes to typos are welcome.

### Encoding

Files must be encoded as UTF-8.

### New Translations

I would love for someone to do translations for me =]

## Credits

Thanks to the following people for support, code, et al
 
* Code/tutorials/general help: Notch, brandon3055, Techjar, AlgorythmX2, thatsIch, Nividica, squeek502, M3gaFr3ak, DrummerMC, cpw, LexManos, Pahimar, diesieben07, Wuppy, Jabelar, blay09, SirSengir, mezz

* Contributors: condensedapple, TKH+
