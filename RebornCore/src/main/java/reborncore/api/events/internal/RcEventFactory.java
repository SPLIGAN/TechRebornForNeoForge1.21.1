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

package reborncore.api.events.internal;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class RcEventFactory {
	private RcEventFactory() {
	}

	public static <T> RcEvent<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
		return new ArrayBackedRcEvent<>(type, invokerFactory);
	}

	private static final class ArrayBackedRcEvent<T> implements RcEvent<T> {
		private final Class<T> type;
		private final Function<T[], T> invokerFactory;
		private final List<T> listeners = new CopyOnWriteArrayList<>();
		private volatile T invoker;

		private ArrayBackedRcEvent(Class<T> type, Function<T[], T> invokerFactory) {
			this.type = type;
			this.invokerFactory = invokerFactory;
			this.invoker = invokerFactory.apply(newArray(0));
		}

		@Override
		public void register(T listener) {
			listeners.add(listener);
			invoker = invokerFactory.apply(listeners.toArray(newArray(listeners.size())));
		}

		@Override
		public T invoker() {
			return invoker;
		}

		@SuppressWarnings("unchecked")
		private T[] newArray(int size) {
			return (T[]) Array.newInstance(type, size);
		}
	}
}
