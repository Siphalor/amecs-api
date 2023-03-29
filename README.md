<div align="center">
<img alt="Logo" src="src/main/resources/assets/amecsapi/icon.png" />

# Amecs API (or Amecs' API)

![supported Minecraft versions: 1.14 | 1.15 | 1.16 | 1.17 | 1.18 | 1.19](https://img.shields.io/badge/support%20for%20MC-1.14%20%7C%201.15%20%7C%201.16%20%7C%201.17%20%7C%201.18%20%7C%201.19-%2356AD56?style=for-the-badge)

[![latest maven release](https://img.shields.io/maven-metadata/v?color=0f9fbc&metadataUrl=https%3A%2F%2Fmaven.siphalor.de%2Fde%2Fsiphalor%2Famecsapi-1.16%2Fmaven-metadata.xml&style=flat-square)](https://maven.siphalor.de/de/siphalor/amecsapi-1.16/)

This library mod provides modifier keys for Minecraft keybindings as well as some related keybinding utilities

**&nbsp;
[Amecs](https://github.com/Siphalor/amecs) ·
[Discord](https://discord.gg/6gaXmbj)
&nbsp;**

</div>

## Usage
 If you're not a modder you're probably looking for [Amecs](https://github.com/Siphalor/amecs).
 
 If you **are** a modder then you can use Amecs with the following in your build.gradle:
 
 ```groovy
repositories {
    maven {
        url "https://maven.siphalor.de/"
        name "Siphalor's Maven"
    }
}

dependencies {
    modImplementation "de.siphalor:amecsapi-1.15:1+"
    include "de.siphalor:amecsapi-1.15:1+"
}
```

Have fun ;)

## License

This mod is licensed under [the Apache 2.0 license](./LICENSE).
