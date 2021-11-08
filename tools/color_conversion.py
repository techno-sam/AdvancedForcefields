#!/usr/bin/python3
#import pygame
import colorsys
from matplotlib import pyplot as plt
from matplotlib import style

#pygame.init()

#src = pygame.image.load(input("Src: "))
#dest = pygame.Surface((src.get_width(),src.get_height()))

OFFX = 8
OFFY = -3

SHOULD_OFFSET = False

colormap = {}
colormap[82]  = (246, 250, 189)
colormap[97]  = (238, 246, 180)
colormap[109] = (222, 230, 164)
colormap[136] = (213, 218, 148)
colormap[166] = (205, 198, 139)
colormap[181] = (197, 190, 139)
"""
colormap[82]  = (246, 250, 189)
colormap[90]  = (206, 206, 150)
colormap[97]  = (238, 246, 180)
#colormap[99]  = (169, 162, 116)
colormap[107] = (194, 187, 136)
colormap[109] = (222, 230, 164)
colormap[119] = (232, 244, 178)
colormap[127] = (232, 244, 178)
colormap[136] = (213, 218, 148)
colormap[138] = (194, 187, 136)
colormap[155] = (221, 228, 165)
colormap[166] = (205, 198, 139)
colormap[181] = (197, 190, 139)"""

def div_tup(tup, x):
    ret = []
    for v in tup:
        ret.append(v/x)
    return ret

for k in colormap:
    print(f"{div_tup(colormap[k],k)}")

x = list(colormap.keys())
r = []
g = []
b = []
grey = list(colormap.keys())
grey.reverse()
for k in colormap:
    v = colormap[k]
    r.append(v[0])
    g.append(v[1])
    b.append(v[2])

print(f"x: {x}\nr: {r}\ng: {g}\nb: {b}")

plt.plot(x,r,color='red')
plt.plot(x,g,color='green')
plt.plot(x,b,color='blue')
plt.plot(x,grey,color='grey')

plt.show()
'''
for x in range(dest.get_width()):
    for y in range(dest.get_height()):
        if SHOULD_OFFSET:
            src_x = (x+OFFX)%16
            src_y = (y+OFFY)%16
        else:
            src_x = x
            src_y = y

        color = src.get_at((src_x,src_y))
        
        dest.set_at((x,y),color)

pygame.image.save(dest, input("Dest: "))
'''
