#
# TimerBlocks ⌛

This plugin features a countdown timer using blocks
that can be modified from within the game

It supports hexadecimal RGB: **<#FF0000>**!

> Plugins Commands Help 🔍!
```
/timer wand
/timer create <TimerName>
/timer delete <TimerName>
/timer start <TimerName>
/timer stop <TimerName>
/timer editor (Change the Timer)
/timer reload (Reload Config.yml)
```

> Preview Timer 🔍!
![Timer1](https://cdn.modrinth.com/data/cached_images/52cb62b23fdccd272d180d59bdee5360e794387a_0.webp)

# How to add more Blocks in your Timer ⚡
```
config.yml
```
```
# TimerPlugin Configuration
# You can change the material used for the timer digits here.
# Use valid Bukkit Material names (e.g., ORANGE_CONCRETE, LIME_WOOL, GOLD_BLOCK)

# Format/Formato:
# - ORANGE_CONCRETE

render-material:
  - ORANGE_CONCRETE
  - RED_CONCRETE
  - WHITE_CONCRETE     <----- HERE!. "add more lines"

########################
#     Finish Rockets   #
########################
#
# Lanzara cohetes al finalizar la cuenta regresiva
# Fireworks will be set off at the end of the countdown

fireworks-on-finish: true # true/false
fireworks-duration: 10 # seconds/segundos

########################
#       ANNOUNCES      #
########################

announce:
  - "<green>Evento terminado"
  - "<#C30000>&lS<#C30000>&le<#C30000>&lm<#CD0000>&li <#E10000>&lA<#EB0000>&ln<#F50000>&la<#FF0000>&lr<#FF0000>&lq<#FF0000>&lu<#FF0000>&li<#FF0000>&lc<#FF0000>&lo"
  - "<gradient:#ff0000:#ffff00>GANADOR</gradient>"
  - "<bold><yellow>TIEMPO FINALIZADO</bold>"
```
