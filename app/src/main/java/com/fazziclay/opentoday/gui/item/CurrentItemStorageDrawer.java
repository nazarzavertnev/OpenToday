package com.fazziclay.opentoday.gui.item;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

import org.jetbrains.annotations.NotNull;

public class CurrentItemStorageDrawer {
    private final @NonNull Activity activity;
    private final @NotNull ViewGroup view;
    private final @NotNull ItemViewGenerator itemViewGenerator;
    private final @NotNull ItemViewGeneratorBehavior itemViewGeneratorBehavior;
    private final @NotNull CurrentItemStorage currentItemStorage;
    private final @NotNull OnUpdateListener listener = new OnUpdateListener();
    private final @NotNull HolderDestroyer holderDestroyer;
    private @Nullable OnCurrentItemStorageUpdate userListener = null;

    public CurrentItemStorageDrawer(@NonNull Activity activity,
                                    @NotNull ViewGroup view,
                                    @NonNull ItemViewGenerator itemViewGenerator,
                                    @NonNull ItemViewGeneratorBehavior itemViewGeneratorBehavior,
                                    @NonNull CurrentItemStorage currentItemStorage,
                                    @NotNull HolderDestroyer holderDestroyer) {
        this.activity = activity;
        this.itemViewGeneratorBehavior = itemViewGeneratorBehavior;
        this.itemViewGenerator = itemViewGenerator;
        this.currentItemStorage = currentItemStorage;
        this.view = view;
        this.holderDestroyer = holderDestroyer;
    }

    public void create() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, listener);
        updateView(currentItemStorage.getCurrentItem());
    }

    public void destroy() {
        currentItemStorage.getOnCurrentItemStorageUpdateCallbacks().removeCallback(listener);
        view.removeAllViews();
    }

    @NonNull
    public View getView() {
        return view;
    }

    private void updateView(Item currentItem) {
        view.removeAllViews();
        if (userListener != null) {
            userListener.onCurrentChanged(currentItem);
        }
        if (currentItem != null) {
            view.addView(itemViewGenerator.generate(currentItem, view, itemViewGeneratorBehavior, holderDestroyer));
        }
    }

    public void setOnUpdateListener(OnCurrentItemStorageUpdate listener) {
        userListener = listener;
    }

    private class OnUpdateListener implements OnCurrentItemStorageUpdate {
        @Override
        public Status onCurrentChanged(Item item) {
            activity.runOnUiThread(() -> updateView(item));
            return Status.NONE;
        }
    }
}
