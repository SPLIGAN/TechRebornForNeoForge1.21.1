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

package reborncore.common.transfer;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Links the lifetime of {@link RcTransaction}s to NeoForge {@link Transaction}s and vice versa, so that
 * RebornCore storages can be exposed through NeoForge capabilities (and NeoForge handlers used from RebornCore code)
 * without losing abort/rollback semantics.
 */
public final class RcNeoTransactionBridge {
	private static final ThreadLocal<PendingRcTransactions> PENDING = ThreadLocal.withInitial(PendingRcTransactions::new);

	private RcNeoTransactionBridge() {
	}

	/**
	 * Opens an {@link RcTransaction} that stays open until {@code transaction} is resolved: it is closed without
	 * committing (reverting RebornCore snapshots) if {@code transaction} or one of its parents is aborted, and committed
	 * once the root NeoForge transaction commits.
	 */
	public static RcTransaction openBoundTo(TransactionContext transaction) {
		PendingRcTransactions pending = PENDING.get();
		pending.updateSnapshots(transaction);
		RcTransaction rcTransaction = RcTransaction.openOuter();
		pending.transactions.add(rcTransaction);
		return rcTransaction;
	}

	/**
	 * Opens a NeoForge {@link Transaction} nested in the currently open one (or a root transaction) that is committed
	 * or aborted together with {@code transaction}. With a {@code null} context the caller must commit and close the
	 * returned transaction itself.
	 */
	@SuppressWarnings("deprecation")
	public static Transaction openNeoBoundTo(@Nullable RcTransactionContext transaction) {
		Transaction neoTransaction = Transaction.open(Transaction.getCurrentOpenedTransaction());
		if (transaction != null) {
			transaction.addCloseCallback(committed -> {
				if (committed) {
					neoTransaction.commit();
				} else {
					neoTransaction.close();
				}
			});
		}
		return neoTransaction;
	}

	private static final class PendingRcTransactions extends SnapshotJournal<Integer> {
		private final List<RcTransaction> transactions = new ArrayList<>();

		@Override
		protected Integer createSnapshot() {
			return transactions.size();
		}

		@Override
		protected void revertToSnapshot(Integer snapshot) {
			while (transactions.size() > snapshot) {
				transactions.removeLast().close();
			}
		}

		@Override
		protected void onRootCommit(Integer originalState) {
			List<RcTransaction> committed = new ArrayList<>(transactions);
			transactions.clear();
			for (RcTransaction rcTransaction : committed) {
				rcTransaction.commit();
				rcTransaction.close();
			}
		}
	}
}
