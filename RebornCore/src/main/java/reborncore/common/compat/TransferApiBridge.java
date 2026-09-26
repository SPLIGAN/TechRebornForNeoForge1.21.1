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

import com.google.common.primitives.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import reborncore.common.transfer.MachineSlotsItemAccess;
import reborncore.common.transfer.RcCombinedItemStorage;
import reborncore.common.transfer.RcFluidHandlerBackedStorage;
import reborncore.common.transfer.RcFluidVariant;
import reborncore.common.transfer.RcItemHandlerSlotStorage;
import reborncore.common.transfer.RcItemVariant;
import reborncore.common.transfer.RcNeoTransactionBridge;
import reborncore.common.transfer.RcSingleStackItemStorage;
import reborncore.common.transfer.RcStorage;
import reborncore.common.transfer.RcStorageView;
import reborncore.common.transfer.RcTankResourceHandler;
import reborncore.common.transfer.RcTransaction;
import reborncore.common.transfer.RcTransactionContext;
import reborncore.common.transfer.RcTransferConstants;
import reborncore.common.util.Tank;
import reborncore.common.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * NeoForge-native transfer helpers over {@link Capabilities.Item} / {@link Capabilities.Fluid} {@link ResourceHandler}s.
 * {@link RcStorage} wrappers join RebornCore transactions, so simulated or aborted moves never change inventories.
 */
public final class TransferApiBridge {
	private static final long DROPLETS_PER_MB = RcTransferConstants.DROPLETS_PER_BUCKET / 1000;
	private static final RcFluidHandlerBackedStorage EMPTY_FLUID_STORAGE = new RcFluidHandlerBackedStorage(EmptyResourceHandler.instance());

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
		ResourceHandler<ItemResource> handler = world.getCapability(Capabilities.Item.BLOCK, pos, direction);
		return wrapItemHandler(handler != null ? handler : EmptyResourceHandler.instance());
	}

	public static RcStorage<RcFluidVariant> findFluidStorage(Level world, BlockPos pos, Direction direction) {
		ResourceHandler<FluidResource> handler = world.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
		return handler == null ? EMPTY_FLUID_STORAGE : new RcFluidHandlerBackedStorage(handler);
	}

	public static long fluidConstantsBucket() {
		return RcTransferConstants.DROPLETS_PER_BUCKET;
	}

	/**
	 * Runs {@code body} in a NeoForge transaction that joins {@code transaction} (or commits immediately if it is null).
	 */
	private static int inNeoTransaction(@Nullable RcTransactionContext transaction, ToIntFunction<Transaction> body) {
		if (transaction != null) {
			return body.applyAsInt(RcNeoTransactionBridge.openNeoBoundTo(transaction));
		}
		try (Transaction neoTransaction = RcNeoTransactionBridge.openNeoBoundTo(null)) {
			int result = body.applyAsInt(neoTransaction);
			neoTransaction.commit();
			return result;
		}
	}

	/** Amounts are in droplets; only whole millibuckets are moved. */
	public static long moveFluids(@Nullable RcStorage<RcFluidVariant> from, @Nullable RcStorage<RcFluidVariant> to, Predicate<RcFluidVariant> filter, long maxAmount, @Nullable RcTransactionContext transaction) {
		if (from == null || to == null || maxAmount < DROPLETS_PER_MB) {
			return 0;
		}
		ResourceHandler<FluidResource> fromHandler = toFluidResourceHandler(from);
		ResourceHandler<FluidResource> toHandler = toFluidResourceHandler(to);
		if (fromHandler == null || toHandler == null) {
			return 0;
		}
		int mb = Ints.saturatedCast(maxAmount / DROPLETS_PER_MB);
		int moved = inNeoTransaction(transaction, tx -> ResourceHandlerUtil.move(fromHandler, toHandler, resource -> filter.test(RcFluidVariant.of(resource.getFluid())), mb, tx));
		return moved * DROPLETS_PER_MB;
	}

	public static long moveItems(@Nullable RcStorage<RcItemVariant> from, @Nullable RcStorage<RcItemVariant> to, Predicate<RcItemVariant> filter, long maxAmount, @Nullable RcTransactionContext transaction) {
		if (from == null || to == null || maxAmount == 0) {
			return 0;
		}
		ResourceHandler<ItemResource> fromHandler = toItemResourceHandler(from);
		ResourceHandler<ItemResource> toHandler = toItemResourceHandler(to);
		if (fromHandler != null && toHandler != null) {
			int amount = Ints.saturatedCast(maxAmount);
			return inNeoTransaction(transaction, tx -> ResourceHandlerUtil.move(fromHandler, toHandler, resource -> filter.test(RcItemVariant.of(resource.toStack())), amount, tx));
		}
		return moveItemsViaStorageViews(from, to, filter, maxAmount, transaction);
	}

	private static long moveItemsViaStorageViews(RcStorage<RcItemVariant> from, RcStorage<RcItemVariant> to, Predicate<RcItemVariant> filter, long maxAmount, @Nullable RcTransactionContext transaction) {
		long transferred = 0;
		try (RcTransaction moveTx = RcTransaction.openNested(transaction)) {
			for (var view : from.views()) {
				RcItemVariant resource = view.getResource();
				if (view.isResourceBlank() || !filter.test(resource)) {
					continue;
				}
				long maxExtracted;
				try (RcTransaction extractionTest = moveTx.openNested()) {
					maxExtracted = view.extract(resource, maxAmount - transferred, extractionTest);
				}
				if (maxExtracted == 0) {
					continue;
				}
				try (RcTransaction slotTx = moveTx.openNested()) {
					long inserted = to.insert(resource, maxExtracted, slotTx);
					if (inserted > 0 && view.extract(resource, inserted, slotTx) == inserted) {
						slotTx.commit();
						transferred += inserted;
					}
				}
				if (transferred >= maxAmount) {
					break;
				}
			}
			moveTx.commit();
		}
		return transferred;
	}

	private static @Nullable ResourceHandler<FluidResource> toFluidResourceHandler(RcStorage<RcFluidVariant> storage) {
		if (storage instanceof Tank tank) {
			return new RcTankResourceHandler(() -> tank);
		}
		if (storage instanceof RcFluidHandlerBackedStorage wrapped) {
			return wrapped.handler();
		}
		return null;
	}

	private static @Nullable ResourceHandler<ItemResource> toItemResourceHandler(RcStorage<RcItemVariant> storage) {
		if (storage instanceof RcItemHandlerWrapper wrapper) {
			return wrapper.handler();
		}
		if (storage instanceof RcItemHandlerSlotStorage slot) {
			return RangedResourceHandler.ofSingleIndex(slot.handler(), slot.slot());
		}
		return null;
	}

	/**
	 * Direct (side-less) access for the owner's own logic uses {@link VanillaContainerWrapper}; sided access honours
	 * {@link WorldlyContainer} face rules.
	 */
	private static ResourceHandler<ItemResource> containerHandler(Container inventory, @Nullable Direction direction) {
		if (direction != null && inventory instanceof WorldlyContainer worldly) {
			return new WorldlyContainerWrapper(worldly, direction);
		}
		return VanillaContainerWrapper.of(inventory);
	}

	private record RcItemHandlerWrapper(ResourceHandler<ItemResource> handler) implements RcStorage<RcItemVariant> {
		@Override
		public RcItemVariant getResource() {
			for (int i = 0; i < handler.size(); i++) {
				ItemResource resource = handler.getResource(i);
				if (!resource.isEmpty()) {
					return RcItemVariant.of(resource.toStack());
				}
			}
			return RcItemVariant.of(ItemStack.EMPTY);
		}

		@Override
		public long getAmount() {
			long sum = 0;
			for (int i = 0; i < handler.size(); i++) {
				sum += handler.getAmountAsLong(i);
			}
			return sum;
		}

		@Override
		public boolean isResourceBlank() {
			return getAmount() == 0;
		}

		@Override
		public long insert(RcItemVariant resource, long maxAmount, @Nullable RcTransactionContext tx) {
			if (resource.isBlank() || maxAmount <= 0) {
				return 0;
			}
			ItemResource item = ItemResource.of(resource.toStack(1));
			int amount = Ints.saturatedCast(maxAmount);
			return inNeoTransaction(tx, neoTx -> ResourceHandlerUtil.insertStacking(handler, item, amount, neoTx));
		}

		@Override
		public long extract(RcItemVariant resource, long maxAmount, @Nullable RcTransactionContext tx) {
			if (resource.isBlank() || maxAmount <= 0) {
				return 0;
			}
			ItemResource item = ItemResource.of(resource.toStack(1));
			int amount = Ints.saturatedCast(maxAmount);
			return inNeoTransaction(tx, neoTx -> handler.extract(item, amount, neoTx));
		}

		@Override
		public Iterable<RcStorageView<RcItemVariant>> views() {
			List<RcStorageView<RcItemVariant>> list = new ArrayList<>();
			for (int slot = 0; slot < handler.size(); slot++) {
				list.add(new RcItemHandlerSlotStorage(handler, slot));
			}
			return list;
		}
	}

	private static RcStorage<RcItemVariant> wrapItemHandler(ResourceHandler<ItemResource> handler) {
		return new RcItemHandlerWrapper(handler);
	}

	public static RcStorage<RcItemVariant> playerInventoryStorage(Player player) {
		return wrapItemHandler(PlayerInventoryWrapper.of(player));
	}

	@Nullable
	public static RcStorage<RcItemVariant> playerInventorySlotMatchingStack(Player player, ItemStack stack) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i) == stack) {
				return new RcItemHandlerSlotStorage(PlayerInventoryWrapper.of(player), i);
			}
		}
		return null;
	}

	/**
	 * Fluid view of the container in {@code inputSlot}; filled/emptied containers are moved to {@code outputSlot}.
	 */
	public static RcStorage<RcFluidVariant> fluidStorageConnectingInventorySlots(Container inventory, int inputSlot, int outputSlot) {
		if (inventory.getItem(inputSlot).isEmpty()) {
			return EMPTY_FLUID_STORAGE;
		}
		ResourceHandler<FluidResource> handler = new MachineSlotsItemAccess(inventory, inputSlot, outputSlot)
			.oneByOne()
			.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? EMPTY_FLUID_STORAGE : new RcFluidHandlerBackedStorage(handler);
	}

	@Nullable
	public static RcStorage<RcFluidVariant> fluidItemStorageNullable(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copyWithCount(1))
			.oneByOne()
			.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? null : new RcFluidHandlerBackedStorage(handler);
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
		return fluidStorage != null && fluidStorage.isResourceBlank();
	}

	public static boolean fluidItemStorageMatchesFluid(ItemStack stack, Predicate<net.minecraft.world.level.material.Fluid> predicate) {
		RcStorage<RcFluidVariant> fluidStorage = fluidItemStorageNullable(stack);
		if (fluidStorage == null || fluidStorage.isResourceBlank()) {
			return false;
		}
		return predicate.test(fluidStorage.getResource().fluid());
	}

	public static boolean drainFluidStorageCompletelyCommitted(RcStorage<RcFluidVariant> itemStorage) {
		if (!(itemStorage instanceof RcFluidHandlerBackedStorage wrapped)) {
			return false;
		}
		ResourceHandler<FluidResource> handler = wrapped.handler();
		return inNeoTransaction(null, tx -> {
			int drained = 0;
			for (int i = 0; i < handler.size(); i++) {
				FluidResource resource = handler.getResource(i);
				if (!resource.isEmpty()) {
					drained += handler.extract(i, resource, Integer.MAX_VALUE, tx);
				}
			}
			return drained;
		}) > 0;
	}

	public static RcItemVariant itemVariantOf(ItemStack stack) {
		return RcItemVariant.of(stack);
	}

	public static RcStorage<RcItemVariant> inventorySlot(Container inventory, @Nullable Direction direction, int slotIndex) {
		return new RcItemHandlerSlotStorage(containerHandler(inventory, direction), slotIndex);
	}

	public static long insertItemStackedIntoInventory(RcStorage<RcItemVariant> inventory, RcItemVariant variant, long maxAmount) {
		return insertVariantCommitted(inventory, variant, maxAmount);
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

	public static RcStorage<RcItemVariant> inventoryStorageOf(Container inventory, @Nullable Direction direction) {
		return wrapItemHandler(containerHandler(inventory, direction));
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
