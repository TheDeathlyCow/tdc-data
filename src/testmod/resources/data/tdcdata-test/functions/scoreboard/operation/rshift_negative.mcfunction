# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set RShiftNegativePlayer TestOperationObjective -100

scoreboard players operation RShiftNegativePlayer TestOperationObjective >>= #2 TestConsts

execute if score RShiftNegativePlayer TestOperationObjective matches -25 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each