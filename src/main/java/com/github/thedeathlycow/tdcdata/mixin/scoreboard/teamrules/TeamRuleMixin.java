package com.github.thedeathlycow.tdcdata.mixin.scoreboard.teamrules;

import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import net.minecraft.scoreboard.AbstractTeam;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractTeam.class)
public class TeamRuleMixin implements RuledTeam {

    private boolean tdcdata$keepInventory;

    @Override
    public void tdcdata$setKeepInventory(boolean value) {
        this.tdcdata$keepInventory = value;
    }

    @Override
    public boolean tdcdata$shouldKeepInventory() {
        return this.tdcdata$keepInventory;
    }
}
