# Datapack Extensions Contributing Guidelines

This document contains the guidelines for contributing to the Datapack Extensions project. It may be updated at any time so be sure to check back whenever you're contributing! 

Quick links:

* If you want to report a bug or suggest a feature, you may do so on
  the [issue tracker](https://github.com/TheDeathlyCow/tdc-data/issues).
* Documentation about existing features can be found on the [wiki](https://github.com/TheDeathlyCow/tdc-data/wiki)
* If you have any questions or need me to clarify something, my Discord is `TheDeathlyCow#1989`.

| Table of Contents                                                   |
|---------------------------------------------------------------------|
| [General Guidelines](#General-Guidelines)                           |
| [Commands](#Commands)                                               |
| [Advancements](#Advancement-Criteria-and-Predicates)                |
| [Code Guidelines](#Code-Guidelines)                                 |
| [Testing](#Testing)                                                 |
| [Translation](#Translation)                                         |
| [Existing Features in Other Mods](#Existing-Features-in-Other-Mods) | 


# General Guidelines

The mod id of this mod is `tdcdata`. All new features should use this namespace.

New features should be clear in both purpose and structure. That means that when you add something, it should be obvious what it does and why it needs to exist. For example, commands act as shorthands for existing commands, like `/gm`for `/gamemode`, may lack purpose (they're just doing something vanilla already does), and structure (someone unfamiliar with Spigot/old commands may not understand what `/gmc` means).

This mod is a server side mod, which means that no matter what you add, it should *always* be possible for clients to join without needing to install this mod themselves. Clients without the mod may not necessarily get everything that this mod has to offer (for example, custom argument types will not autocomplete properly unless this mod is installed client side), but they should still be able to join and play on servers that run this mod. This does mean that some things that would be very nice to have, like key input detection, may be very difficult or even impossible to do in this mod, but that is just a sacrifice we have to make for this to work as a server side mod. However, this doesn't mean that client side features are a complete no-go. If you have an idea for a client side feature for this mod, and that feature can work without needing all clients on a server to install this mod, then that feature is absolutely welcome!

New features should **NEVER** change or break vanilla behaviours. If a datapack works on a vanilla server, it should work exactly the same on a server with this mod. Changing or breaking vanilla features will almost certainly have unpredictable consequences for datapacks. Server owners should know that when they install this mod, all of their already existing datapacks will still work exactly the same as they did before. After all, mod is called Datapack *Extensions*, not Datapack *Modifications*.

While vanilla datapacks must always work with this mod, the reverse is not true. Therefore, it should be clear that when someone is using a feature of this mod that extends a vanilla feature. This could just be as simple as prefixing the parent element of your feature with this mod's ID, `tdcdata`. This makes it clear to people who are reading code made by datapack authors using this mod that they are using a feature added by this mod, and also avoid conflicts with features with the same name if they were ever to be implemented by vanilla. However, when adding a completely brand-new command, this is not strictly necessary, as the new command will be obvious to most people that is not vanilla by itself. Below are some examples of how you can do this in various contexts:

Adding a new field to a vanilla JSON object: The outermost field of your object should be prefixed with `tdcdata.`. For example, in the [light type predicate](https://github.com/TheDeathlyCow/tdc-data/wiki/Light-Type-Predicate):

```json
{
    "condition": "minecraft:location_check",
    "predicate": {
        "light": {
            "light": 15,
            "tdcdata.type": {
                "type": "sky",
                "include_sky_darkness": false
            }
        }
    }
}
```

Adding a new sub-command to a vanilla command: The beginning of the subcommand should be a literal prefixed with `tdcdata.`. For example in the [item condition of the execute command](https://github.com/TheDeathlyCow/tdc-data/wiki/Execute-Additions#execute-if-item), instead of using ```execute (if | unless) item```, we instead use ```execute (if | unless) tdcdata.item```. The only exception to this are the extra operations of `/scoreboard players operation`, as there wasn't really a way of doing this that looked 'nice'.

Adding new tags, advancement criteria, or other registered things: Register them under the namespace `tdcdata`. For example, the trigger `player_trigger_game_event` must be referred to by datapacks as `tdcdata:player_trigger_game_event`.

When serializing new pieces of data to NBT, it is best to put your custom data in an object called `tdcdata`. See the [serialize rules mixin](./src/main/java/com/github/thedeathlycow/tdcdata/mixin/scoreboard/teamrules/SerializeRulesMixin.java) for an example.

## Commands

New commands should generally do something that cannot be done in vanilla, can only be done with NBT data, or can only be done in some way in vanilla that is not particularly elegant. That last point is somewhat subjective, but covers things like NBT Crafting which requires a lot of overhead to implement and doesn't jive particularly well with how crafting normally works. Commands like [/freeze](https://github.com/TheDeathlyCow/tdc-data/wiki/Freeze-Command) and [/health](https://github.com/TheDeathlyCow/tdc-data/wiki/Health-Command) are good examples of commands that do something that can otherwise only really be done with NBT data.

## Advancement Criteria and Predicates

Advancement criteria are best thought of as different types of 'events' in datapacks, and so new criteria should be thought of not as new ways to actually get advancements, but as new ways to trigger events. For example, the [game event criteria]() allow datapacks to detect sculk events without needing to use actual sculk sensors and redstone.

# Code Guidelines

Where possible it is best to reuse vanilla code. For example Minecraft already provides a lot of reusable code for things in JSON serialisation that would be much better to use rather than reimplementing it yourself.

The execution of commands should be handled by static methods, rather than in lambda functions. These methods should always return an `int` (which will be the "result" of the command), and the first parameter of the method should be a `ServerCommandSource` object called `source`. The rest of the method's parameters should be the arguments of the command itself - do NOT pass in the command context itself. Where errors are encountered, the method should throw a `CommandSyntaxException` rather than return 0 - the exception will be handled by Brigadier. For example:

```java
private static int executeCommand(ServerCommandSource source, A argument1, B argument2, ...) throws CommandSyntaxException
```

Where possible, it is preferable to use the Fabric Event API to add listeners to vanilla code over Mixin, however Mixin is still fine. Just be sure not to use any `@Overwrite`s or `@Redirect`s, as these may break with other mods installed. `@Inject` should be able to handle most, if not all, cases.

# Testing

Where possible, it would be nice if you could provide testmod cases with your pull request. Commands can easily be tested using datapacks and command blocks. See [the testmod source set](./src/testmod/) for some examples.

# Translation

For translations into other languages: you can provide translations either in a file in an [issue](https://github.com/TheDeathlyCow/tdc-data/issues) or as a [pull request](https://github.com/TheDeathlyCow/tdc-data/pulls). I am happy for your translations either way!

Because this is a server side mod, we cannot use the regular translation system Minecraft provides for serverside features. If possible, you should reuse translations from vanilla, but if you want to add a new message, you must use literal text objects instead. All non-translatable messages should be in United States English, as this is the language Minecraft uses by default. Clientside features may of course use regular translatable objects. For features that force translation (like the names of statistics), you may provide translations for those as well.

# Existing Features in Other Mods

Some features you may want to add may already exist in other mods. Unless you have an idea for that feature that is very different from how it is implemented in these other mods, it may be best to just use those mods instead of reinventing the wheel. Below is a list of some mods that may add some of these types of features, though it is not comprehensive.

* [NBT Crafting](https://github.com/Siphalor/nbt-crafting)
* [Carpet (Custom Game Rules, Dummy Players, In-game Scripting)](https://github.com/gnembon/fabric-carpet)
* [Lithium (Server-side optimisations)](https://github.com/CaffeineMC/lithium-fabric)
* [Custom Entity Models](https://github.com/dorianpb/cem)
* [Fabric API (Convention Tags)](https://github.com/FabricMC/fabric/tree/1.19/fabric-convention-tags-v1)

