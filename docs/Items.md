# How to create item

Define it it in this class
in [Items.java](src/main/java/com/loficostudios/japaneseMinecraft/items/Items.java)...

```java
package com.loficostudios.japaneseMinecraft.items;

public class Items {

    private static final ItemRegistry ITEMS = new ItemRegistry();

    // Define your custom items here
    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword");
    public static final JItem GIFT_BAG = ITEMS.create("gift_bag");

}
```

...and then add it to the items folder [Items](src/main/resources/assets/items)

`flower_sword.json`
```json
{
    "material": "WOODEN_SWORD",
    "model": 0
}
```

You can find a list of materials [HERE](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html)

## Example:

```java

public class Items {
    public static final JItem EXAMPLE_ITEM = ITEMS.create("example_item");
}
```

`example_item.json`
```json
{
    "material": "BEDROCK",
    "model": 6970
}
```
