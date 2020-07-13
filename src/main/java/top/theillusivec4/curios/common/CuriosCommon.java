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

package top.theillusivec4.curios.common;

import java.util.Map;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.SlotTypeInfo.BuildScheme;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.network.NetworkPackets;
import top.theillusivec4.curios.common.slottype.SlotTypeManager;
import top.theillusivec4.curios.server.CuriosConfig;
import top.theillusivec4.curios.server.SlotHelper;

public class CuriosCommon implements ModInitializer {

  public static final String MODID = CuriosApi.MODID;
  public static final Logger LOGGER = LogManager.getLogger();

  private static final boolean DEBUG = false;

  @Override
  public void onInitialize() {
    CuriosApi.setCuriosHelper(new CuriosHelper());

    if (DEBUG) {

      for (SlotTypePreset value : SlotTypePreset.values()) {
        CuriosApi.enqueueSlotType(BuildScheme.REGISTER, value.getInfoBuilder().cosmetic().build());
      }
    }

    ServerStartCallback.EVENT.register((minecraftServer) -> {
      CuriosApi.setSlotHelper(new SlotHelper());
      SlotTypeManager.buildQueuedSlotTypes();
      CuriosConfig.init();
      SlotTypeManager.buildConfigSlotTypes();
      SlotTypeManager.buildSlotTypes();
    });

    ServerStopCallback.EVENT.register((minecraftServer) -> CuriosApi.setSlotHelper(null));

    UseItemCallback.EVENT.register(((player, world, hand) -> {
      ItemStack stack = player.getStackInHand(hand);
      return CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> {

        if (curio.canRightClickEquip()) {
          return CuriosApi.getCuriosHelper().getCuriosHandler(player).map(handler -> {

            if (!player.world.isClient()) {
              Map<String, ICurioStacksHandler> curios = handler.getCurios();

              for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                IDynamicStackHandler stackHandler = entry.getValue().getStacks();

                for (int i = 0; i < stackHandler.size(); i++) {
                  ItemStack present = stackHandler.getStack(i);

                  if (present.isEmpty() && curio.canEquip(entry.getKey(), player)) {
                    stackHandler.setStack(i, stack.copy());
                    curio.playRightClickEquipSound(player);

                    if (!player.isCreative()) {
                      int count = stack.getCount();
                      stack.decrement(count);
                    }
                    return TypedActionResult.success(stack);
                  }
                }
              }
            }
            return TypedActionResult.success(stack);
          }).orElse(TypedActionResult.pass(stack));
        }
        return TypedActionResult.pass(stack);
      }).orElse(TypedActionResult.pass(stack));
    }));

    EntityComponentCallback.event(PlayerEntity.class).register(
        (playerEntity, componentContainer) -> componentContainer
            .put(CuriosComponent.INVENTORY, new PlayerCuriosComponent(playerEntity)));
    EntityComponents
        .setRespawnCopyStrategy(CuriosComponent.INVENTORY, RespawnCopyStrategy.INVENTORY);

    CuriosRegistry.registerItems();
    CuriosRegistry.registerComponents();
    NetworkPackets.registerPackets();
  }
}
