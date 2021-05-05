#!/bin/sh

#Sprite width/height
SSIZE="90"

BACKCOLOR_T="white"
BACKCOLOR_JEWELHOLE="transparent"
BORDERCOLOR_JEWEL="#000000"

#Line Color for Border of T
LCOLOR="#000000"
#Width of a line of the T
LWIDTH_T="3"

# Margin between Jewel Border and Border Line of T
MARGIN=10

COLORCIRCLEGREEN="#009245" #matt green
COLORCIRCLEBLUE="#0072bf" #matt blue
COLORCIRCLERED="#fe0001" #mattred

#INFO SPRITES = JEWELS + HOLES + BUTTON
function sprites {
  OUTJEWEL="android/assets/plates"
  mkdir -p $OUTJEWEL

  #Width of line around jewel/hole
  LWIDTH_JEWEL=5

  HOLECOLOR="#000000"
  #Command to generate hole that only depends later on the draw shape
  CONVERTHOLE="convert -size ${SSIZE}x${SSIZE} xc:$BACKCOLOR_JEWELHOLE -stroke $HOLECOLOR -strokewidth $LWIDTH_JEWEL -fill $HOLECOLOR -type TrueColorMatte -define png:color-type=6 -depth 8 "

  circle "GREEN"
  circle "BLUE"
  circle "RED"
}

#$1 = Color JEWEL
#$2 = draw Stuff
#$3 = OUTfile
function CONVERTJEWEL {
  FILLCOLOR="$1"
  DRAWCMDS="$2"
  OUTFILE="$3"
  eval "convert -size ${SSIZE}x${SSIZE} xc:$BACKCOLOR_JEWELHOLE -stroke \"$BORDERCOLOR_JEWEL\" -strokewidth $LWIDTH_JEWEL -fill \"$FILLCOLOR\" -type TrueColorMatte -define png:color-type=6 -depth 8 -draw \"$DRAWCMDS\" $OUTFILE"
}

#COLOR \in {GREEN, BLUE, RED}
function circle() {
  COLOR=$1
  FILLCOLOR="\$COLORCIRCLE$COLOR" #Get write variable name

  MID=$(((SSIZE - 1)/2)) #Round Down + Max Pixel is SSIZE-1
  RADIUS=$(( (SSIZE - 1 - LWIDTH_T - LWIDTH_JEWEL - MARGIN)/2 ))
  CIRCLE="circle $MID $MID $((MID - RADIUS)) $MID"

  CONVERTJEWEL "$FILLCOLOR" "$CIRCLE" "$OUTJEWEL/circle$COLOR.png"
}

function triangles() {
  FILLCOLOR="#00ff00" #Green
  uptri $FILLCOLOR
  lowtri $FILLCOLOR
}

function uptri() {
  FILLCOLOR="$1"

  #INFO polyline instead of polygon so that hypotenuse is not stroked to show that jewels can be merged
  UPTRI="polyline $((SSIZE - 1 - LWIDTH_T - MARGIN)) $((LWIDTH_T + MARGIN ))  $((LWIDTH_T + MARGIN )) $((LWIDTH_T + MARGIN )) $((LWIDTH_T + MARGIN )) $((SSIZE - 1 - LWIDTH_T - MARGIN)) "

  CONVERTJEWEL "$FILLCOLOR" "$UPTRI" "$OUTJEWEL/uptri.png"
  $CONVERTHOLE -draw "$UPTRI" "$OUTHOLE/uptri.png"
}

function lowtri() {
  FILLCOLOR="$1"

  #INFO polyline instead of polygon so that hypotenuse is not stroked to show that jewels can be merged
  UPTRI="polyline $((SSIZE - 1 - LWIDTH_T - MARGIN)) $((LWIDTH_T + MARGIN ))  $((LWIDTH_T + MARGIN )) $((LWIDTH_T + MARGIN )) $((LWIDTH_T + MARGIN )) $((SSIZE - 1 - LWIDTH_T - MARGIN)) "
  LOWTRI="polyline $((SSIZE - 1 - LWIDTH_T - MARGIN)) $((LWIDTH_T + MARGIN )) $((SSIZE - 1 - LWIDTH_T - MARGIN)) $((SSIZE - 1 - LWIDTH_T - MARGIN)) $((LWIDTH_T + MARGIN )) $((SSIZE - 1 - LWIDTH_T - MARGIN))    "
  CONVERTJEWEL "$FILLCOLOR" "$LOWTRI" "$OUTJEWEL/lowtri.png"

  $CONVERTHOLE -draw "$LOWTRI" "$OUTHOLE/lowtri.png"
}

function button() {
  OUTBUTTON="android/assets/button"
  mkdir -p $OUTBUTTON
  CORNER=5 #DEGREE of roundness button (\in Z, not only \in N)
  MARGINX=5
  MARGINY=40

  SSIZE_BUTTON=200 #INFO Button bigger than normal sprite

  FILLCOLOR="#F1ED10" #Bright Yellow
  FILLCOLOR_SHADOW="#9B9909" #Dark Yellow
  MARGIN_SHADOW=12 # Size of shadow downwards

  #INFO Button moves MARGIN_SHADOW up, not Shadow Down
  #INFO convert has shadow function, but I couldn't understand how to use it
  BUTTON="roundrectangle $((LWIDTH_T + MARGINX )) $((LWIDTH_T + MARGINY  - MARGIN_SHADOW)) $((SSIZE_BUTTON - 1 - LWIDTH_T - MARGINX )) $((SSIZE_BUTTON - 1 - LWIDTH_T - MARGINY - MARGIN_SHADOW)) $((LWIDTH_T + MARGINX + CORNER )) $((LWIDTH_T + MARGINY +CORNER - MARGIN_SHADOW)) "
  BUTTON_SHADOW="roundrectangle $((LWIDTH_T + MARGINX)) $((LWIDTH_T + MARGINY )) $((SSIZE_BUTTON - 1 - LWIDTH_T - MARGINX )) $((SSIZE_BUTTON - 1 - LWIDTH_T - MARGINY )) $((LWIDTH_T + MARGINX + CORNER )) $((LWIDTH_T + MARGINY +CORNER )) "
  CONVERTBUTTON "$FILLCOLOR" "$BUTTON" "$OUTBUTTON/buttonWithoutShadow.png"
  CONVERTBUTTON "$FILLCOLOR_SHADOW" "$BUTTON_SHADOW" "$OUTBUTTON/shadow.png"
  convert "$OUTBUTTON/shadow.png" "$OUTBUTTON/buttonWithoutShadow.png"  -composite "$OUTBUTTON/buttonShadow.png"

  #Resize Arrow, than add it with offset
  SIZEARROW=$((SSIZE_BUTTON - 2 * (MARGINY + LWIDTH_T + OFFSETARROW) ))
  convert "stuff/retryarrow.png" -resize "${SIZEARROW}x${SIZEARROW}" -type TrueColorMatte -define png:color-type=6 -depth 8 "$OUTBUTTON/arrowresize.png"
  convert "$OUTBUTTON/buttonShadow.png" "$OUTBUTTON/arrowresize.png" -gravity center -geometry "+0-$MARGIN_SHADOW" -composite "$OUTBUTTON/button.png"
}

#INFO Like CONVERT for jewels, only no stroke of different color
function CONVERTBUTTON {
  FILLCOLOR="$1"
  DRAWCMDS="$2"
  OUTFILE="$3"
  eval "convert -size ${SSIZE_BUTTON}x${SSIZE_BUTTON} xc:$BACKCOLOR_JEWELHOLE -stroke \"$BORDERCOLOR_JEWEL\" -strokewidth $LWIDTH_JEWEL -fill \"$FILLCOLOR\" -type TrueColorMatte -define png:color-type=6 -depth 8 -draw \"$DRAWCMDS\" $OUTFILE"
}

#INFO Create empty file which can be used as default value for textures
sprites
