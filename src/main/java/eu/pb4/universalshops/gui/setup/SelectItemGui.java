package eu.pb4.universalshops.gui.setup;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.gui.ExtraGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SelectItemGui extends SimpleGui implements ExtraGui {
    private static final Map<ItemGroup, List<ItemStack>> ITEM_GROUP_ITEMS = new IdentityHashMap<>();
    private static final List<ItemGroup> ITEM_GROUPS = new ArrayList<>();

    private final ItemModificatorGui.ItemStackHolder holder;
    private final GuiInterface previousGui;
    private ItemGroup currentGroup = ItemGroups.getSearchGroup();
    private List<ItemStack> currentItems = ITEM_GROUP_ITEMS.get(this.currentGroup);
    private int page;

    public SelectItemGui(ServerPlayerEntity player, ItemModificatorGui.ItemStackHolder holder) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.holder = holder;
        this.previousGui = GuiHelpers.getCurrentGui(player);
        if (!hasTexture()) {
            for (int i = 0; i < 9; i++) {
                this.setSlot(9 * 5 + i, GuiElements.FILLER);
            }
        }
        this.updateItemsVisual();

        this.setSlot(5 * 9 + 0, new GuiElementBuilder(Items.CHEST)
                .setName(TextUtil.gui("modifying_item.change_item.select_item_group.title").formatted(Formatting.WHITE))
                .setCallback((a, b, c, d) -> {
                    this.playClickSound();
                    this.close(true);
                    new SelectItemGroup();
                })
                .build());

        this.setSlot(5 * 9 + 2, GuiElements.previousPage(() -> {
            this.playClickSound();
            var pages = (int) Math.ceil(this.currentItems.size() / (9 * 5d));
            this.page = (pages + this.page - 1) % pages;
            this.updateItemsVisual();
        }));

        this.setSlot(5 * 9 + 4, GuiElements.BACK);

        this.setSlot(5 * 9 + 6, GuiElements.nextPage(() -> {
            this.playClickSound();
            var pages = (int) Math.ceil(this.currentItems.size() / (9 * 5d));
            this.page = (this.page + 1) % pages;
            this.updateItemsVisual();
        }));

        this.setTitle(texture(GuiBackground.SELECTOR_BIG).append(TextUtil.gui("modifying_item.change_item.title")));
        this.open();
    }

    private void updateItemsVisual() {
        for (int x = 0; x < 9 * 5; x++) {
            var i = this.page * 9 * 5 + x;
            this.setSlot(x, i < this.currentItems.size() ? GuiElementBuilder.from(this.currentItems.get(i)).setCallback(this::setStack).build() : GuiElement.EMPTY);
        }
    }

    private void setStack(int i, ClickType type, SlotActionType slotActionType) {
        this.holder.setItemStack(this.currentItems.get(i + this.page * 9 * 5).copy());
        this.playClickSound();
        this.close();
    }

    @Override
    public void close() {
        if (this.previousGui != null) {
            this.previousGui.open();
        } else {
            super.close();
        }
    }

    public static void updateCachedItems(MinecraftServer server) {
        ITEM_GROUP_ITEMS.clear();
        ITEM_GROUPS.clear();
        ITEM_GROUPS.add(ItemGroups.getSearchGroup());

        var items = ItemStackSet.create();

        for (var group : ItemGroups.getGroups()) {
            if (group.getType() == ItemGroup.Type.CATEGORY) {
                var contents = PolymerItemGroupUtils.getContentsFor(group, server.getRegistryManager().toImmutable(), server.getOverworld().getEnabledFeatures(), false);

                if (contents.main().size() > 0) {
                    ITEM_GROUP_ITEMS.put(group, new ArrayList<>(contents.main()));
                    ITEM_GROUPS.add(group);
                }
                items.addAll(contents.search());
            }
        }

        for (var group : PolymerItemGroupUtils.REGISTRY) {
            var contents = PolymerItemGroupUtils.getContentsFor(group, server.getRegistryManager().toImmutable(), server.getOverworld().getEnabledFeatures(), false);

            if (contents.main().size() > 0) {
                ITEM_GROUP_ITEMS.put(group, new ArrayList<>(contents.main()));
                ITEM_GROUPS.add(group);
            }

            items.addAll(contents.search());
        }
        ITEM_GROUP_ITEMS.put(ItemGroups.getSearchGroup(), new ArrayList<>(items));
    }


    private class SelectItemGroup extends SimpleGui {
        private int page;

        public SelectItemGroup() {
            super(ScreenHandlerType.GENERIC_9X6, SelectItemGui.this.player, false);

            this.updateItemsVisual();

            if (!hasTexture()) {
                for (int i = 0; i < 9; i++) {
                    this.setSlot(9 * 5 + i, GuiElements.FILLER);
                }
            }
            this.updateItemsVisual();
            this.setSlot(5 * 9 + 2, GuiElements.previousPage(() -> {
                SelectItemGui.this.playClickSound();
                var pages = (int) Math.ceil(ITEM_GROUPS.size() / (9 * 5d));
                this.page = (pages + this.page - 1) % pages;
                this.updateItemsVisual();
            }));

            this.setSlot(5 * 9 + 4, GuiElements.BACK);

            this.setSlot(5 * 9 + 6, GuiElements.nextPage(() -> {
                SelectItemGui.this.playClickSound();
                var pages = (int) Math.ceil(ITEM_GROUPS.size() / (9 * 5d));
                this.page = (this.page + 1) % pages;
                this.updateItemsVisual();
            }));

            this.setTitle(texture(GuiBackground.SELECTOR_BIG).append(TextUtil.gui("modifying_item.change_item.select_item_group.title")));
            this.open();
        }

        private void updateItemsVisual() {
            for (int x = 0; x < 9 * 5; x++) {
                var i = this.page * 9 * 5 + x;
                if (i < ITEM_GROUPS.size()) {

                    var group = ITEM_GROUPS.get(i);

                    var b = GuiElementBuilder.from(group.getIcon().copy()).setName(group.getDisplayName()).hideFlags();

                    if (group == SelectItemGui.this.currentGroup) {
                        b.glow();
                    }

                    this.setSlot(x, b.setCallback(this::setItemGroup).build());
                } else {
                    this.setSlot(x, GuiElement.EMPTY);
                }
            }
        }

        @Override
        public void onClose() {
            SelectItemGui.this.open();
        }

        private void setItemGroup(int i, ClickType type, SlotActionType slotActionType) {
            var group = ITEM_GROUPS.get(this.page * 9 * 5 + i);
            SelectItemGui.this.playClickSound();

            if (SelectItemGui.this.currentGroup != group) {
                SelectItemGui.this.page = 0;
                SelectItemGui.this.currentGroup = group;
                SelectItemGui.this.currentItems = ITEM_GROUP_ITEMS.get(group);
                SelectItemGui.this.updateItemsVisual();
            }

            SelectItemGui.this.open();

        }
    }
}
