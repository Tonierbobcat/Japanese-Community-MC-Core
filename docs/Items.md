# How to create item

define it it in this class
in [Items.java](src/main/java/com/loficostudios/japaneseMinecraft/items/Items.java)

```java
package com.loficostudios.japaneseMinecraft.items;

public class Items {

    private static final ItemRegistry ITEMS = new ItemRegistry();

    // Define your custom items here
    public static final JItem FLOWER_SWORD = ITEMS.create("flower_sword");
    public static final JItem GIFT_BAG = ITEMS.create("gift_bag");

}
```

and then add it to the items

[items.json](src/main/resources/items.json)

```json
{
  "flower_sword": {
    "material": "WOODEN_SWORD",
    "model": 0
  },
  "gift_bag": {
    "material": "BUNDLE",
    "model": 1
  }
}
```
