/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common.inventory;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CosmeticCurioSlot extends CurioSlot {

  public CosmeticCurioSlot(PlayerEntity player, IDynamicStackHandler handler, int index,
      String identifier, int xPosition, int yPosition) {
    super(player, handler, index, identifier, xPosition, yPosition, null);
    this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
        new ResourceLocation(Curios.MODID, "item/empty_cosmetic_slot"));
  }

  @Override
  public boolean getRenderStatus() {
    return true;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public String getSlotName() {
    return I18n.format("curios.cosmetic") + " " + super.getSlotName();
  }
}
