# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set LnZeroFuncPlayer TestFunctionObjective 0

scoreboard players tdcdata.function ln LnZeroFuncPlayer TestFunctionObjective

execute if score LnZeroFuncPlayer TestFunctionObjective matches 0 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each