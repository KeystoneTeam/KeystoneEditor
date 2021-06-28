# Keystone Editor
A reimagining of [MCEdit Unified](https://www.mcedit-unified.net/). A Minecraft world editing mod which aims to make large scale editing as easy as possible using tools similar to the outdated program MCEdit. You can view the Keystone javadoc [here](https://keystoneteam.github.io)

Download
--------------
The mod is currently unfinished and in alpha. You can find a snapshot builds on the [GitHub releases page](https://github.com/KeystoneTeam/KeystoneEditor/releases/), but these are not complete and may have bugs. Be sure to backup any worlds you use this mod with.

Contributing
--------------
If you'd like to contribute something to Keystone Editor, you're free to do so. Any methods you change or add require javadoc comments saying what they do. Not all changes will be accepted. If you're unsure if your suggestion fits the mod, [open an issue](https://github.com/KeystoneTeam/KeystoneEditor/issues) to discuss it first!

Compilation
--------------
1) Clone this repository and check out the branch of the version you want to build. (master is updated whenever a new version is released)
2) Load it into an IDE of your choice and import the project.
3) Run `gradlew genIntellijRuns`, `gradlew genVSCodeRuns` or `gradlew genEclipseRuns` depending on the IDE you use.
4) Run `gradlew build` to build the jar
5) You'll find the built jars in the `/build/libs/` folder. The file ending with (Forge \<version\>) is the mod file with all required dependencies, the other jar file only contains the actual mod code and will not work in Minecraft
