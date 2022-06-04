package com.github.thedeathlycow.tdcdata.mixin.scoreboard.teamrules;

import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.codec.language.bm.Rule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Iterator;

@Mixin(ScoreboardState.class)
public class SerializeRulesMixin {

    @Shadow
    @Final
    private Scoreboard scoreboard;

    @Inject(
            method = "readTeamsNbt",
            at = @At(
                    value = "TAIL"
            )
    )
    private void readRulesFromNbt(NbtList nbt, CallbackInfo ci) {
        for (int i = 0; i < nbt.size(); i++) {
            NbtCompound nbtCompound = nbt.getCompound(i);
            String teamName = nbtCompound.getString("Name");
            Team team = this.scoreboard.getTeam(teamName);
            if (team instanceof RuledTeam ruledTeam && nbtCompound.contains("CustomRules")) {
                NbtCompound customRules = nbtCompound.getCompound("CustomRules");
                ruledTeam.tdcdata$setKeepInventory(customRules.getBoolean("KeepInventory"));
            }
        }
    }

    @Inject(
            method = "teamsToNbt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    @SuppressWarnings("rawtypes")
    private void writeRulesToNbt(CallbackInfoReturnable<NbtList> cir, NbtList nbtList, Collection collection, Iterator var3, Team team, NbtCompound nbtCompound) {
        if (team instanceof RuledTeam ruledTeam) {
            NbtCompound customRules = new NbtCompound();
            customRules.putBoolean("KeepInventory", ruledTeam.tdcdata$shouldKeepInventory());

            nbtCompound.put("CustomRules", customRules);
        }
    }

}
