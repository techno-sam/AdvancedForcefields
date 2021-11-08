#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

src = pygame.image.load("/home/sam/MinecraftForge/Reference/Vanilla_Assets_And_Data/assets/minecraft/textures/block/end_stone.png")#input("Src: "))
cobble = pygame.image.load("/home/sam/MinecraftForge/Reference/Vanilla_Assets_And_Data/assets/minecraft/textures/block/cobblestone.png")
hue_dest = pygame.Surface((src.get_width(),src.get_height()))
grey_dest = pygame.Surface((src.get_width(),src.get_height()))
shift_dest = pygame.Surface((src.get_width(),src.get_height()))
div_dest = pygame.Surface((src.get_width(),src.get_height()))

OFFX = 3
OFFY = -8

OFFX = -8
OFFY = 3

def convert_single(x):
    return int(255-x)

def convert_triple(x):
    return (convert_single(x[0]),convert_single(x[1]),convert_single(x[2]))

def divide_tup(tup1,tup2):
    ret = []
    if len(tup1)!=len(tup2):
        raise Exception("Please us same length for both inputs")
    
    for i in range(len(tup1)):
        ret.append(int(tup1[i]/tup2[i]))
    for i in range(len(ret)):
        v = ret[i]
        if v<0:
            v = 0
        if v>255:
            v = 255
        ret[i]=v
    return tuple(ret)

def print_differences(real_rgb, real_hsv, goal_rgb, goal_hsv):
    print(f"\nGot:\n\tRGB: {real_rgb}\n\tHSV: {real_hsv}\nExpected:\n\tRGB: {goal_rgb}\n\tHSV: {goal_hsv}")

for x in range(src.get_width()):
    for y in range(src.get_height()):
        src_x = (x+OFFX)%16
        src_y = (y+OFFY)%16
        color = src.get_at((src_x,src_y))
        hsv = colorsys.rgb_to_hsv(color[0],color[1],color[2])
        
        hue_color = colorsys.hsv_to_rgb(hsv[0],1,255)
        hue_color = (int(hue_color[0]),int(hue_color[1]),int(hue_color[2]))
        hue_dest.set_at((x,y),hue_color)


        goal_color = cobble.get_at((x,y))
        goal_hsv = colorsys.rgb_to_hsv(goal_color[0],goal_color[1],goal_color[2])
        
        
        grey_color = colorsys.hsv_to_rgb(0,0,hsv[2])
        grey_color = (int(grey_color[0]),int(grey_color[1]),int(grey_color[2]))
        #grey_color = convert_triple(grey_color)
        grey_hsv = colorsys.rgb_to_hsv(grey_color[0],grey_color[1],grey_color[2])
        #print_differences(grey_color,grey_hsv,goal_color,goal_hsv)

        grey_dest.set_at((x,y),grey_color)

        div_dest.set_at((x,y),divide_tup(color,goal_color))

        shift_dest.set_at((x,y),color)

pygame.image.save(div_dest, "/home/sam/MinecraftForge/AdvancedForcefields/tools/test_div.png")
pygame.image.save(shift_dest, "/home/sam/MinecraftForge/AdvancedForcefields/tools/test_shift.png")
pygame.image.save(grey_dest, "/home/sam/MinecraftForge/AdvancedForcefields/tools/test_grey.png")#input("Dest: "))
pygame.image.save(hue_dest, "/home/sam/MinecraftForge/AdvancedForcefields/tools/test_hue.png")#input("Dest: "))
