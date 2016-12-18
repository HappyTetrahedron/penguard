#!/bin/bash
echo "Building pdf..."
pandoc -t beamer --template template.tex --listings pres.md -o pres.pdf \
    && echo "Build successful"
