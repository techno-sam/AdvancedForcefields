#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

src = pygame.image.load(input("Src: "))
dest = pygame.Surface((src.get_width(),src.get_height()))

OFFX = 3
OFFY = -8

OFFX = -8
OFFY = 3

OFFX = 0
OFFY = 0

def convert_single(x):
    return int(255-x)

def convert_triple(x):
    return (convert_single(x[0]),convert_single(x[1]),convert_single(x[2]))

def print_differences(real_rgb, real_hsv, goal_rgb, goal_hsv):
    print(f"\nGot:\n\tRGB: {real_rgb}\n\tHSV: {real_hsv}\nExpected:\n\tRGB: {goal_rgb}\n\tHSV: {goal_hsv}")

for x in range(src.get_width()):
    for y in range(src.get_height()):
        src_x = (x+OFFX)%16
        src_y = (y+OFFY)%16
        color = src.get_at((src_x,src_y))
        hsv = colorsys.rgb_to_hsv(color[0],color[1],color[2])
        
        dest.set_at((x,y),convert_triple(color))

pygame.image.save(dest, input("Dest: "))
