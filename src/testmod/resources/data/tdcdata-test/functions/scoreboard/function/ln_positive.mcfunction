# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set LnPositiveFuncPlayer TestFunctionObjective 100

scoreboard players tdcdata.function ln LnPositiveFuncPlayer TestFunctionObjective

execute if score LnPositiveFuncPlayer TestFunctionObjective matches 4 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each