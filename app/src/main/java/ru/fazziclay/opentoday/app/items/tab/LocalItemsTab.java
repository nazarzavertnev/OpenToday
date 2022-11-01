package ru.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.SimpleItemsStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemIEUtil;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public class LocalItemsTab extends Tab {
    public static final LocalItemsTabIETool IE_TOOL = new LocalItemsTabIETool();
    protected static class LocalItemsTabIETool extends Tab.TabIETool {
        @NonNull
        @Override
        public JSONObject exportTab(@NonNull Tab tab) throws Exception {
            return super.exportTab(tab)
                    .put("items", ItemIEUtil.exportItemList(tab.getAllItems()));
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull JSONObject json, @Nullable Tab tab) throws Exception {
            LocalItemsTab localItemsTab = tab != null ? (LocalItemsTab) tab : new LocalItemsTab();
            super.importTab(json, localItemsTab);
            localItemsTab.itemsStorage.importData(ItemIEUtil.importItemList(json.getJSONArray("items")));
            return localItemsTab;
        }
    }

    private final SimpleItemsStorage itemsStorage;

    public LocalItemsTab(UUID id, String name) {
        this(id, name, new Item[0]);
    }

    public LocalItemsTab(UUID id, String name, Item[] data) {
        super(id, name);
        itemsStorage = new SimpleItemsStorage(new ArrayList<>(Arrays.asList(data))) {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
    }

    protected LocalItemsTab() {
        itemsStorage = new SimpleItemsStorage() {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
    }

    @Override
    public void addItem(Item item) {
        itemsStorage.addItem(item);
    }

    @Override
    public void addItem(Item item, int position) {
        itemsStorage.addItem(item, position);
    }

    @Override
    public void deleteItem(Item item) {
        itemsStorage.deleteItem(item);
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return itemsStorage.copyItem(item);
    }

    @Override
    public int getItemPosition(Item item) {
        return itemsStorage.getItemPosition(item);
    }

    @Nullable
    @Override
    public Item getItemById(UUID itemId) {
        return itemsStorage.getItemById(itemId);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        itemsStorage.move(positionFrom, positionTo);
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsStorage.getAllItems();
    }

    @Override
    public void tick(TickSession tickSession) {
        itemsStorage.tick(tickSession);
    }

    @Override
    public int size() {
        return itemsStorage.size();
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return itemsStorage.getOnUpdateCallbacks();
    }
}
