# JumpNRun
A Minecraft Plugin for creating Jump'n'Runs with ease.

## Requirements
To use this plugin, you need a spigot or paper server.
Just download the latest jar from the Releases-Section or build it yourself using `maven package` and move it into the `plugins` directory in your server folder.

## Usage
For configuring a new Jump'n'Run, head over to the config file and follow the instructions in the comments.
Once you have done this, reload your server and it should setup your new Jump'n'Run.

Players have to step on a pressure plate to start the Jump'n'Run.
A time is starting and they have to reach the end plate.
They can use checkpoints to save their progress for the case of falling down.
They're given items for navigating to the checkpoint or the start of the parkour.

You can place a sign that displays the current highscore holder.

## Commands
### Resetting all times of a Jump'n'Run
To reset a Jump'n'Run for all players, use the `/jnr reset` command.

**Syntax: ** `/jnr reset <JNR-Name>`
**Permission: ** `jumpnrun.command.reset`

### Removing the current highscore holder of a Jump'n'Run
To reset the current highscore and changing to the second best time, use the `/jnr resethighscore` command.

**Syntax: ** `/jnr resethighscore <JNR-Name>`
**Permission: ** `jumpnrun.command.reset`

### Reloading the plugin
Use the `/jnr reload` command to reload the entire plugin.

**Syntax: ** `/jnr reload`
**Permission: ** `jumpnrun.command.reload`