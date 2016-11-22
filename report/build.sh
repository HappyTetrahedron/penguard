#!/bin/bash

bibtex report > /dev/null

texi2pdf report.tex > /dev/null

pandoc report.tex -o ../README.md
