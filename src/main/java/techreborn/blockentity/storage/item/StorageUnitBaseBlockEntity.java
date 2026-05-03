/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.blockentity.storage.item;

import reborncore.common.transfer.RcItemVariant;
import reborncore.common.transfer.RcStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import reborncore.api.IListInfoProvider;
import reborncore.api.IToolDrop;
import reborncore.api.blockentity.InventoryProvider;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.blockentity.SlotConfiguration;
import reborncore.common.compat.TransferApiBridge;
import reborncore.common.screen.BuiltScreenHandler;
import reborncore.common.screen.BuiltScreenHandlerProvider;
import reborncore.common.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.RebornInventory;
import reborncore.common.util.WorldUtils;
import techreborn.init.TRBlockEntities;
import techreborn.init.TRContent;

import java.util.List;

public class StorageUnitBaseBlockEntity extends MachineBaseBlockEntity implements InventoryProvider, SlotConfiguration.SlotFilter, IToolDrop, IListInfoProvider, BuiltScreenHandlerProvider {

	// Inventory constants
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;

	// Client sync variables for GUI, what and how much stored
	public int storedAmount = 0;

	protected final RebornInventory<StorageUnitBaseBlockEntity> inventory;
	private int maxCapacity;
	private int serverCapacity = -1;

	private ItemStack storeItemStack;
	// Fabric transfer API support for the internal stack (one per direction);
	private final TransferApiBridge.SingleStackStorageHandle[] internalStoreStorage = new TransferApiBridge.SingleStackStorageHandle[6];

	private TRContent.StorageUnit type;

	// A locked storage unit will continue behaving as if it contains
	// the locked-in item, even if the stored amount drops to zero.
	private ItemStack lockedItemStack = ItemStack.EMPTY;

	public StorageUnitBaseBlockEntity(BlockPos pos, BlockState state) {
		super(TRBlockEntities.STORAGE_UNIT, pos, state);
		inventory = new RebornInventory<>(2, "ItemInventory", 64, this);
	}

	public StorageUnitBaseBlockEntity(BlockPos pos, BlockState state, TRContent.StorageUnit type) {
		super(TRBlockEntities.STORAGE_UNIT, pos, state);
		inventory = new RebornInventory<>(2, "ItemInventory", 64, this);
		configureEntity(type);
	}
	private void configureEntity(TRContent.StorageUnit type) {
		// Set capacity to local config unless overridden by server
		if(serverCapacity == -1){
			this.maxCapacity = type.capacity;
		}
		storeItemStack = ItemStack.EMPTY;
		this.type = type;
	}

	public boolean isLocked() {
		return lockedItemStack != ItemStack.EMPTY;
	}

	public void setLocked(boolean value) {
		if (isLocked() == value) {
			return;
		}

		// Only set lockedItem in response to user input
		ItemStack stack = getStoredStack().copy();
		stack.setCount(1);
		lockedItemStack = value ? stack : ItemStack.EMPTY;
		syncWithAll();
	}

	public boolean canModifyLocking() {
		// Can always be unlocked
		if (isLocked()) {
			return true;
		}

		// Can only lock if there is an item to lock
		return !isEmpty();
	}

	private void populateOutput() {
		// Set to storeItemStack to get the stack type
		ItemStack output = storeItemStack.copy();

		int outputSlotCount = inventory.getItem(OUTPUT_SLOT).getCount();

		// Set to current outputSlot count
		output.setCount(outputSlotCount);

		// Calculate amount needed to fill stack in output slot
		int amountToFill = getStoredStack().getMaxStackSize() - outputSlotCount;

		if (storeItemStack.getCount() >= amountToFill) {
			storeItemStack.shrink(amountToFill);

			if (storeItemStack.isEmpty()) {
				storeItemStack = ItemStack.EMPTY;
			}

			output.grow(amountToFill);
		} else {
			output.grow(storeItemStack.getCount());
			storeItemStack = ItemStack.EMPTY;
		}

		inventory.setItem(OUTPUT_SLOT, output);
	}

	private void addStoredItemCount(int amount) {
		storeItemStack.grow(amount);
	}

	public ItemStack getStoredStack() {
		return storeItemStack.isEmpty() ? inventory.getItem(OUTPUT_SLOT) : storeItemStack;
	}

	// Returns the ItemStack to be displayed to the player via UI / model
	public ItemStack getDisplayedStack() {
		if (!isLocked()) {
			return getStoredStack();
		} else {
			// Render the locked stack even if the unit is empty
			return lockedItemStack;
		}
	}

	public ItemStack getAll() {
		ItemStack returnStack = ItemStack.EMPTY;

		if (!isEmpty()) {
			returnStack = getStoredStack().copy();
			returnStack.setCount(getCurrentCapacity());
		}

		return returnStack;
	}

	public ItemStack processInput(ItemStack inputStack) {
		if (!isStackValid(INPUT_SLOT, inputStack)){
			return inputStack;
		}

		// Amount of items that can be added before reaching capacity
		int reminder = maxCapacity - getCurrentCapacity();
		NonNullList<ItemStack> optionalShulkerStack = ItemUtils.getBlockEntityStacks(inputStack);
		if (isLocked() && ItemUtils.canExtractFromCachedShulker(optionalShulkerStack, lockedItemStack) > 0 ) {
			Tuple<Integer, ItemStack> pair = ItemUtils.extractFromShulker(inputStack, optionalShulkerStack, lockedItemStack, reminder);
			if (pair.getA() != 0) {
				int amount = pair.getA();
				if (storeItemStack.isEmpty()) {
					storeItemStack = lockedItemStack.copy();
					amount = amount -1;
				}
				addStoredItemCount(amount);
				inputStack = pair.getB().copy();
				inventory.setHashChanged();
			}
			return inputStack;
		}
		if (inputStack.getCount() <= reminder) {
			// Add full stack
			if (storeItemStack == ItemStack.EMPTY){
				// copy input stack into stored if everything is in OUTPUT_SLOT
				storeItemStack = inputStack.copy();
			}
			else {
				addStoredItemCount(inputStack.getCount());
			}

			inputStack = ItemStack.EMPTY;
		} else {
			// Add only what is needed to reach max capacity
			if (storeItemStack == ItemStack.EMPTY) {
				storeItemStack = inputStack.copy();
				storeItemStack.setCount(reminder);
			} else {
				addStoredItemCount(reminder);
			}
			inputStack.shrink(reminder);
		}

		inventory.setHashChanged();
		return inputStack;
	}

	// Creative function
	private void fillToCapacity() {
		storeItemStack = getStoredStack();
		storeItemStack.setCount(maxCapacity);

		inventory.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
	}

	public boolean isFull() {
		return getCurrentCapacity() == maxCapacity;
	}

	public int getCurrentCapacity() {
		return storeItemStack.getCount() + inventory.getItem(OUTPUT_SLOT).getCount();
	}

	// MachineBaseBlockEntity
	@Override
	public void tick(Level world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity) {
		super.tick(world, pos, state, blockEntity);
		if (world == null || world.isClientSide) {
			return;
		}
		// If there is an item in the input AND stored is less than max capacity
		if (!inventory.getItem(INPUT_SLOT).isEmpty() && !isFull()) {
			inventory.setItem(INPUT_SLOT, processInput(inventory.getItem(INPUT_SLOT)));
		}

		// Fill output slot with goodies when stored has items and output count is less than max stack size
		if (storeItemStack.getCount() > 0 && inventory.getItem(OUTPUT_SLOT).getCount() < getStoredStack().getMaxStackSize()) {
			populateOutput();
		}

		if (type == TRContent.StorageUnit.CREATIVE) {
			if (!isFull() && !isEmpty()) {
				fillToCapacity();
			}
			// void input items for creative storage (#2205)
			if (!inventory.getItem(INPUT_SLOT).isEmpty()){
				inventory.setItem(INPUT_SLOT, ItemStack.EMPTY);
			}
		}

		if (inventory.hasChanged()) {
			syncWithAll();
			inventory.resetHasChanged();
		}
	}

	@Override
	public boolean isEmpty() {
		return getCurrentCapacity() == 0;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		if (!super.canPlaceItemThroughFace(index, stack, direction)) {
			return false;
		}
		if (index == INPUT_SLOT) {
			return isStackValid(INPUT_SLOT, stack);
		}
		return true;
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { INPUT_SLOT };
	}

	@Override
	public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider registryLookup) {
		super.loadAdditional(tagCompound, registryLookup);

		if (tagCompound.contains("unitType")) {
			this.type = TRContent.StorageUnit.valueOf(tagCompound.getString("unitType"));
			configureEntity(type);
		} else {
			this.type = TRContent.StorageUnit.QUANTUM;
		}

		storeItemStack = ItemStack.EMPTY;

		if (tagCompound.contains("storedStack")) {
			storeItemStack = ItemStack.parse(registryLookup, tagCompound.getCompound("storedStack")).orElseThrow();
		}

		if (!storeItemStack.isEmpty()) {
			storeItemStack.setCount(Math.min(tagCompound.getInt("storedQuantity"), this.maxCapacity));
		}

		// Renderer only
		if (tagCompound.contains("totalStoredAmount")) {
			storedAmount = tagCompound.getInt("totalStoredAmount");
		}

		if (tagCompound.contains("lockedItem")) {
			lockedItemStack = ItemStack.parse(registryLookup, tagCompound.getCompound("lockedItem")).orElseThrow();
		}
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider registryLookup) {
		super.saveAdditional(tagCompound, registryLookup);

		tagCompound.putString("unitType", this.type.name());

		if (!storeItemStack.isEmpty()) {
			ItemStack temp = storeItemStack.copy();
			if (storeItemStack.getCount() > storeItemStack.getMaxStackSize()) {
				temp.setCount(storeItemStack.getMaxStackSize());
			}
			tagCompound.put("storedStack", temp.save(registryLookup, new CompoundTag()));
			tagCompound.putInt("storedQuantity", Math.min(storeItemStack.getCount(), maxCapacity));
		} else {
			tagCompound.putInt("storedQuantity", 0);
		}

		// Renderer only
		tagCompound.putInt("totalStoredAmount", getCurrentCapacity());

		if (isLocked()) {
			tagCompound.put("lockedItem", lockedItemStack.save(registryLookup));
		}
	}

	@Override
	public void onBreak(Level world, Player playerEntity, BlockPos blockPos, BlockState blockState) {
		super.onBreak(world, playerEntity, blockPos, blockState);

		// No need to drop anything for creative peeps
		if (type == TRContent.StorageUnit.CREATIVE) {
			this.inventory.clearContent();
			return;
		}

		if (storeItemStack != ItemStack.EMPTY) {
			if (storeItemStack.getMaxStackSize() == 64) {
				// Drop stacks (In one clump, reduce lag)
				WorldUtils.dropItem(storeItemStack, world, blockPos);
			} else {
				int size = storeItemStack.getMaxStackSize();

				for (int i = 0; i < storeItemStack.getCount() / size; i++) {
					ItemStack toDrop = storeItemStack.copy();
					toDrop.setCount(size);
					WorldUtils.dropItem(toDrop, world, blockPos);
				}

				if (storeItemStack.getCount() % size != 0) {
					ItemStack toDrop = storeItemStack.copy();
					toDrop.setCount(storeItemStack.getCount() % size);
					WorldUtils.dropItem(toDrop, world, blockPos);
				}

			}
		}

		// Inventory gets dropped automatically
	}

	@Override
	public boolean isStackValid(int slot, ItemStack inputStack) {
		if (slot != INPUT_SLOT) {
			return false;
		}
		if (inputStack == ItemStack.EMPTY) {
			return false;
		}
		// Do not allow player heads into storage due to lag. Fix #2888
		if (inputStack.getItem() instanceof PlayerHeadItem) {
			return false;
		}
		// do not allow other storage units to avoid NBT overflow. Fix #2580
		if (inputStack.is(TRContent.ItemTags.STORAGE_UNITS)) {
			return false;
		}

		if (isLocked()) {
			//allow shulker bundle extraction when locked
			if (ItemUtils.canExtractAnyFromShulker(inputStack, lockedItemStack)) {
				return true;
			}
			return ItemUtils.isItemEqual(lockedItemStack, inputStack, true, true);
		}

		if (isEmpty()){
			return true;
		}

		return ItemUtils.isItemEqual(getStoredStack(), inputStack, true, true);
	}

	@Override
	public boolean canBeUpgraded() {
		return false;
	}

	// InventoryProvider
	@Override
	public net.minecraft.world.Container getInventory() {
		return inventory;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(Player entityPlayer) {
		ItemStack dropStack = new ItemStack(getBlockType(), 1);
		final CompoundTag nbt = new CompoundTag();
		if (level != null){
			saveAdditional(nbt, level.registryAccess());
			dropStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
		}

		return dropStack;
	}

	// IListInfoProvider
	@Override
	public void addInfo(final List<Component> info, final boolean isReal, boolean hasData) {
		if (isReal || hasData) {
			if (!this.isEmpty()) {
				info.add(
						Component.literal(String.valueOf(this.getCurrentCapacity()))
								.append(Component.translatable("techreborn.tooltip.unit.divider"))
								.append(this.getStoredStack().getHoverName())
				);
			} else {
				info.add(Component.translatable("techreborn.tooltip.unit.empty"));
			}
		}

		info.add(
				Component.translatable("techreborn.tooltip.unit.capacity")
						.withStyle(ChatFormatting.GRAY)
						.append(
								Component.literal(String.valueOf(this.getMaxCapacity()))
										.withStyle(ChatFormatting.GOLD)
										.append(" ")
										.append(Component.translatable("techreborn.tooltip.unit.items"))
										.append(" (")
										.append(String.valueOf(this.getMaxCapacity() / 64))
										.append(" ")
										.append(Component.translatable("techreborn.tooltip.unit.stacks"))
										.append(")")
						)
		);
	}

	// BuiltScreenHandlerProvider
	@Override
	public BuiltScreenHandler createScreenHandler(int syncID, final Player playerEntity) {
		return new ScreenHandlerBuilder("chest").player(playerEntity.getInventory()).inventory().hotbar().addInventory()
				.blockEntity(this)
				.slot(INPUT_SLOT, 100, 53)
				.outputSlot(OUTPUT_SLOT, 140, 53)
				.sync(ByteBufCodecs.INT, this::isLockedInt, this::setLockedInt)
				.sync(ByteBufCodecs.COMPOUND_TAG, this::getStoredStackNBT, this::setStoredStackFromNBT)
				.sync(ByteBufCodecs.INT, this::getStoredAmount, this::setStoredAmount)
				.sync(ByteBufCodecs.INT, this::getMaxCapacity, this::setMaxCapacity)
				.addInventory().create(this, syncID);

		// Note that inventory is synced, and it gets the stack from that
	}

	// The int methods are only for ContainerBuilder.sync()
	private int isLockedInt() {
		return isLocked() ? 1 : 0;
	}

	private void setLockedInt(int lockedInt) {
		setLocked(lockedInt == 1);
	}

	public int getStoredAmount() {
		return this.getCurrentCapacity();
	}

	public void setStoredAmount(int storedAmount) {
		this.storedAmount = storedAmount;
	}

	// Sync between server/client if configs are mis-matched.
	public int getMaxCapacity() {
		return this.maxCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
		this.serverCapacity = maxCapacity;
	}

	public CompoundTag getStoredStackNBT() {
		CompoundTag tag = new CompoundTag();
		ItemStack stack = getStoredStack();

		tag.putInt("count", stack.getCount());

		if (!stack.isEmpty()) {
			// We are not allowed to serialize empty or large stacks
			ItemStack singleStack = stack.copy();
			singleStack.setCount(1);
			tag.put("item", singleStack.save(level.registryAccess(), new CompoundTag()));
		}

		return tag;
	}

	public void setStoredStackFromNBT(CompoundTag tag) {
		if (!tag.contains("item")) {
			storeItemStack = ItemStack.EMPTY;
		} else {
			storeItemStack = ItemStack.parse(level.registryAccess(), tag.getCompound("item")).orElseThrow();
		}

		storeItemStack.setCount(tag.getInt("count"));
	}

	private TransferApiBridge.SingleStackStorageHandle getInternalStoreStorage(@Nullable Direction direction) {
		// Quick fix to handle null sides. https://github.com/TechReborn/TechReborn/issues/3175
		final Direction side = direction != null ? direction : Direction.DOWN;

		if (internalStoreStorage[side.get3DDataValue()] == null) {
			final Direction boundSide = side;
			internalStoreStorage[side.get3DDataValue()] = TransferApiBridge.createSingleStackStorage(new TransferApiBridge.SingleStackHooks() {
				@Override
				public ItemStack getStack() {
					return storeItemStack;
				}

				@Override
				public void setStack(ItemStack stack) {
					if (stack.isEmpty()) {
						storeItemStack = ItemStack.EMPTY;
					} else {
						storeItemStack = stack;
					}
				}

				@Override
				public int getCapacity(RcItemVariant itemVariant) {
					return maxCapacity - TransferApiBridge.itemVariantMaxStackSize(itemVariant);
				}

				@Override
				public boolean canInsert(RcItemVariant itemVariant) {
					return StorageUnitBaseBlockEntity.this.canPlaceItemThroughFace(INPUT_SLOT, itemVariant.toStack(1), boundSide);
				}

				@Override
				public boolean canExtract(RcItemVariant itemVariant) {
					return StorageUnitBaseBlockEntity.this.canTakeItemThroughFace(OUTPUT_SLOT, itemVariant.toStack(1), boundSide);
				}

				@Override
				public void onFinalCommit() {
					inventory.setHashChanged();
				}
			});
		}
		return internalStoreStorage[side.get3DDataValue()];
	}

	public RcStorage<RcItemVariant> getExposedStorage(Direction side) {
		return TransferApiBridge.combineSlottedItemStorages(List.of(
				getInternalStoreStorage(side),
				TransferApiBridge.inventoryStorageOf(this, side)
		));
	}
}
