package com.loficostudios.japaneseMinecraft.profile.gui;

import com.loficostudios.forgified.paper.gui.AbstractFloralGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerProfileGui extends AbstractFloralGui {
    public PlayerProfileGui() {
        super(9);
    }

    @Override
    public boolean open(@NotNull Player player) {
//        clear();
//        var factory = new PropertyIconFactory()
//                .renderer(GiftColor.class, new GiftColorIconRenderer())
//                .renderer(Language.class, new LanguageIconRenderer());
//        var properties = Property.getProperties(JapaneseMinecraft.getPlayerProfile(player));
//        for (int i = 0; i < properties.size(); i++) {
//            setSlot(i, factory.create(properties.get(i)));
//        }
        return super.open(player);
    }
}
