#!/usr/bin/python3
import pygame
import colorsys

pygame.init()
print("will compute src1/src2")
src1 = pygame.image.load(input("Src1: "))
src2 = pygame.image.load(input("Src2: "))
dest = pygame.Surface((src1.get_width(),src1.get_height()))

OFFX = 3
OFFY = -8

OFFX = -8
OFFY = 3

OFFX = 0
OFFY = 0

def divide_tup(tup1,tup2):
    ret = []
    if len(tup1)!=len(tup2):
        raise Exception("Please us same length for both inputs")
    
    for i in range(len(tup1)):
        result = tup1[i]/tup2[i]
        #print(result)
        ret.append(int(result*75))
    for i in range(len(ret)):
        v = ret[i]
        if v<0:
            print("LESS THAN 0, CUTTING")
            v = 0
        if v>255:
            print("MORE THAN 255, CUTTING")
            v = 255
        ret[i]=v
    return tuple(ret)

def print_differences(real_rgb, real_hsv, goal_rgb, goal_hsv):
    print(f"\nGot:\n\tRGB: {real_rgb}\n\tHSV: {real_hsv}\nExpected:\n\tRGB: {goal_rgb}\n\tHSV: {goal_hsv}")

for x in range(src1.get_width()):
    for y in range(src1.get_height()):
        src_x = (x+OFFX)%16
        src_y = (y+OFFY)%16
        color1 = src1.get_at((src_x,src_y))
        color2 = src2.get_at((src_x,src_y))
        
        dest.set_at((x,y),divide_tup(color1,color2))

pygame.image.save(dest, input("Dest: "))
