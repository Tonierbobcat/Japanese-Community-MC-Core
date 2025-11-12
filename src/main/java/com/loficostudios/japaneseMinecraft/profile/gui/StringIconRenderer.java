package com.loficostudios.japaneseMinecraft.profile.gui;

import com.loficostudios.forgified.paper.UnnamedModule0;
import com.loficostudios.forgified.paper.gui.GuiIcon;
import com.loficostudios.forgified.paper.gui.GuiManager;
import com.loficostudios.forgified.paper.gui.unnamed.Property;
import com.loficostudios.forgified.paper.gui.unnamed.PropertyIconRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.List;

public class StringIconRenderer implements PropertyIconRenderer<String> {
    @Override
    public GuiIcon icon(Property<String> property) {
        var string = property.get();
        var display = Component.text(property.name())
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false);

        var ico = GuiIcon.material(Material.NAME_TAG)
                .display(display)
                .description(List.of(
                        Component.text("§7Current: " + (string != null && !string.isEmpty() ? string : "§cnone" ))
                ));

        if (!property.mutable())
            return ico;

        ico = ico.onClick((p, c) -> {
            if (!c.isLeftClick())
                return;
            var last = GuiManager.getInstance().getGui(p);
            last.close(p);
            UnnamedModule0.queueEdit(p, last, (s) -> {
                property.set(s);
                p.sendMessage("Set string to " + s);
                last.open(p);
            });
        });

        return ico;
    }
}
