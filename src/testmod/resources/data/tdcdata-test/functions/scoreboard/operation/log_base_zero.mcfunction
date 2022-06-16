# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set LogBZeroPlayer TestOperationObjective 9

scoreboard players operation LogBZeroPlayer TestOperationObjective log_b #0 TestConsts

# Checking that the operation did not execute because input is negative
execute if score LogBZeroPlayer TestOperationObjective matches 9 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each