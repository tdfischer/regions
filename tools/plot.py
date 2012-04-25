#!/usr/bin/env python
from yaml import load
import random
from PIL import Image, ImageFont, ImageDraw
from PIL.ImageColor import getcolor
from argparse import ArgumentParser

parser = ArgumentParser(description='Plot a world')
parser.add_argument('config', help='path to config.yml')
parser.add_argument('--world', help='world name', dest='world', default='world')
parser.add_argument('--output', help='Output file', dest='output', default='plot.png')
args = parser.parse_args()

colorList = []
colors = open("/usr/share/config/colors/Web.colors")
for c in colors:
  colorTuple = c.strip().split(" ")
  try:
    colorList.append((int(colorTuple[0]), int(colorTuple[1]), int(colorTuple[2])))
  except Exception, e:
    pass

data = load(open(args.config))

regions = data['worlds'][args.world]['regions']

regionData = []

for region in regions:
  regionData.append({'name': region, 'x': regions[region]['x'], 'y': regions[region]['z'], 'color': random.choice(colorList)})

minX = 0
minY = 0
maxX = 0
maxY = 0
for region in regionData:
  if region['x'] < minX:
    minX = region['x']
  if region['y'] < minY:
    minY = region['y']
  if region['x'] > maxX:
    maxX = region['x']
  if region['y'] > maxY:
    maxY = region['y']
  print region['name'], region['color']

minX = minX-100
minY = minY-100
maxX = maxX+100
maxY = maxY+100

width = abs(maxX-minX)
height = abs(maxY-minY)

def nearest(x, y, regions):
  minDistance = 0
  nearest = None
  for region in regions:
    check = abs(region['x']-x)+abs(region['y']-y)
    #check = math.sqrt(abs(region['x']-x)**2+abs(region['y']-y)**2)
    if (minDistance == 0 or check < minDistance):
      nearest = region
      minDistance = check
  return nearest

img = Image.new("RGB", (width, height))
putpixel = img.im.putpixel
pix = img.load()
for x in range(0, width):
  worldX = minX + x
  for y in range(0, height):
    worldY = minY + y
    region = nearest(worldX, worldY, regionData)
    pix[x, y] = region['color']

  print "%d/%d"%(x, width)

draw = ImageDraw.Draw(img)
font = ImageFont.truetype("/usr/share/fonts/gnu-free/FreeSansBold.ttf", 24)
for region in regionData:
  regionX = region['x']+abs(minX)
  regionY = region['y']+abs(minY)
  draw.ellipse((regionX-10, regionY-10, regionX+10, regionY+10), fill=128)
  draw.text((regionX, regionY), region['name'], font=font)


img.save(args.output, "PNG")

