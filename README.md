CFRandomTeleport
================

CFRandomTeleport is a very simple Bukkit plugin that lets players run a command
to be teleported to completely random locations in worlds.

This plugin is also intended for newbie Bukkit plugin developers to learn from.


Features
--------

* Players simply use the /cfrtp command to teleport to a random location!
* Per-world configuration for: shape (round/rectangle), center X/Y, radius X/Y
* Continuous uniform distribution random algorithm (nerd talk for awesome randomness!)
* Integration with WorldBorder
* Very simple configuration
* Minimal permissions setup needed


Commands
--------

| Command | Description                    | Permission                |
| ------- | ------------------------------ | ------------------------- |
| cfrtp   | Teleport to a random location! | cfrandomteleport.teleport |


Configuration
-------------

The plugin can be configured by editing config.yml in its data folder
(typically plugins/CFRandomTeleport/config.yml).

When the plugin runs for the first time, it will save its default configuration 
to the data folder.

| Key                        | Description                                                                                                       | Default |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------- | ------- |
| use-worldborder            | If set to true, the plugin will use WorldBorder to get world border data. Else it will use its own configuration. | false   |
| teleport-on-specific-world | If specified, players will always be teleported onto this world instead of within their current world.            | ''      |

**At the moment only WorldBorder border data is supported. Therefore
use-worldborder needs to be set to true in the configuration!**


Permission Nodes
----------------

| Node                      | Description                                         | Default  |
| ------------------------- | --------------------------------------------------- | -------- |
| cfrandomteleport.teleport | Allows the player to teleport to a random location. | Only OPs |


Compiling
---------

This project uses [Maven](http://maven.apache.org/) to handle dependencies.

CFRandomTeleport has the following Maven dependencies:

* [Bukkit](https://github.com/Bukkit/Bukkit)
* [WorldBorder](https://github.com/Brettflan/WorldBorder) (Needs to be built locally)

Simply run `mvn clean install` on the project root to build the plugin.


Licence
-------

This project is licensed under a MIT/X11 licence. Please see LICENCE.txt for
more information.

