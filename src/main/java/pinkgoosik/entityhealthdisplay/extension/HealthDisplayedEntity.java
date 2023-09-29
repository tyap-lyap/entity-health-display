package pinkgoosik.entityhealthdisplay.extension;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.text.Text;

public interface HealthDisplayedEntity {
	ElementHolder getHealthDisplayHolder();
	void initHealthDisplay();
	void updateHealthDisplay();
	void setHealthDisplayText(Text text);
	void discardHealthDisplay();
}
