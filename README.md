# FarmControl
[Hangar](https://hangar.papermc.io/froobynooby/FarmControl) | [Modrinth](https://modrinth.com/plugin/farmcontrol) | [Spigot](https://www.spigotmc.org/resources/86923/)

## About
FarmControl is a Paper plugin that allows you to control certain properties of farms on your server.

**Requirements**: Paper (or Folia) 26.3 or newer, running on Java 25 or newer.

**Features**:
* Disable breeding in oversized animal farms and villager breeders.
* Reduce unnecessary random movement within mob farms.
* Disable the AI of mobs in farms.
* Limit the number of entities allowed in an area.
* Highly configurable - allowing you to tailor the plugin to your needs.
* Low impact - with the brunt of the plugin's processing performed asynchronously.

## Building
If you would like to build the plugin yourself you can follow these steps.

Building requires Java 25. If no Java 25 installation is found, Gradle will download one automatically.

1\. Clone FarmControl and build
```bash
git clone https://github.com/froobynooby/FarmControl
cd FarmControl
./gradlew clean build
```

2\. Find jar in `FarmControl/build/libs`
