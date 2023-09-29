package pinkgoosik.entityhealthdisplay.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pinkgoosik.entityhealthdisplay.extension.Ageable;

@Mixin(DisplayEntity.class)
public abstract class DisplayEntityMixin extends Entity {

	public DisplayEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	void tick(CallbackInfo ci) {
		if(this instanceof Ageable display) {
			if (display.isAgeable()) {
				if(display.getAge() == display.getMaxAge()) {
					this.remove(RemovalReason.DISCARDED);
				}
				else {
					display.setAge(display.getAge() + 1);
				}
			}
		}
	}
}
