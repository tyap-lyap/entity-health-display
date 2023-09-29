package pinkgoosik.entityhealthdisplay.mixin;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pinkgoosik.entityhealthdisplay.api.HealthDisplayEvents;
import pinkgoosik.entityhealthdisplay.extension.HealthDisplayedEntity;
import pinkgoosik.entityhealthdisplay.extension.Ageable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements HealthDisplayedEntity {
	public ElementHolder healthDisplayHolder;

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER))
	void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		summonHitDisplay(source, amount);
		this.updateHealthDisplay();
	}

	@Inject(method = "heal", at = @At("TAIL"))
	void heal(float amount, CallbackInfo ci) {
		this.updateHealthDisplay();
	}

	void summonHitDisplay(DamageSource source, float amount) {
		LivingEntity entity = LivingEntity.class.cast(this);
		if (getWorld() instanceof ServerWorld world && HealthDisplayEvents.SHOULD_DISPLAY_HEALTH.invoker().shouldDisplay(entity, world)) {
			if (source.getAttacker() instanceof ServerPlayerEntity player) {
				if (source.getType().equals(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).get(DamageTypes.ARROW))) {
					Text text = HealthDisplayEvents.FORMAT_HIT_DISPLAY.invoker().getText(entity, world, source, amount);
					if (!text.getString().isBlank()) {
						player.networkHandler.sendPacket(new TitleS2CPacket(Text.of("")));
						player.networkHandler.sendPacket(new SubtitleS2CPacket(text));
						player.networkHandler.sendPacket(new TitleFadeS2CPacket(1, 20, 1));
					}
				} else {
					Text text = HealthDisplayEvents.FORMAT_HIT_DISPLAY.invoker().getText(entity, world, source, amount);
					if (!text.getString().isBlank()) {
						double x = (player.getX() + entity.getX()) / 2.0D;
						double y = entity.getY() + (entity.getHeight() / 2) + entity.getRandom().nextDouble() / 2;
						double z = (player.getZ() + entity.getZ()) / 2.0D;
						DisplayEntity.TextDisplayEntity hit = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
						hit.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
						hit.setText(text);
						hit.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
						((Ageable)hit).setMaxAge(30);
						world.spawnEntity(hit);
					}
				}
			} else {
				Text text = HealthDisplayEvents.FORMAT_HIT_DISPLAY.invoker().getText(entity, world, source, amount);
				if (!text.getString().isBlank()) {
					double x = entity.getX() + entity.getRandom().nextDouble() * 2.0D - 1.0D;
					double y = entity.getY() + (entity.getHeight() / 2) + entity.getRandom().nextDouble() / 2;
					double z = entity.getZ() + entity.getRandom().nextDouble() * 2.0D - 1.0D;
					DisplayEntity.TextDisplayEntity hit = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
					hit.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
					hit.setText(text);
					hit.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
					((Ageable)hit).setMaxAge(30);
					world.spawnEntity(hit);
				}
			}
		}
	}

	@Override
	public void updateHealthDisplay() {
		LivingEntity entity = LivingEntity.class.cast(this);
		if (getWorld() instanceof ServerWorld world) {
			if (HealthDisplayEvents.SHOULD_DISPLAY_HEALTH.invoker().shouldDisplay(entity, world)) {
				Text healthText = HealthDisplayEvents.FORMAT_HEALTH_DISPLAY.invoker().getText(entity, world);
				if (healthText != null && !healthText.getString().isBlank()) {
					this.setHealthDisplayText(healthText);
				}
			}
			else {
				discardHealthDisplay();
			}
		}
	}

	@Override
	public void discardHealthDisplay() {
		if(this.healthDisplayHolder != null) {
			this.healthDisplayHolder.destroy();
			this.healthDisplayHolder = null;
		}
	}

	@Override
	public ElementHolder getHealthDisplayHolder() {
		if(this.healthDisplayHolder == null) initHealthDisplay();
		return this.healthDisplayHolder;
	}

	@Override
	public void initHealthDisplay() {
		LivingEntity entity = LivingEntity.class.cast(this);
		if (getWorld() instanceof ServerWorld world && HealthDisplayEvents.SHOULD_DISPLAY_HEALTH.invoker().shouldDisplay(entity, world)) {
			Text healthText = HealthDisplayEvents.FORMAT_HEALTH_DISPLAY.invoker().getText(entity, world);
			if (healthText != null && !healthText.getString().isBlank()) {
				ElementHolder holder = new ElementHolder();

				ItemDisplayElement element = new ItemDisplayElement();
				element.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
				element.setCustomName(healthText);
				element.setCustomNameVisible(true);

				holder.addElement(element);
				EntityAttachment.of(holder, entity);
				VirtualEntityUtils.addVirtualPassenger(entity, element.getEntityId());
				this.healthDisplayHolder = holder;
			}
		}
	}

	@Override
	public void setHealthDisplayText(Text text) {
		ElementHolder holder = getHealthDisplayHolder();
		if(holder != null) {
			holder.getElements().forEach(element -> {
				if(element instanceof ItemDisplayElement ide) {
					ide.setCustomName(text);
					ide.setCustomNameVisible(true);
					ide.tick();
				}
			});
		}
	}
}

