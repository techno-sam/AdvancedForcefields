#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

hue = float(input("Hue (0-1): "))
src = pygame.image.load("/home/sam/MinecraftForge/Reference/Vanilla_Assets_And_Data/assets/minecraft/textures/item/gold_ingot.png")
dest = pygame.Surface((src.get_width(),src.get_height()))

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

for x in range(src.get_width()):
    for y in range(src.get_height()):
        c = src.get_at((x,y))
        hsv = colorsys.rgb_to_hsv(c[0], c[1], c[2])
        color = colorsys.hsv_to_rgb(hsv[0]+hue, hsv[1], hsv[2])
        #print(f"rgb {c}, hsv {hsv}, color {color}")
        dest.set_at((x,y),color)

pygame.image.save(dest, input("Dest: "))
