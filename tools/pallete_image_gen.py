#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

src1 = pygame.image.load(input("Src1: "))
output = input("Output: ")

OFFX = 3
OFFY = -8

OFFX = -8
OFFY = 3

OFFX = 0
OFFY = 0

colormap = {}

def rgb_hsv(color):
    return colorsys.rgb_to_hsv(color[0],color[1],color[2])

for x in range(src1.get_width()):
    for y in range(src1.get_height()):
        src_x = (x+OFFX)%src1.get_width()
        src_y = (y+OFFY)%src1.get_height()
        color1 = tuple(src1.get_at((src_x,src_y)))[:3]
        value = rgb_hsv(color1)[2]

        if not value in colormap.keys():
            colormap[value] = color1

print(f"RGB: {colormap}")

dest = pygame.Surface((len(colormap),1))

x = 0

for i in range(min(colormap.keys()), max(colormap.keys())):
    try:
        color = colormap[i]
        dest.set_at((x,0),color)
        x += 1
    except KeyError:
        pass

pygame.image.save(dest, output)
