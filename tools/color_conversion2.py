#!/usr/bin/python3
import pygame
import colorsys
#from matplotlib import pyplot as plt
#from matplotlib import style
from scipy.interpolate import make_interp_spline, BSpline
import numpy as np

pygame.init()

#src = pygame.image.load(input("Src: "))
dest = pygame.Surface((256,256))

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

xold = list(colormap.keys())
#xnew = np.linspace(82,181,300)
POINTS = 256
xnew = np.linspace(0,255,POINTS)
r = []
g = []
b = []
grey = list(colormap.keys())
#grey.reverse()
for k in colormap:
    v = colormap[k]
    r.append(v[0])
    g.append(v[1])
    b.append(v[2])

def smooth(xold, xnew, y):
    spl = make_interp_spline(xold, y, k=3)
    return spl(xnew)

def cut(LIST,MAX,MIN):
    ret = []
    for l in LIST:
        ret.append(min(max(l,MIN),MAX))
    return ret

r = cut(smooth(xold,xnew,r),255,0)
g = cut(smooth(xold,xnew,g),255,0)
b = cut(smooth(xold,xnew,b),255,0)
grey = cut(smooth(xold,xnew,grey),255,0)

#print(f"x: {x}\nr: {r}\ng: {g}\nb: {b}")

for x in range(256):
    color = (int(r[x]),int(g[x]),int(b[x]))
    for y in range(dest.get_height()):
        if y>225 and x in xold:
            color = (255,0,0)
        dest.set_at((x,y),color)

pygame.image.save(dest, input("Dest: "))
