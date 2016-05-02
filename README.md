# Wireless Crafting Terminal (Beta)

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

Author: TheRealp455w0rd (reddit flair)
* [Website](http://p455w0rd.net/mc/)
* [IRC #p455w0rdCraft on esper.net](http://webchat.esper.net/?channels=p455w0rdCraft&prompt=1)
* [GitHub](https://github.com/p455w0rd/WirelessCraftingTerminal)
* [Twitter](https://twitter.com/TheRealp455w0rd)

## License

* TODO

## Downloads

* TODO

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It requires that [AE2 RV3 Beta](http://ae-mod.info/Downloads/) (any build) also be installed.

## Issues

* TODO

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

* TODO

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [synching](https://help.github.com/articles/syncing-a-fork) if you plan to contribute on regular basis.

## Wireless Crafting Terminal API

* There is an API =D Info coming soon

## Wireless Crafting Terminal Localization

### English Text

`en_US` is included in this repository, fixes to typos are welcome.

### Encoding

Files must be encoded as UTF-8.

### New Translations

I would love for someone to do translations for me =]

## Credits

Thanks to the following people for support, code, et al
 
* Notch, brandon3055, Techjar, AlgorythmX2, thatsIch, Nividica, squeek502, M3gaFr3ak, DrummerMC, cpw, LexManos, Pahimar, diesieben07, Wuppy, Jabelar, blay09, SirSengir, mezz

