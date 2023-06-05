package com.fazziclay.opentoday.gui.item;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.DebugTickCounterItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.MathGameItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemCounterBinding;
import com.fazziclay.opentoday.databinding.ItemCycleListBinding;
import com.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemFilterGroupBinding;
import com.fazziclay.opentoday.databinding.ItemGroupBinding;
import com.fazziclay.opentoday.databinding.ItemLongtextBinding;
import com.fazziclay.opentoday.databinding.ItemMathGameBinding;
import com.fazziclay.opentoday.databinding.ItemTextBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.DebugUtil;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.annotation.ForItem;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.Arrays;

public class ItemViewGenerator {
    private static final String TAG = "ItemViewGenerator";
    @NonNull private final Activity activity;
    @NonNull private final LayoutInflater layoutInflater;
    @NonNull private final ItemViewGeneratorBehavior itemViewGeneratorBehavior;
    @NonNull private final ItemsStorageDrawerGenerator itemsStorageDrawerGenerator;
    @NonNull private final StorageEditsActions storageEdits;
    @Nullable private final ItemInterface itemOnClick; // Action when view click
    private final boolean previewMode; // Disable items minimize view patch & disable buttons

    public ItemViewGenerator(@NonNull final Activity activity, @NonNull final ItemViewGeneratorBehavior itemViewGeneratorBehavior, final boolean previewMode, @Nullable final ItemInterface itemOnClick, @NonNull final StorageEditsActions storageEdits, @NonNull ItemsStorageDrawerGenerator itemsStorageDrawerGenerator) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.itemViewGeneratorBehavior = itemViewGeneratorBehavior;
        this.previewMode = previewMode;
        this.itemOnClick = itemOnClick;
        this.storageEdits = storageEdits;
        this.itemsStorageDrawerGenerator = itemsStorageDrawerGenerator;
    }

    public static CreateBuilder builder(final Activity activity, final ItemViewGeneratorBehavior itemViewGeneratorBehavior, final ItemsStorageDrawerGenerator itemsStorageDrawerGenerator) {
        return new CreateBuilder(activity, itemViewGeneratorBehavior, itemsStorageDrawerGenerator);
    }

    public View generate(final Item item, final ViewGroup parent) {
        final Class<? extends Item> type = item.getClass();
        final View resultView;

        if (type == Item.class) {
            throw new RuntimeException("Illegal itemType to generate view. Use children's of Item");

        } else if (type == TextItem.class) {
            resultView = generateTextItemView((TextItem) item, parent);

        } else if (type == CheckboxItem.class) {
            resultView = generateCheckboxItemView((CheckboxItem) item, parent);

        } else if (type == DebugTickCounterItem.class) {
            resultView = generateDebugTickCounterItemView((DebugTickCounterItem) item, parent);

        } else if (type == DayRepeatableCheckboxItem.class) {
            resultView = generateDayRepeatableCheckboxItemView((DayRepeatableCheckboxItem) item, parent);

        } else if (type == CycleListItem.class) {
            resultView = generateCycleListItemView((CycleListItem) item, parent);

        } else if (type == CounterItem.class) {
            resultView = generateCounterItemView((CounterItem) item, parent);

        } else if (type == GroupItem.class) {
            resultView = generateGroupItemView((GroupItem) item, parent);

        } else if (type == FilterGroupItem.class) {
            resultView = generateFilterGroupItemView((FilterGroupItem) item, parent);

        } else if (type == LongTextItem.class) {
            resultView = generateLongTextItemView((LongTextItem) item, parent);

        } else if (type == MathGameItem.class) {
            resultView = generateMathGameItemView((MathGameItem) item, parent);

        } else {
            RuntimeException exception = new RuntimeException("Unexpected item type '" + type.getName() + "'! check ItemViewGenerator for fix this.");
            Log.e(TAG, "Unexpected item type to generate view. (wait 3000ms in DebugUtil.sleep())", exception);
            DebugUtil.sleep(3000);
            throw exception;
        }

        // Minimal height
        if (!item.isMinimize() && !previewMode) resultView.setMinimumHeight(item.getViewMinHeight());

        // BackgroundColor
        if (item.isViewCustomBackgroundColor()) {
            resultView.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }

        // Minimize view patch
        if (!previewMode && item.isMinimize()) {
            if (itemViewGeneratorBehavior.isMinimizeGrayColor()) {
                resultView.setForeground(AppCompatResources.getDrawable(activity, R.drawable.shape));
                resultView.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#44f0fff0")));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 15, 0);
            resultView.setLayoutParams(layoutParams);
        }
        if (itemOnClick != null) viewClick(resultView, () -> itemOnClick.run(item));
        applyForeground(resultView, item);
        return resultView;
    }

    private void applyForeground(View view, Item item) {
        view.setForeground(itemViewGeneratorBehavior.getForeground(item));
    }

    @ForItem(key = MathGameItem.class)
    private View generateMathGameItemView(MathGameItem item, ViewGroup parent) {
        final ItemMathGameBinding binding = ItemMathGameBinding.inflate(this.layoutInflater, parent, false);

        final MathGameInterface gameInterface = new MathGameInterface() {
            private String currentNumberStr = "0";
            private int currentNumber = 0;


            public void numberPress(byte b) {
                currentNumberStr += b;
                try {
                    currentNumber = Integer.parseInt(currentNumberStr);
                } catch (Exception ignored) {
                    currentNumber = (int) (Math.PI * 10000000);
                }
                currentNumberStr = String.valueOf(currentNumber);
                updateDisplay();
            }

            public void done() {
                int color;
                item.postResult(currentNumber);
                if (item.isResultRight(currentNumber)) {
                    color = Color.GREEN;
                    binding.questText.setText(item.getQuestText());
                    binding.questText.setTextSize(item.getQuestTextSize());
                    binding.questText.setGravity(item.getQuestTextGravity());
                } else {
                    color = Color.RED;
                }

                clear();
                binding.userEnterNumber.setBackgroundColor(color);
                new Handler().postDelayed(() -> binding.userEnterNumber.setBackgroundColor(Color.TRANSPARENT), 100);
            }

            public void clear() {
                setValue(0);
            }

            private void setValue(int v) {
                currentNumber = v;
                currentNumberStr = String.valueOf(v);
                updateDisplay();
            }

            private void updateDisplay() {
                binding.userEnterNumber.setText(currentNumberStr);
            }

            public void invert() {
                setValue(-currentNumber);
            }

            @Override
            public void init() {
                binding.questText.setText(item.getQuestText());
                binding.questText.setTextSize(item.getQuestTextSize());
                binding.questText.setGravity(item.getQuestTextGravity());

                binding.userEnterNumber.setText(currentNumberStr);
                viewClick(binding.userEnterNumber, this::invert);

                viewClick(binding.number0, () -> numberPress((byte) 0));
                viewClick(binding.number1, () -> numberPress((byte) 1));
                viewClick(binding.number2, () -> numberPress((byte) 2));
                viewClick(binding.number3, () -> numberPress((byte) 3));
                viewClick(binding.number4, () -> numberPress((byte) 4));
                viewClick(binding.number5, () -> numberPress((byte) 5));
                viewClick(binding.number6, () -> numberPress((byte) 6));
                viewClick(binding.number7, () -> numberPress((byte) 7));
                viewClick(binding.number8, () -> numberPress((byte) 8));
                viewClick(binding.number9, () -> numberPress((byte) 9));
                viewClick(binding.numberClear, this::clear);
                viewClick(binding.numberNext, this::done);
            }
        };

        // Text
        applyTextItemToTextView(item, binding.title);
        gameInterface.init();

        binding.keyboard.setEnabled(!previewMode);
        binding.userEnterNumber.setEnabled(!previewMode);
        binding.questText.setEnabled(!previewMode);
        binding.numberClear.setEnabled(!previewMode);
        binding.numberNext.setEnabled(!previewMode);
        binding.number0.setEnabled(!previewMode);
        binding.number1.setEnabled(!previewMode);
        binding.number2.setEnabled(!previewMode);
        binding.number3.setEnabled(!previewMode);
        binding.number4.setEnabled(!previewMode);
        binding.number5.setEnabled(!previewMode);
        binding.number6.setEnabled(!previewMode);
        binding.number7.setEnabled(!previewMode);
        binding.number8.setEnabled(!previewMode);
        binding.number9.setEnabled(!previewMode);


        if (item.isMinimize()) {
            binding.keyboard.setVisibility(View.GONE);
            binding.userEnterNumber.setVisibility(View.GONE);
            binding.questText.setGravity(Gravity.NO_GRAVITY);
            binding.questText.setTextSize(18);
        }

        return binding.getRoot();
    }

    public void destroy() {

    }

    public void create() {

    }

    interface MathGameInterface {
        void init();
    }

    @ForItem(key = LongTextItem.class)
    public View generateLongTextItemView(final LongTextItem item, final  ViewGroup parent) {
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);
        applyLongTextItemToLongTextView(item, binding.longText);

        return binding.getRoot();
    }

    @ForItem(key = DebugTickCounterItem.class)
    private View generateDebugTickCounterItemView(final DebugTickCounterItem item, final ViewGroup parent) {
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // Debugs
        binding.longText.setText(ColorUtil.colorize(item.getDebugStat(), Color.WHITE, Color.TRANSPARENT, Typeface.NORMAL));
        binding.longText.setTextSize(10);

        return binding.getRoot();
    }

    @ForItem(key = FilterGroupItem.class)
    private View generateFilterGroupItemView(final FilterGroupItem item, final ViewGroup parent) {
        final ItemFilterGroupBinding binding = ItemFilterGroupBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // FilterGroup
        if (!item.isMinimize()) {
            for (Item activeItem : item.getActiveItems()) {
                final ItemViewHolder holder = new ItemViewHolder(activity);
                holder.layout.addView(generate(activeItem, binding.content));

                binding.content.addView(holder.layout);
            }
        }

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> storageEdits.onFilterGroupEdit(item));

        return binding.getRoot();
    }

    @ForItem(key = GroupItem.class)
    private View generateGroupItemView(GroupItem item, ViewGroup parent) {
        final ItemGroupBinding binding = ItemGroupBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // Group
        if (!item.isMinimize()) {
            // TODO: 05.06.2023 use for-loop for generate all internal item separately?
            final ItemsStorageDrawer itemsStorageDrawer = itemsStorageDrawerGenerator.generate(item);
            itemsStorageDrawer.create();
            binding.content.addView(itemsStorageDrawer.getView());
        }
        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> storageEdits.onGroupEdit(item));

        return binding.getRoot();
    }

    @ForItem(key = CounterItem.class)
    public View generateCounterItemView(CounterItem item, ViewGroup parent) {
        final ItemCounterBinding binding = ItemCounterBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title);

        // Counter
        viewClick(binding.up, () -> runFastChanges(R.string.item_counter_fastChanges_up, item::up));
        viewClick(binding.down, () -> runFastChanges(R.string.item_counter_fastChanges_down, item::down));
        binding.up.setEnabled(!previewMode);
        binding.down.setEnabled(!previewMode);

        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    @ForItem(key = CycleListItem.class)
    public View generateCycleListItemView(CycleListItem item, ViewGroup parent) {
        final ItemCycleListBinding binding = ItemCycleListBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        // CycleList
        binding.next.setEnabled(!previewMode);
        binding.next.setOnClickListener(v -> item.next());
        binding.previous.setEnabled(!previewMode);
        binding.previous.setOnClickListener(v -> item.previous());

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> storageEdits.onCycleListEdit(item));

        if (!item.isMinimize()) {
            final CurrentItemStorageDrawer currentItemStorageDrawer = new CurrentItemStorageDrawer(this.activity, this, item);
            binding.content.addView(currentItemStorageDrawer.getView());
            currentItemStorageDrawer.setOnUpdateListener(currentItem -> {
                viewVisible(binding.empty, currentItem == null, View.GONE);
                return Status.NONE;
            });
            currentItemStorageDrawer.create();
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @ForItem(key = DayRepeatableCheckboxItem.class)
    public View generateDayRepeatableCheckboxItemView(DayRepeatableCheckboxItem item, ViewGroup parent) {
        final ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = CheckboxItem.class)
    public View generateCheckboxItemView(CheckboxItem item, ViewGroup parent) {
        final ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text);
        applyCheckItemToCheckBoxView(item, binding.checkbox);

        return binding.getRoot();
    }

    @ForItem(key = TextItem.class)
    public View generateTextItemView(TextItem item, ViewGroup parent) {
        final ItemTextBinding binding = ItemTextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title);

        return binding.getRoot();
    }

    //
    @SuppressLint("SetTextI18n")
    private void applyTextItemToTextView(final TextItem item, final TextView view) {
        if (Debug.SHOW_PATH_TO_ITEM_ON_ITEMTEXT) {
            view.setText(Arrays.toString(ItemUtil.getPathToItem(item)));
            view.setTextSize(15);
            view.setTextColor(Color.RED);
            view.setBackgroundColor(Color.BLACK);
            return;
        }
        if (Debug.SHOW_ID_ON_ITEMTEXT) {
            view.setText(item.getId() + "\n\n"+ item.getText());
            view.setTextSize(17);
            view.setTextColor(Color.GREEN);
            return;
        }

        final int textColor = item.isCustomTextColor() ? item.getTextColor() : ResUtil.getAttrColor(activity, R.attr.item_textColor);
        final SpannableString visibleText = item.isParagraphColorize() ? colorize(item.getText(), textColor) : SpannableString.valueOf(item.getText());
        final int MAX = 60;
        if (!previewMode && item.isMinimize()) {
            final String text = visibleText.toString().split("\n")[0];
            if (text.length() > MAX) {
                view.setText(new SpannableStringBuilder().append(visibleText.subSequence(0, MAX-3)).append("..."));
            } else {
                view.setText(visibleText);
            }
        } else {
            view.setText(visibleText);
        }
        if (item.isCustomTextColor()) {
            view.setTextColor(ColorStateList.valueOf(textColor));
        }
        if (item.isClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private SpannableString colorize(String text, int textColor) {
        return ColorUtil.colorize(text, textColor, Color.TRANSPARENT, Typeface.NORMAL);
    }

    private void applyLongTextItemToLongTextView(final LongTextItem item, final TextView view) {
        final int longTextColor = item.isCustomLongTextColor() ? item.getLongTextColor() : ResUtil.getAttrColor(activity, R.attr.item_textColor);
        final SpannableString visibleText = item.isParagraphColorize() ? colorize(item.getLongText(), longTextColor) : SpannableString.valueOf(item.getLongText());
        final int MAX = 150;
        if (!previewMode && item.isMinimize()) {
            if (visibleText.length() > MAX) {
                view.setText(new SpannableStringBuilder().append(visibleText.subSequence(0, MAX-3)).append("..."));
            } else {
                view.setText(visibleText);
            }
        } else {
            view.setText(visibleText);
        }
        if (item.isCustomLongTextSize()) view.setTextSize(item.getLongTextSize());
        if (item.isCustomLongTextColor()) {
            view.setTextColor(ColorStateList.valueOf(longTextColor));
        }
        if (item.isLongTextClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private void applyCheckItemToCheckBoxView(final CheckboxItem item, final CheckBox view) {
        view.setChecked(item.isChecked());
        view.setEnabled(!previewMode);
        viewClick(view, () -> {
            boolean to = view.isChecked();
            view.setChecked(!to);
            runFastChanges(to ? R.string.item_checkbox_fastChanges_checked : R.string.item_checkbox_fastChanges_unchecked, () -> {
                view.setChecked(to);
                item.setChecked(to);
                item.visibleChanged();
                item.save();
            });
        });
    }

    @SuppressWarnings("unused")
    private String getString(int resId, Object... formatArgs) {
        return activity.getString(resId, formatArgs);
    }

    private String getString(int resId) {
        return activity.getString(resId);
    }

    private void runFastChanges(int message, Runnable runnable) {
        runFastChanges(getString(message), runnable);
    }

    private void runFastChanges(String message, Runnable runnable) {
        if (itemViewGeneratorBehavior.isConfirmFastChanges()) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.fastChanges_dialog_title)
                    .setMessage(message)
                    .setNeutralButton(R.string.fastChanges_dialog_dontAsk, (_ignore, __ignore) -> {
                        itemViewGeneratorBehavior.setConfirmFastChanges(false);
                        runnable.run();
                    })
                    .setNegativeButton(R.string.fastChanges_dialog_cancel, null)
                    .setPositiveButton(R.string.fastChanges_dialog_apply, (_ignore, __ignore) -> runnable.run())
                    .show();
        } else {
            runnable.run();
        }
    }

    public static class CreateBuilder {
        private final Activity activity;
        private final ItemViewGeneratorBehavior itemViewGeneratorBehavior;
        private final ItemsStorageDrawerGenerator itemsStorageDrawerGenerator;
        private boolean previewMode = false;
        private ItemInterface onItemClick = null;
        private StorageEditsActions storageEditsAction = null;

        public CreateBuilder(final Activity activity, final ItemViewGeneratorBehavior itemViewGeneratorBehavior, final ItemsStorageDrawerGenerator itemsStorageDrawerGenerator) {
            this.activity = activity;
            this.itemViewGeneratorBehavior = itemViewGeneratorBehavior;
            this.itemsStorageDrawerGenerator = itemsStorageDrawerGenerator;
        }

        public CreateBuilder setPreviewMode() {
            this.previewMode = true;
            return this;
        }

        public CreateBuilder setPreviewMode(boolean b) {
            this.previewMode = b;
            return this;
        }

        public CreateBuilder setOnItemClick(ItemInterface i) {
            this.onItemClick = i;
            return this;
        }


        public CreateBuilder setStorageEditsActions(StorageEditsActions i) {
            this.storageEditsAction = i;
            return this;
        }

        public ItemViewGenerator build() {
            return new ItemViewGenerator(activity, itemViewGeneratorBehavior, previewMode, onItemClick, storageEditsAction, itemsStorageDrawerGenerator);
        }
    }
}
