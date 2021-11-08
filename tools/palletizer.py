#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

src1 = pygame.image.load(input("Src1: "))
src2 = pygame.image.load(input("Src2: "))
dest = pygame.Surface((src1.get_width(),src1.get_height()))

OFFX = 3
OFFY = -8

OFFX = -8
OFFY = 3

OFFX = 0
OFFY = 0

colormap = {}
hsvcolormap = {}

def rgb_hsv(color):
    return colorsys.rgb_to_hsv(color[0],color[1],color[2])

for x in range(src1.get_width()):
    for y in range(src1.get_height()):
        src_x = (x+OFFX)%src1.get_width()
        src_y = (y+OFFY)%src1.get_height()
        color1 = tuple(src1.get_at((src_x,src_y)))[:3]
        color2 = tuple(src2.get_at((src_x,src_y)))[:3]

        if not color1 in colormap.keys():
            colormap[color1] = color2
            hsvcolormap[rgb_hsv(color1)] = rgb_hsv(color2)

print(f"RGB: {colormap}")
print(f"HSV: {hsvcolormap}")
#pygame.image.save(dest, input("Dest: "))
