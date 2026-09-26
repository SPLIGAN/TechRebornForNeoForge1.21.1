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

package reborncore.common.blockentity;

import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import reborncore.common.RebornCoreConfig;

import java.util.UUID;

public final class MachinePermissions {
	private MachinePermissions() {
	}

	/**
	 * Whether {@code player} may change a machine's slot, fluid or redstone configuration: it must be allowed to
	 * interact at the machine's position ({@link net.minecraft.world.level.Level#mayInteract}: spawn protection, world
	 * border and protection mods hooking it), and be an operator or the machine's owner. Machines without a recorded
	 * owner are operator-only unless {@link RebornCoreConfig#allowOwnerlessMachineConfig} is set.
	 */
	public static boolean canConfigure(Player player, MachineBaseBlockEntity machine) {
		if (machine.getLevel() == null || !machine.getLevel().mayInteract(player, machine.getBlockPos())) {
			return false;
		}
		if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			return true;
		}
		UUID owner = machine.getOwner();
		if (owner == null) {
			return RebornCoreConfig.allowOwnerlessMachineConfig;
		}
		return owner.equals(player.getUUID());
	}
}
