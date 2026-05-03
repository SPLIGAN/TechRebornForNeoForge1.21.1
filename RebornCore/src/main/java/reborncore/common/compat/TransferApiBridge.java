/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2026 TeamReborn
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

package reborncore.common.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import reborncore.common.transfer.ConnectingInventoryFluidHandler;
import reborncore.common.transfer.RcCombinedItemStorage;
import reborncore.common.transfer.RcFluidAmounts;
import reborncore.common.transfer.RcFluidHandlerBackedStorage;
import reborncore.common.transfer.RcFluidVariant;
import reborncore.common.transfer.RcItemHandlerSlotStorage;
import reborncore.common.transfer.RcItemVariant;
import reborncore.common.transfer.RcSingleStackItemStorage;
import reborncore.common.transfer.RcStorage;
import reborncore.common.transfer.RcTransaction;
import reborncore.common.transfer.RcTransactionContext;
import reborncore.common.transfer.RcTransferConstants;
import reborncore.common.transfer.TankFluidHandler;
import reborncore.common.util.Tank;
import reborncore.common.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * NeoForge-native transfer helpers only: {@link Capabilities.ItemHandler}, {@link IFluidHandler}, {@link FluidUtil}, {@link ItemHandlerHelper}.
 * {@link RcStorage} wrappers preserve Tech Reborn transaction semantics without Fabric Transfer API types or classpath dependency.
 */
public final class TransferApiBridge {
	private static final RcFluidHandlerBackedStorage EMPTY_FLUID_STORAGE = new RcFluidHandlerBackedStorage(new IFluidHandler() {
		@Override
		public int getTanks() {
			return 0;
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return 0;
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return false;
		}

		@Override
		public int fill(FluidStack resource, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
			return FluidStack.EMPTY;
		}
	});

	private TransferApiBridge() {
	}

	public static void registerFluidFallback(@SuppressWarnings("unused") Object unused) {
		// Legacy entrypoint: fluid capability registration moved to NeoForge RegisterCapabilitiesEvent.
	}

	public static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerItemStorageForBlockEntity(
		@SuppressWarnings("unused") BiFunction<? super T, Direction, ?> provider,
		@SuppressWarnings("unused") BlockEntityType<T> blockEntityType
	) {
		// Legacy entrypoint: item capability registration moved to NeoForge RegisterCapabilitiesEvent.
	}

	public static RcStorage<RcItemVariant> findItemStorage(Level world, BlockPos pos, Direction direction) {
		IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
		if (handler == null) {
			return emptyItemStorage();
		}
		return wrapItemHandler(handler);
	}

	public static RcStorage<RcFluidVariant> findFluidStorage(Level world, BlockPos pos, Direction direction) {
		IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
		if (handler == null) {
			return EMPTY_FLUID_STORAGE;
		}
		return new RcFluidHandlerBackedStorage(handler);
	}

	public static long fluidConstantsBucket() {
		return RcTransferConstants.DROPLETS_PER_BUCKET;
	}

	public static long moveFluids(@Nullable RcStorage<RcFluidVariant> from, @Nullable RcStorage<RcFluidVariant> to, Predicate<RcFluidVariant> filter, long maxAmount, @Nullable RcTransactionContext transaction) {
		if (from == null || to == null || maxAmount == 0) {
			return 0;
		}
		if (transaction != null) {
			throw new UnsupportedOperationException("Fluid transfers with an explicit transaction context are not supported");
		}
		IFluidHandler fromHandler = toFluidHandler(from);
		IFluidHandler toHandler = toFluidHandler(to);
		if (fromHandler == null || toHandler == null) {
			return 0;
		}
		FluidStack moved = FluidUtil.tryFluidTransfer(toHandler, fromHandler, RcFluidAmounts.toMilliBucketsClamped(maxAmount), true);
		return RcFluidAmounts.dropletsFromFluidStack(moved);
	}

	public static long moveItems(@Nullable RcStorage<RcItemVariant> from, @Nullable RcStorage<RcItemVariant> to, Predicate<RcItemVariant> filter, long maxAmount, @Nullable RcTransactionContext transaction) {
		if (from == null || to == null || maxAmount == 0) {
			return 0;
		}
		if (transaction != null) {
			throw new UnsupportedOperationException("Item transfers with an explicit transaction context are not supported");
		}
		IItemHandler fromHandler = toItemHandler(from);
		IItemHandler toHandler = toItemHandler(to);
		if (fromHandler == null || toHandler == null) {
			return moveItemsViaStorageViews(from, to, filter, maxAmount);
		}
		return moveItemHandlers(fromHandler, toHandler, filter, maxAmount);
	}

	private static long moveItemsViaStorageViews(RcStorage<RcItemVariant> from, RcStorage<RcItemVariant> to, Predicate<RcItemVariant> filter, long maxAmount) {
		long transferred = 0;
		for (var view : from.views()) {
			RcItemVariant resource = view.getResource();
			if (view.isResourceBlank() || !filter.test(resource)) {
				continue;
			}
			while (transferred < maxAmount) {
				long remaining = maxAmount - transferred;
				long maxExtracted;
				try (RcTransaction extractionTest = RcTransaction.openNested(null)) {
					maxExtracted = view.extract(resource, remaining, extractionTest);
				}
				if (maxExtracted == 0) {
					break;
				}
				try (RcTransaction moveTx = RcTransaction.openOuter()) {
					long extracted = view.extract(resource, maxExtracted, moveTx);
					if (extracted == 0) {
						break;
					}
					long inserted = to.insert(resource, extracted, moveTx);
					if (inserted == 0) {
						break;
					}
					long leftover = extracted - inserted;
					if (leftover > 0) {
						((RcStorage<RcItemVariant>) view).insert(resource, leftover, moveTx);
					}
					if (inserted == extracted) {
						moveTx.commit();
						transferred += inserted;
					}
				}
			}
		}
		return transferred;
	}

	private static long moveItemHandlers(IItemHandler fromHandler, IItemHandler toHandler, Predicate<RcItemVariant> filter, long maxAmount) {
		long moved = 0;
		for (int i = 0; i < fromHandler.getSlots(); i++) {
			ItemStack slotStack = fromHandler.getStackInSlot(i);
			if (slotStack.isEmpty()) {
				continue;
			}
			if (!filter.test(RcItemVariant.of(slotStack))) {
				continue;
			}
			long remaining = maxAmount - moved;
			if (remaining <= 0) {
				break;
			}
			ItemStack simulated = fromHandler.extractItem(i, (int) Math.min(remaining, Integer.MAX_VALUE), true);
			if (simulated.isEmpty()) {
				continue;
			}
			ItemStack leftoverSim = ItemHandlerHelper.insertItemStacked(toHandler, simulated, true);
			int insertable = simulated.getCount() - leftoverSim.getCount();
			if (insertable <= 0) {
				continue;
			}
			ItemStack extracted = fromHandler.extractItem(i, insertable, false);
			ItemStack leftover = ItemHandlerHelper.insertItemStacked(toHandler, extracted, false);
			moved += insertable - leftover.getCount();
		}
		return moved;
	}

	private static IFluidHandler toFluidHandler(RcStorage<RcFluidVariant> storage) {
		if (storage instanceof Tank tank) {
			return new TankFluidHandler(tank);
		}
		if (storage instanceof RcFluidHandlerBackedStorage wrapped) {
			return wrapped.handler();
		}
		return null;
	}

	private static IItemHandler toItemHandler(RcStorage<RcItemVariant> storage) {
		if (storage instanceof RcItemHandlerWrapper wrapper) {
			return wrapper.handler();
		}
		return null;
	}

	private record RcItemHandlerWrapper(IItemHandler handler) implements RcStorage<RcItemVariant> {
		@Override
		public RcItemVariant getResource() {
			return RcItemVariant.of(ItemStack.EMPTY);
		}

		@Override
		public long getAmount() {
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return true;
		}

		@Override
		public long insert(RcItemVariant resource, long maxAmount, RcTransactionContext tx) {
			return 0;
		}

		@Override
		public long extract(RcItemVariant resource, long maxAmount, RcTransactionContext tx) {
			return 0;
		}

		@Override
		public Iterable<reborncore.common.transfer.RcStorageView<RcItemVariant>> views() {
			List<reborncore.common.transfer.RcStorageView<RcItemVariant>> list = new ArrayList<>();
			for (int s = 0; s < handler.getSlots(); s++) {
				int slot = s;
				list.add(new RcItemHandlerSlotStorage(handler, slot));
			}
			return list;
		}
	}

	private static RcStorage<RcItemVariant> wrapItemHandler(IItemHandler handler) {
		return new RcItemHandlerWrapper(handler);
	}

	private static RcStorage<RcItemVariant> emptyItemStorage() {
		return new RcItemHandlerWrapper(new IItemHandler() {
			@Override
			public int getSlots() {
				return 0;
			}

			@Override
			public ItemStack getStackInSlot(int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				return stack;
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {
				return 0;
			}

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		});
	}

	public static RcStorage<RcItemVariant> playerInventoryStorage(Player player) {
		return wrapItemHandler(new PlayerInvWrapper(player.getInventory()));
	}

	@Nullable
	public static RcStorage<RcItemVariant> playerInventorySlotMatchingStack(Player player, ItemStack stack) {
		RcStorage<RcItemVariant> playerInv = playerInventoryStorage(player);
		IItemHandler handler = toItemHandler(playerInv);
		if (handler == null) {
			return null;
		}
		for (int i = 0; i < handler.getSlots(); i++) {
			if (handler.getStackInSlot(i) == stack) {
				return new RcItemHandlerSlotStorage(handler, i);
			}
		}
		return null;
	}

	public static RcStorage<RcFluidVariant> fluidStorageConnectingInventorySlots(Container inventory, int inputSlot, int outputSlot) {
		return new RcFluidHandlerBackedStorage(new ConnectingInventoryFluidHandler(inventory, inputSlot, outputSlot));
	}

	@Nullable
	public static RcStorage<RcFluidVariant> fluidItemStorageNullable(ItemStack stack) {
		return FluidUtil.getFluidHandler(stack.copyWithCount(1))
			.map(h -> (RcStorage<RcFluidVariant>) new RcFluidHandlerBackedStorage(h))
			.orElse(null);
	}

	public static RcStorage<RcFluidVariant> fluidItemStorageFromStack(ItemStack stack) {
		RcStorage<RcFluidVariant> found = fluidItemStorageNullable(stack);
		return found != null ? found : EMPTY_FLUID_STORAGE;
	}

	public static boolean itemStackProvidesFluidItemStorage(ItemStack stack) {
		return fluidItemStorageNullable(stack) != null;
	}

	public static boolean fluidItemStorageEffectivelyEmpty(ItemStack stack) {
		RcStorage<RcFluidVariant> fluidStorage = fluidItemStorageNullable(stack);
		if (fluidStorage == null) {
			return false;
		}
		try (RcTransaction tx = RcTransaction.openNested(null)) {
			for (var view : fluidStorage.views()) {
				if (!view.isResourceBlank() && view.getAmount() > 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean fluidItemStorageMatchesFluid(ItemStack stack, Predicate<net.minecraft.world.level.material.Fluid> predicate) {
		RcStorage<RcFluidVariant> fluidStorage = fluidItemStorageNullable(stack);
		if (fluidStorage == null) {
			return false;
		}
		try (RcTransaction tx = RcTransaction.openNested(null)) {
			for (var view : fluidStorage.views()) {
				if (!view.isResourceBlank() && view.getAmount() > 0 && predicate.test(view.getResource().fluid())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean drainFluidStorageCompletelyCommitted(RcStorage<RcFluidVariant> itemStorage) {
		if (itemStorage instanceof RcFluidHandlerBackedStorage wrapped) {
			boolean didSomething = false;
			IFluidHandler handler = wrapped.handler();
			for (int i = 0; i < handler.getTanks(); i++) {
				FluidStack fs = handler.getFluidInTank(i);
				if (!fs.isEmpty()) {
					handler.drain(fs, IFluidHandler.FluidAction.EXECUTE);
					didSomething = true;
				}
			}
			return didSomething;
		}
		try (RcTransaction tx = RcTransaction.openOuter()) {
			boolean didSomething = false;
			for (var view : itemStorage.views()) {
				if (view.isResourceBlank()) {
					continue;
				}
				didSomething |= view.extract(view.getResource(), Long.MAX_VALUE, tx) > 0;
			}
			tx.commit();
			return didSomething;
		}
	}

	public static RcItemVariant itemVariantOf(ItemStack stack) {
		return RcItemVariant.of(stack);
	}

	public static RcStorage<RcItemVariant> inventorySlot(Container inventory, @Nullable Direction direction, int slotIndex) {
		IItemHandler sided = new SidedInvWrapper((net.minecraft.world.WorldlyContainer) inventory, direction);
		return new RcItemHandlerSlotStorage(sided, slotIndex);
	}

	public static long insertItemStackedIntoInventory(RcStorage<RcItemVariant> inventory, RcItemVariant variant, long maxAmount) {
		IItemHandler handler = toItemHandler(inventory);
		if (handler != null) {
			ItemStack toInsert = variant.toStack((int) Math.min(maxAmount, Integer.MAX_VALUE));
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, toInsert, false);
			return toInsert.getCount() - remainder.getCount();
		}
		long inserted = 0;
		try (RcTransaction tx = RcTransaction.openOuter()) {
			outer:
			for (int loop = 0; loop < 2; ++loop) {
				for (var view : inventory.views()) {
					if (view.getResource().equals(variant) || loop == 1) {
						inserted += ((RcStorage<RcItemVariant>) view).insert(variant, maxAmount - inserted, tx);
						if (inserted >= maxAmount) {
							break outer;
						}
					}
				}
			}
			tx.commit();
		}
		return inserted;
	}

	public static long insertVariantCommitted(RcStorage<RcItemVariant> storage, RcItemVariant variant, long maxAmount) {
		try (RcTransaction tx = RcTransaction.openOuter()) {
			long inserted = storage.insert(variant, maxAmount, tx);
			if (inserted > 0) {
				tx.commit();
			}
			return inserted;
		}
	}

	@FunctionalInterface
	public interface OuterTxLongBody {
		long run(RcTransaction transaction);
	}

	@FunctionalInterface
	public interface EnergyTransferInOuterTx {
		long transfer(EnergyStorage storage, long maxAmount, RcTransactionContext transaction);
	}

	public static long runOuterCommitted(OuterTxLongBody body) {
		try (RcTransaction transaction = RcTransaction.openOuter()) {
			long result = body.run(transaction);
			transaction.commit();
			return result;
		}
	}

	public static long simulateOuter(OuterTxLongBody body) {
		try (RcTransaction transaction = RcTransaction.openOuter()) {
			return body.run(transaction);
		}
	}

	public interface DynamicCellFluidStorageProvider {
		net.minecraft.world.level.material.Fluid fluidFromItemVariant(RcItemVariant variant);

		ItemStack stackWithFluid(net.minecraft.world.level.material.Fluid fluid);
	}

	public static void registerDynamicCellFluidStorage(Item cellItem, DynamicCellFluidStorageProvider provider) {
		// Registered via NeoForge capabilities on the TechReborn side.
	}

	public abstract static class SingleStackStorageHandle extends RcSingleStackItemStorage {
		protected SingleStackStorageHandle(SingleStackHooks hooks) {
			super(hooks);
		}
	}

	public interface SingleStackHooks {
		ItemStack getStack();

		void setStack(ItemStack stack);

		int getCapacity(RcItemVariant itemVariant);

		boolean canInsert(RcItemVariant itemVariant);

		boolean canExtract(RcItemVariant itemVariant);

		void onFinalCommit();
	}

	public static SingleStackStorageHandle createSingleStackStorage(final SingleStackHooks hooks) {
		return new SingleStackStorageHandle(hooks) {};
	}

	public static RcStorage<RcItemVariant> inventoryStorageOf(Container inventory, Direction direction) {
		return wrapItemHandler(new SidedInvWrapper((net.minecraft.world.WorldlyContainer) inventory, direction));
	}

	public static RcStorage<RcItemVariant> combineSlottedItemStorages(List<RcStorage<RcItemVariant>> storages) {
		return new RcCombinedItemStorage(storages);
	}

	public static int itemVariantMaxStackSize(RcItemVariant variant) {
		if (variant.isBlank()) {
			return variant.item().getDefaultMaxStackSize();
		}
		return variant.prototype().getMaxStackSize();
	}
}
