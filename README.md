# RS Unified Terminal

An addon for [Refined Storage](https://github.com/refinedmods/refinedstorage) that adds the **Wireless Universal Terminal**: a single wireless item that combines a Crafting Grid, Pattern Grid, Crafter Manager and Crafting Monitor.

## Items

| Item | How to obtain |
|---|---|
| Wireless Universal Terminal | Shapeless recipe: Wireless Crafting Grid + Wireless Fluid Grid + Wireless Crafting Monitor + Pattern Grid + Crafter Manager |
| Creative Wireless Universal Terminal | Creative mode only |

- Uses the same energy system as the other wireless terminals, with a default capacity of 32000 FE (configurable in `rsunifiedterminal-common.toml`)
- Can be opened by right-clicking it, with a hotkey ("Open Wireless Universal Terminal", unbound by default, found in the Refined Storage category of the key bindings), or from a Curios slot

## Pages

- **Crafting Grid**: the "View" button on the left switches between items and fluids, while keeping the crafting matrix
- **Pattern Grid**: write crafting and processing patterns
- **Crafter Manager**: manage the patterns of every crafter in the network
- **Crafting Monitor**: opened with the button in the top right corner of the terminal; the button in the same spot on the Crafting Monitor goes back
- **Middle-click** a resource on the Crafting Grid or Pattern Grid page to open its autocrafting request window directly, even if it is already in stock
- The search text is kept when switching pages, and cleared when the terminal is closed (`keepSearchOnPageSwitch` in `rsunifiedterminal-client.toml`, on by default)

## Dependencies

- Minecraft 1.20.1, Forge 47.x
- Refined Storage 1.12.4+
- Refined Storage Addons 0.10.0+
- Optional: JEI, Curios, Refined Polymorphism

## Implementation notes

The Crafting Grid and Pattern Grid pages are backed by a "wireless node" that extends Refined Storage's `GridNetworkNode` and stores its data on the item, so they reuse Refined Storage's own grid screen, pattern logic and JEI integration. The Crafter Manager page is adapted from Refined Storage's Crafter Manager (MIT).

## Building

```sh
./gradlew build      # output in build/libs/
./gradlew runClient  # development client
```

Requires JDK 17.

## License

[MIT](LICENSE)
