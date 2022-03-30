#!/usr/bin/python3
import pygame
import colorsys

pygame.init()

src = pygame.image.load("/home/sam/MinecraftForge/Reference/Vanilla_Assets_And_Data/assets/minecraft/textures/item/netherite_ingot.png")
dest = pygame.Surface((src.get_width(),src.get_height()), pygame.SRCALPHA)
background = pygame.image.load("/home/sam/MinecraftForge/Reference/Vanilla_Assets_And_Data/assets/minecraft/textures/block/end_stone.png")

screen = pygame.display.set_mode([src.get_width()*50,src.get_height()*50])

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

def gen(hue, darken):
    for x in range(src.get_width()):
        for y in range(src.get_height()):
            c = src.get_at((x,y))
            hsv = colorsys.rgb_to_hsv(c[0], c[1], c[2])
            hsv = (0, hsv[1], hsv[2])
            color = colorsys.hsv_to_rgb(hsv[0]+hue, hsv[1]+darken, hsv[2])
            color = (color[0], color[1], color[2], c[3])
            #print(f"rgb {c}, hsv {hsv}, color {color}")
            dest.set_at((x,y),cut(color, 255, 0))

h = 0
d = 0
kg = True
while kg:
    for event in pygame.event.get():
        if event.type==pygame.QUIT:
            kg = False
        elif event.type==pygame.MOUSEBUTTONDOWN:
            print(h, d)
            pygame.image.save(dest, "/home/sam/MinecraftForge/AdvancedForcefields/tools/end_ingot.png")#input("Dest: "))
        elif event.type==pygame.MOUSEMOTION:
            mx, my = pygame.mouse.get_pos()
            h = mx/screen.get_width()
            d = my/screen.get_height()
            gen(h, d)
            screen.fill((0,0,0))
            screen.blit(pygame.transform.scale(background, (screen.get_width(), screen.get_height())), (0,0))
            screen.blit(pygame.transform.scale(dest, (screen.get_width(), screen.get_height())), (0,0))
            pygame.display.update()
pygame.quit()
