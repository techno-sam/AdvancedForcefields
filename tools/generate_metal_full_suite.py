#!/usr/bin/python3
import pygame
import colorsys

pygame.init()



def div_tup(tup, x):
    ret = []
    for v in tup:
        ret.append(v/x)
    return ret

def mult_tup(tup, x):
    ret = []
    for v in tup:
        ret.append(v*x)
    return ret

def cut(LIST,MAX,MIN):
    ret = []
    for l in LIST:
        ret.append(min(max(int(l),MIN),MAX))
    return ret

def cutsingle(INT,MAX,MIN):
    return min(max(INT,MIN),MAX)

def convert(infile, outfile, hue, saturate):
    src = pygame.image.load(infile)
    dest = pygame.Surface((src.get_width(),src.get_height()), pygame.SRCALPHA)
    for x in range(src.get_width()):
        for y in range(src.get_height()):
            c = src.get_at((x,y))
            hsv = colorsys.rgb_to_hsv(c[0], c[1], c[2])
            hsv = (0, hsv[1], hsv[2])
            color = colorsys.hsv_to_rgb(hsv[0]+hue, hsv[1]+saturate, hsv[2])
            color = (color[0], color[1], color[2], c[3])
            #print(f"rgb {c}, hsv {hsv}, color {color}")
            dest.set_at((x,y),cut(color, 255, 0))
    pygame.image.save(dest, outfile)

def convert_with_darken(infile, outfile, hue, saturate, darken):
    src = pygame.image.load(infile)
    dest = pygame.Surface((src.get_width(),src.get_height()), pygame.SRCALPHA)
    for x in range(src.get_width()):
        for y in range(src.get_height()):
            c = src.get_at((x,y))
            hsv = colorsys.rgb_to_hsv(c[0], c[1], c[2])
            hsv = (0, hsv[1], hsv[2])
            color = colorsys.hsv_to_rgb(hsv[0]+hue, hsv[1]+saturate, hsv[2]-(darken*255))
            color = (color[0], color[1], color[2], c[3])
            #print(f"rgb {c}, hsv {hsv}, color {color}")
            dest.set_at((x,y),cut(color, 255, 0))
    pygame.image.save(dest, outfile)

'''
#Enderite values
h = 0.4525
s = 0.605
ore_stone = "end_stone"
name = "enderite"
'''
#Enderite values
h = 0.4525
s = 0.605
d = 0.45
ore_stone = "end_stone"
name = "enderite"

def convert_item(original_name):
    convert(
        "/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_src/"+original_name+".png",
        "/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_dest/"+original_name.replace("netherite", name)+".png",
        h,
        s
        )

def convert_item_with_darken(original_name):
    convert_with_darken(
        "/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_src/"+original_name+".png",
        "/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_dest/"+original_name.replace("netherite", name)+".png",
        h,
        s,
        d
        )


convert_item("netherite_ingot")
convert_item("netherite_block")
convert_item_with_darken("netherite_overlay")
convert_item("netherite_bars")
convert_item("netherite_door_bottom")
convert_item("netherite_door_top")
convert_item("netherite_trapdoor")
#Armor
convert_item("netherite_helmet")
convert_item("netherite_chestplate")
convert_item("netherite_leggings")
convert_item("netherite_boots")
#Tools
convert_item("netherite_axe")
convert_item("netherite_hoe")
convert_item("netherite_pickaxe")
convert_item("netherite_shovel")
convert_item("netherite_sword")
#Other items
convert_item("netherite_nugget")

#Finish ore
ore_base = pygame.image.load("/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_src/"+ore_stone+".png")
ore_overlay = pygame.image.load("/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_dest/"+name+"_overlay.png")
ore_base.blit(ore_overlay, (0,0))
pygame.image.save(ore_base, "/home/sam/MinecraftForge/AdvancedForcefields/tools/metal_dest/"+name+"_ore.png")

pygame.quit()
