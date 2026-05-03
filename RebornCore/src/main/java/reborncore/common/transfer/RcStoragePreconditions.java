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

public final class RcStoragePreconditions {
	private RcStoragePreconditions() {
	}

	public static void notNegative(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("Amount must be non-negative: " + value);
		}
	}

	public static void notBlankNotNegative(RcFluidVariant variant, long amount) {
		notNegative(amount);
		if (variant.isBlank()) {
			throw new IllegalArgumentException("Fluid resource may not be blank");
		}
	}

	public static void notBlankNotNegative(RcItemVariant variant, long amount) {
		notNegative(amount);
		if (variant.isBlank()) {
			throw new IllegalArgumentException("Item resource may not be blank");
		}
	}
}
