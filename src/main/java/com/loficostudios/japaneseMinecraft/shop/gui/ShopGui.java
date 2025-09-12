package com.loficostudios.japaneseMinecraft.shop.gui;

import com.loficostudios.japaneseMinecraft.profile.PlayerProfile;
import com.loficostudios.japaneseMinecraft.shop.Shop;
import com.loficostudios.japaneseMinecraft.shop.ShopInstance;
import com.loficostudios.japaneseMinecraft.shop.ShopItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ShopGui<Impl extends ShopItem<Impl>> extends PopOutGui implements Paginated {
    int page = 0;
    private final Shop<Impl> shop;
    private final int[] slots;
    private final ShopGuiTemplate<Impl> template;

    private final Function<Player, PlayerProfile> getPlayer; //todo change this

    public ShopGui(@NotNull Shop<Impl> shop, @NotNull ShopGuiTemplate<Impl> template, @Nullable Consumer<Player> onClose, Function<Player, PlayerProfile> getPlayer) {
        super(template.size(), template.title(shop), onClose);
        this.shop = shop;
        this.getPlayer = getPlayer;

        setSlot(37, getPreviousButton()); //todo add templates
        setSlot(43, getNextButton()); //todo add templates

        slots = new int[] { //todo add templates
                0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26
        };

        this.template = template;
    }

    public ShopGui(@NotNull Shop<Impl> shop, @NotNull ShopGuiTemplate<Impl> template, Function<Player, PlayerProfile> getPlayer) {
        this(shop, template, null, getPlayer);
    }

    private void loadPage(Player player, int page) {
        for (int slot : slots) {
            setSlot(slot, (ItemStack) null);
        }
        var slot = 0;  //todo add templates
        var instances = new ArrayList<>(shop.getInstances());
        var paginated = Paginated.paginate(instances, page, 27);

        for (ShopInstance<Impl> instance : paginated) {
            var clone = instance.clone();

            template.onInstance().accept(instance, player);

            var item = clone.getItem();

            GuiIcon icon;
            if (item instanceof ShopGuiIcon) {
                icon = ((ShopGuiIcon) item).getIcon(clone, 1, (p, c) -> {
                    if (clone.isStackable()) {
                        new AmountSelectionGui<>(this, clone, getPlayer)
                                .open(p);
                    } else {
                        var result = shop.buyItem(getPlayer.apply(p), clone);
                        template.onBuy(result, player);
                    }
                });
            } else {
                icon = new GuiIcon(Material.PURPLE_STAINED_GLASS_PANE, Component.text(item.getName()));
                icon.amount(1);
            }

            template.handleIcon.accept(instance, icon, 1);

            setSlot(slot, icon);  //todo add templates
            slot++;  //todo add templates
        }
    }

    @Override
    public void create(Player player) {
        loadPage(player, 0);
    }

    private GuiIcon getNextButton() {
        return new GuiIcon(ItemStack.of(Material.LIME_WOOL), "", (p,c) -> {
            nextPage(p);
        });
    }

    private GuiIcon getPreviousButton() {
        return new GuiIcon(ItemStack.of(Material.RED_WOOL), "", (p,c) -> {
            previousPage(p);
        });
    }

    @Override
    public void nextPage(Player player) {
        page++;
        loadPage(player, page);
    }

    @Override
    public void previousPage(Player player) {
        page = Math.max(0, page - 1);
        loadPage(player, page);
    }

    public static class AmountSelectionGui<Impl extends ShopItem<Impl>> extends PopOutGui {
        private int selectedAmount = 1;
        private final ShopInstance<Impl> instance;
        private final ShopGui<Impl> parent;
        private final Function<Player, PlayerProfile> getPlayer; //todo change this

        public AmountSelectionGui(ShopGui<Impl> parent, ShopInstance<Impl> instance, Function<Player, PlayerProfile> getPlayer) {
            super(parent.template.selectionSize(), parent.template.selectionTitle(instance), parent::open);
            this.instance = instance;
            this.parent = parent;
            this.getPlayer = getPlayer;
        }

        @Override
        public void create(Player player) {
            updateItem();

            setSlot(0, getRemoveIcon(32));
            setSlot(1, getRemoveIcon(16));
            setSlot(2, getRemoveIcon(8));
            setSlot(3, getRemoveIcon(1));

            setSlot(5, getAddIcon(1));
            setSlot(6, getAddIcon(8));
            setSlot(7, getAddIcon(16));
            setSlot(8, getAddIcon(32));
        }

        private GuiIcon getRemoveIcon(int amount) {
            return new GuiIcon(parent.template.getRemoveAmountIcon(instance, amount), "", (p,c) -> removeSome(amount));
        }

        private GuiIcon getAddIcon(int amount) {
            return new GuiIcon(parent.template.getAddAmountIcon(instance, amount), "", (p,c) -> addSome(amount));
        }

        public void updateItem() {
            var amount = Math.max(1, selectedAmount);
            var item = instance.getItem();

            GuiIcon icon;
            if (item instanceof ShopGuiIcon) {
                icon = ((ShopGuiIcon) item).getIcon(instance, amount, (p, c) -> confirm(p, selectedAmount));
                parent.template.handleIcon.accept(instance, icon, amount);
            } else {
                icon = new GuiIcon(Material.PURPLE_STAINED_GLASS_PANE, Component.text(item.getName()));
                icon.amount(amount);
            }

            setSlot(4, icon);
        }

        public void removeSome(int amount) {
            selectedAmount = Math.max(1, selectedAmount - amount);
            updateItem();
        }
        public void addSome(int amount) {
            selectedAmount = Math.min(64, selectedAmount + amount);
            updateItem();
        }

        public void confirm(Player player, int amount) {
            var result = parent.shop.buyItem(getPlayer.apply(player), instance, amount);
            parent.template.onBuy(result, player);
        }
    }


}
