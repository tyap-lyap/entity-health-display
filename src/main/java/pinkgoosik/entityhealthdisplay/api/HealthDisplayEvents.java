package pinkgoosik.entityhealthdisplay.api;

import eu.pb4.placeholders.api.TextParserUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class HealthDisplayEvents {

	public static final Event<ShouldDisplayHealth> SHOULD_DISPLAY_HEALTH = EventFactory.createArrayBacked(ShouldDisplayHealth.class, callbacks -> (entity, world) -> {
		for (ShouldDisplayHealth callback : callbacks) {
			boolean shouldDisplayHealth = callback.shouldDisplay(entity, world);
			if (shouldDisplayHealth) return true;
		}
		return false;
	});

	@FunctionalInterface
	public interface ShouldDisplayHealth {
		boolean shouldDisplay(LivingEntity entity, ServerWorld world);
	}

	/**
	 * return null to set to default
	 * or return Text.of("") to prevent hit display
	 */
	public static final Event<FormatHitDisplay> FORMAT_HIT_DISPLAY = EventFactory.createArrayBacked(FormatHitDisplay.class, callbacks -> (entity, world, source, amount) -> {
		for (FormatHitDisplay callback : callbacks) {
			var text = callback.getText(entity, world, source, amount);
			if(text != null) return text;
		}
		return Text.of(String.valueOf((int) amount));
	});

	@FunctionalInterface
	public interface FormatHitDisplay {
		Text getText(LivingEntity entity, ServerWorld world, DamageSource source, float amount);
	}

	/**
	 * return null to set to default
	 * or return Text.of("") to prevent health display
	 */
	public static final Event<FormatHealthDisplay> FORMAT_HEALTH_DISPLAY = EventFactory.createArrayBacked(FormatHealthDisplay.class, callbacks -> (entity, world) -> {
		for (FormatHealthDisplay callback : callbacks) {
			var text = callback.getText(entity, world);
			if(text != null) return text;
		}

		String color = (entity.getMaxHealth() / 2) >= entity.getHealth() ? "<yellow>" : "<green>";
		return TextParserUtils.formatText("<red>" + entity.getName().getString() + " " + color + String.format("%.1f", entity.getHealth()) + "<white>/<green>" + (int)entity.getMaxHealth() + "<red>❤");
	});

	@FunctionalInterface
	public interface FormatHealthDisplay {
		Text getText(LivingEntity entity, ServerWorld world);
	}
}
