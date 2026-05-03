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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class RcTransaction implements RcTransactionContext, AutoCloseable {
	private final @Nullable RcTransaction parent;
	private boolean closed;
	private boolean committed;
	private final List<Consumer<Boolean>> closeCallbacks = new ArrayList<>();

	private RcTransaction(@Nullable RcTransaction parent) {
		this.parent = parent;
	}

	public static RcTransaction openOuter() {
		return new RcTransaction(null);
	}

	public static RcTransaction openNested(@Nullable RcTransactionContext parentContext) {
		if (parentContext instanceof RcTransaction parentTx) {
			return new RcTransaction(parentTx);
		}
		return new RcTransaction(null);
	}

	public void commit() {
		if (closed) {
			throw new IllegalStateException("Transaction already closed");
		}
		this.committed = true;
	}

	public boolean isCommitted() {
		return committed;
	}

	@Override
	public RcTransaction openNested() {
		return openNested(this);
	}

	@Override
	public void addCloseCallback(Consumer<Boolean> committedConsumer) {
		this.closeCallbacks.add(committedConsumer);
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		boolean wasCommitted = committed;
		for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
			closeCallbacks.get(i).accept(wasCommitted);
		}
		closeCallbacks.clear();
	}
}
