package pinkgoosik.entityhealthdisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pinkgoosik.entityhealthdisplay.extension.HealthDisplayedEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityHealthDisplay implements ModInitializer {
	public static final String MOD_ID = "entity-health-display";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ArrayList<EntityType<?>> blacklistedEntities = new ArrayList<>(List.of(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN));

	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof LivingEntity living && living instanceof HealthDisplayedEntity ex) {
				ex.initHealthDisplay();
			}
		});
	}
}
