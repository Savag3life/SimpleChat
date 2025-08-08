# SimpleChat
1.21 native simple chat formatting solution. Depends on LuckPerms & supports PlaceholderAPI. Group based, fast, and easy. No bloat, no lag, no fuss.

- [Requires LuckPerms](https://luckperms.net/)
- [Supports PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
- [MiniMessage/Adventure](https://docs.advntr.dev/minimessage/index.html) Colors, Gradients, and Formatting
- **Requires Paper or a fork**. Spigot will not work!
- Configurable Event Priority (Allows you to override other plugins)
- Unlimited Group Formats

## Config
```yml
formats:
  default: "<white><bold>%name%</bold> <gray>» <white>"
  admin: "<red><bold>%prefix% %name%</bold> <gray>» <white>"
  owner: "<rainbow><bold>%prefix% %name% %suffix%</bold> <gray>» <white>"

priority: HIGHEST

messages:
  no_permission: "<white>Unknown command."
  reloaded: "<green>Simple Chat Reloaded."
```

