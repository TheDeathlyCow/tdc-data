# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set RShiftNegativePlayer TestObjective -100

scoreboard players operation RShiftNegativePlayer TestObjective >>= #2 TestConsts

execute if score RShiftNegativePlayer TestObjective matches -25 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each