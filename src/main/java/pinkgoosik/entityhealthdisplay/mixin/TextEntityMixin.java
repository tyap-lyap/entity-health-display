package pinkgoosik.entityhealthdisplay.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pinkgoosik.entityhealthdisplay.extension.Ageable;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextEntityMixin extends DisplayEntity implements Ageable {
	int age = 0;
	int maxAge = 40;
	boolean ageable = false;

	public TextEntityMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		this.ageable = true;
	}

	@Override
	public int getMaxAge() {
		return maxAge;
	}

	@Override
	public boolean isAgeable() {
		return ageable;
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if(nbt.contains("ageable")) {
			this.ageable = nbt.getBoolean("ageable");
			this.maxAge = nbt.getInt("maxAge");
			this.age = nbt.getInt("age");
		}
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		if(this.ageable) {
			nbt.putBoolean("ageable", true);
			nbt.putInt("maxAge", this.maxAge);
			nbt.putInt("age", this.age);
		}
	}
}

