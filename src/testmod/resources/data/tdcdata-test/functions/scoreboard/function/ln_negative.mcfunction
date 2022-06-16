# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set LnNegativeFuncPlayer TestFunctionObjective -100

scoreboard players tdcdata.function ln LnNegativeFuncPlayer TestFunctionObjective

execute if score LnNegativeFuncPlayer TestFunctionObjective matches -100 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each